package com.createicbm.content.blast;

import com.createicbm.client.ICBMSounds;
import com.createicbm.CreateICBM;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.events.BlastBuildEvent;
import com.createicbm.api.explosion.IBlastInit;
import com.createicbm.api.explosion.IBlastRestore;
import com.createicbm.api.explosion.IBlastTickable;
import com.createicbm.api.reg.IExplosiveData;
import com.createicbm.config.ConfigDebug;
import com.createicbm.content.blast.thread.ChunkSnapshot;
import com.createicbm.content.blast.thread.ThreadExplosion;
import com.createicbm.content.entity.EntityExplosion;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.actions.WorkTickingActionHandler;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.transform.vector.Location;
import com.createicbm.mods.sable.SableProxy;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Explosion;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prefab for any Explosion/Blast object created
 */
@Deprecated
public abstract class Blast implements IBlastInit, IBlastRestore
{
    // 1.21 Explosion has final fields and needs a level in its constructor, so the fields it used are kept here with
    // the same names and a vanilla Explosion is created on demand by asVanillaExplosion() for events, damage sources
    // and resistance checks.
    protected Level world;
    protected double x, y, z;
    protected float size;
    protected Entity exploder;
    private final List<BlockPos> affectedBlockPositions = new ArrayList<>();
    /** Cached vanilla explosion, reset when position/size/world changes */
    private Explosion vanillaExplosion;
    /**
     * When set, block drops are merged into this list like vanilla explosions do and the caller pops them once all
     * blocks are broken. PERF: one item entity per drop was spawned before, hundreds per small blast.
     */
    @Nullable
    protected List<Pair<ItemStack, BlockPos>> dropCollector = null;

    //Thread stuff
    private ThreadExplosion thread;
    protected ConcurrentLinkedQueue<BlockPos> threadResults;
    /** Written by the explosion thread, read by the server thread */
    private volatile boolean threadComplete = false;
    private volatile ChunkSnapshot chunkSnapshot;

    //TODO remove position as we are double storing location data
    public Location location;

    /**
     * Host of the blast
     */
    public Entity controller = null;

    /**
     * Is the blast alive, if false the blast is dead
     */
    private boolean isAlive = true;

    /**
     * The amount of times the explosion has been called
     */
    protected int callCount = 0;

    private boolean hasSetupBlast = false;

    private boolean hasBuilt = false;

    private IExplosiveData explosiveData;
    private IActionSource actionSource;

    /** Sounds this blast started with {@link #playBlastSound}, faded out when the blast ends */
    private long soundKey = 0;
    private long soundTime = 0;

    /**
     * Only use the default if you plan to init required data
     */
    public Blast()
    {
        //Fields are set by the builder methods
    }

    public Blast(Level world, double x, double y, double z) {
        this();
        setBlastWorld(world);
        setBlastPosition(x, y, z);
    }

    @Override
    @Nonnull
    public IActionSource getSource() {
        return this.actionSource;
    }

    /**
     * Gets data used to create this action
     *
     * @return data
     */
    @Override
    @Nonnull
    public IExplosiveData getActionData() {
        return explosiveData;
    }

    @Override
    public IBlastInit setActionSource(IActionSource source) {
        this.actionSource = source;
        return this;
    }

    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        try
        {
            if (!this.world().isClientSide())
            {
                //Forge event, allows for interaction and canceling the explosion
                if (EventHooks.onExplosionStart(world, asVanillaExplosion()))
                {
                    return ActionResponses.EXPLOSION_CANCELED;
                }

                //Play audio to confirm explosion triggered
                playExplodeSound();
                playBlastSoundForViewers();

                // Sable ships nearby keep their blocks in a plot the blast doesn't reach
                SableProxy.INSTANCE.damageShips(world, exploder, x, y, z, getShipDamageRadius());

                //Start explosion
                if (this instanceof IBlastTickable)
                {
                    if (!this.world().addFreshEntity(new EntityExplosion(this)))
                    {
                        isAlive = false;
                        return ActionResponses.ENTITY_SPAWN_FAILED;
                    }
                    return ActionResponses.COMPLETED;
                }
                else
                {
                    //Do setup tasks
                    if (!this.doFirstSetup())
                    {
                        return BlastStatus.SETUP_ERROR; //TODO specify why we failed during setup
                    }

                    //Call explosive, only complete if true
                    if (this.doExplode(-1))
                    {
                        this.completeBlast();
                    }
                }
            }
            else
            {
                clientRunBlast();
            }
            return ActionResponses.COMPLETED;
        }
        catch (Exception e)
        {
            CreateICBM.logger().error(this + ": Unexpected error running blast", e);
            endBlast();
            return ActionResponses.UNKNOWN_ERROR; //TODO provide dynamic data
        }
    }

    //Handles client only stuff
    protected void clientRunBlast()
    {

    }

    /**
     * Called by ticking explosives
     *
     * @param ticksExisted
     * @return
     */
    public boolean onBlastTick(int ticksExisted)
    {
        if (!world.isClientSide())
        {
            //Do setup work
            if (!doFirstSetup())
            {
                return false;
            }

            //Do ticks
            if (isAlive)
            {
                if (this.isCompleted() || this.doExplode(callCount++))
                {
                    completeBlast();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called to start the blast and run setup code
     *
     * @return true if the blast should continue to run, false otherwhise
     */
    @Deprecated // TODO inline with runBlast to provide proper feedback using IActionStatus
    public final boolean doFirstSetup()
    {
        if (isAlive && !hasSetupBlast)
        {
            hasSetupBlast = true;
            WorkTickingActionHandler.add(this);
            return this.setupBlast();
        }

        return true;
    }

    /**
     * @return true if the blast should continue to run, false otherwhise
     */
    protected boolean setupBlast()
    {
        return true;
    }

    /**
     * Called each tick of the blast
     *
     * @param callCount - call count or -1 to indicate this is not a ticking explosive
     * @return true to finish, false to continue/indicate the blast is doing something external like a thread
     */
    protected boolean doExplode(int callCount)
    {
        return false;
    }

    /**
     * Called to kill the blast and run last min code
     */
    public final void completeBlast()
    {
        if (isAlive)
        {
            this.endBlast();

            //Run post code
            this.onBlastCompleted();
        }
    }

    /**
     * Internal call for running post blast code
     * Do not se the entity or blast dead. This is completed
     * in the {@link #completeBlast()} method.
     */
    protected void onBlastCompleted()
    {
        clearBlast();
    }

    /**
     * Called each tick the blast has moved
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public void onPositionUpdate(double posX, double posY, double posZ)
    {
        setPosition(posX, posY, posZ);
    }

    /**
     * Called to set the position of the blast. Only call this for initialization, anything
     * that happens during world tick should call {@link #onPositionUpdate(double, double, double)}
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public Blast setPosition(double posX, double posY, double posZ)
    {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.vanillaExplosion = null;
        location = new Location(world, posX, posY, posZ);
        //TODO super contains a vec3 also called position, we need to set that value instead of overriding the return
        return this;
    }

    @Override
    public Vec3 getPosition()
    {
        return this.location.toVec3d();
    }

    /**
     * Creates a vanilla explosion matching this blast. Used where Minecraft or NeoForge APIs
     * require an {@link Explosion} (events, damage sources, block resistance checks).
     * The returned object is never exploded, it only carries the position, size and source.
     *
     * @return new vanilla explosion instance, never null if the world is set
     */
    public Explosion asVanillaExplosion()
    {
        if (vanillaExplosion == null)
        {
            vanillaExplosion = new Explosion(world, exploder, x, y, z, Math.max(size, 0), false, Explosion.BlockInteraction.DESTROY);
        }
        return vanillaExplosion;
    }

    public List<BlockPos> getAffectedBlockPositions()
    {
        return affectedBlockPositions;
    }

    /**
     * Fires {@link ExplosionEvent.Detonate} so other mods can edit the affected block and entity lists.
     * The list is copied to the vanilla explosion and copied back after the event.
     *
     * @param entities affected entities, may be modified by listeners
     */
    protected void fireDetonateEvent(List<Entity> entities)
    {
        final Explosion explosion = asVanillaExplosion();
        explosion.getToBlow().clear();
        explosion.getToBlow().addAll(affectedBlockPositions);
        NeoForge.EVENT_BUS.post(new ExplosionEvent.Detonate(world, explosion, entities));
        affectedBlockPositions.clear();
        affectedBlockPositions.addAll(explosion.getToBlow());
    }

    /**
     * For blasts that edit blocks on their own instead of through {@link #fireDetonateEvent(List)}: hands the
     * positions to {@link ExplosionEvent.Detonate} so claim and protection mods can take theirs out, the same way they
     * guard vanilla explosions. See {@link BlastProtection}.
     *
     * @param positions blocks the blast is about to edit, filtered in place
     */
    protected void filterBlockEdits(Collection<BlockPos> positions)
    {
        final Set<BlockPos> denied = deniedBlockEdits(positions);
        if (!denied.isEmpty())
        {
            positions.removeIf(denied::contains);
        }
    }

    /**
     * Asks claim and protection mods about the positions, see {@link #filterBlockEdits(Collection)}. Server thread only.
     *
     * @param positions blocks the blast wants to edit, not changed
     * @return the positions a listener took out, empty when all are allowed
     */
    protected Set<BlockPos> deniedBlockEdits(Collection<BlockPos> positions)
    {
        return BlastProtection.denied(world, asVanillaExplosion(), positions);
    }

    public float getBlockResistance(BlockPos pos, BlockState state)
    {
        return state.getExplosionResistance(world, pos, asVanillaExplosion());
    }

    /**
     * The loot table survives_explosion condition applies the 1 / radius chance from
     * {@link LootContextParams#EXPLOSION_RADIUS}.
     *
     * @param explosionRadius - radius of the explosion, not the chance. Loot tables drop with chance 1 / radius.
     */
    protected void dropBlockAsItemWithChance(BlockPos pos, BlockState state, float explosionRadius)
    {
        if (world instanceof ServerLevel serverLevel)
        {
            final BlockEntity tile = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            final LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, tile)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, exploder)
                .withParameter(LootContextParams.EXPLOSION_RADIUS, explosionRadius);
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
            final List<Pair<ItemStack, BlockPos>> collector = this.dropCollector;
            state.getDrops(builder).forEach(stack -> {
                if (collector != null)
                {
                    addOrAppendStack(collector, stack, pos);
                }
                else
                {
                    Block.popResource(world, pos, stack);
                }
            });
        }
    }

    /** Same as vanilla Explosion#addOrAppendStack, merges a drop into the first stacks it fits on */
    protected static void addOrAppendStack(List<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos)
    {
        for (int i = 0; i < drops.size(); i++)
        {
            final Pair<ItemStack, BlockPos> pair = drops.get(i);
            final ItemStack existing = pair.getFirst();
            if (ItemEntity.areMergable(existing, stack))
            {
                drops.set(i, Pair.of(ItemEntity.merge(existing, stack, 16), pair.getSecond()));
                if (stack.isEmpty())
                {
                    return;
                }
            }
        }
        drops.add(Pair.of(stack, pos));
    }

    /**
     * Drops (when allowed) then removes the block.
     */
    protected void destroyBlockByBlast(BlockPos pos, BlockState state, boolean doDrops)
    {
        final Explosion explosion = asVanillaExplosion();
        if (doDrops && state.canDropFromExplosion(world, pos, explosion))
        {
            dropBlockAsItemWithChance(pos, state, this.getBlastRadius());
        }
        state.onBlockExploded(world, pos, explosion);
    }

    /**
     * Plays a sound that fades out once this blast has finished, for long sounds of blasts that run over many ticks
     */
    protected void playBlastSound(ICBMSounds sound, double x, double y, double z, float volume, float pitch)
    {
        if (world.isClientSide())
        {
            sound.play(world, x, y, z, volume, pitch, true);
            return;
        }
        if (soundKey == 0)
        {
            soundKey = ICBMSounds.nextTrackedKey();
        }
        soundTime = world.getGameTime();
        sound.playTracked(world, soundKey, x, y, z, volume, pitch);
    }

    private void stopBlastSounds()
    {
        // Nothing to cut short once the longest sound (about 11 seconds, 20 at the lowest pitch) is over
        if (soundKey != 0 && world != null && world.getGameTime() - soundTime < 420)
        {
            ICBMSounds.stopTracked(world, soundKey);
        }
        soundKey = 0;
    }

    /**
     * @return true to play the distant explosion for everybody who can see this blast, see {@link BlastSounds}.
     * Blasts that are not explosions (gas, effects, vortexes) and the helper blasts of others turn it off.
     */
    protected boolean playsDistantSound()
    {
        return getBlastRadius() >= 12;
    }

    /** @return blocks around the blast where {@link #getNearSound()} is played at full volume, 0 for none */
    protected double getNearSoundRadius()
    {
        return 0;
    }

    /** @return the blast's own sound for players inside {@link #getNearSoundRadius()}, null for none */
    protected ICBMSounds getNearSound()
    {
        return null;
    }

    /** @return volume of {@link #getNearSound()} */
    protected float getNearSoundVolume()
    {
        return 4.0F;
    }

    private void playBlastSoundForViewers()
    {
        if (world instanceof ServerLevel level && (playsDistantSound() || getNearSound() != null))
        {
            BlastSounds.play(level, x, y, z,
                getNearSound(), getNearSoundVolume(), getNearSoundRadius(),
                playsDistantSound() ? ICBMSounds.EXPLOSION_DISTANT : null, 1.0F);
        }
    }

    protected void playExplodeSound()
    {
        this.world.playSound((Player) null,
                this.x, this.y, this.z,
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F);
    }

    /**
     * @return radius of ship blocks this blast breaks, see {@link SableProxy#damageShips}. 0 for blasts that don't break blocks
     */
    protected float getShipDamageRadius()
    {
        return 0;
    }

    /**
     * Radius of the blast
     *
     * @return
     */
    public float getBlastRadius()
    {
        return Math.max(3, this.size);
    }

    protected boolean doDamageEntities(float radius, float power)
    {
        return this.doDamageEntities(radius, power, true);
    }

    protected List<Entity> getEntities(double radius) {
        Location minCoord = location.add(-radius - 1); //TODO drop need for location
        Location maxCoord = location.add(radius + 1);
        return world().getEntitiesOfClass(Entity.class, new AABB(minCoord.xi(), minCoord.yi(), minCoord.zi(), maxCoord.xi(), maxCoord.yi(), maxCoord.zi()));
    }

    /**
     * @return true if the method ran successfully, false if it was interrupted
     */
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem)
    {
        final List<Entity> allEntities = getEntities(radius * 2);
        return doDamageEntities(allEntities, radius, power, destroyItem);
    }

    protected boolean  doDamageEntities(List<Entity> entities, float radius, float power, boolean destroyItem) {
        final Vec3 center = new Vec3(location.x(), location.y(), location.z());
        for (Entity entity : entities) {
            if (this.onDamageEntity(entity)) {
                continue;
            }

            if (entity instanceof ItemEntity && !destroyItem) {
                continue;
            }

            double distance = Math.sqrt(entity.distanceToSqr(location.x(), location.y(), location.z())) / radius;

            if (distance <= 1.0D) {
                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();

                double mag = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference); //TODO switch to sq for better speed
                if (mag < 1.0E-6)
                {
                    mag = 1; // exactly on the center, a zero length direction would turn the velocity into NaN
                }

                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                double blockDensity = Explosion.getSeenPercent(center, entity);
                double damageScale = (1.0D - distance) * blockDensity;
                int damage = (int) ((damageScale * damageScale + damageScale) / 2.0D * 8.0D * power + 1.0D); //TODO magic numbers

                entity.hurt(getDamageSource(), damage);

                entity.setDeltaMovement(entity.getDeltaMovement().add(xDifference * damageScale, yDifference * damageScale, zDifference * damageScale));
            }
        }

        return true;
    }

    /**
     * Call after changing the velocity of an entity on the server. Players move themselves, the server only sends
     * them a velocity change when hurtMarked is set (hurt() does this for damage knockback).
     *
     * @param entity - entity that had its velocity changed
     */
    protected static void markVelocityChanged(Entity entity)
    {
        if (entity instanceof ServerPlayer)
        {
            entity.hurtMarked = true;
        }
    }

    protected DamageSource getDamageSource() {
        return world.damageSources().explosion(asVanillaExplosion());
    }

    /**
     * Called by doDamageEntity on each entity being damaged. This function should be inherited if
     * something special is to happen to a specific entity.
     *
     * @return True if something special happens to this specific entity.
     */
    protected boolean onDamageEntity(Entity entity)
    {
        return false;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        this.callCount = nbt.getInt(NBTConstants.CALL_COUNT);
        this.size = nbt.getFloat(NBTConstants.EXPLOSION_SIZE);

        if (world instanceof ServerLevel && nbt.hasUUID(NBTConstants.BLAST_EXPLODER_ENT_ID)) //don't load the exploder if it hasn't been saved
        {
            exploder = ((ServerLevel) world).getEntity(nbt.getUUID(NBTConstants.BLAST_EXPLODER_ENT_ID));
        }
    }

    @Override
    public void save(CompoundTag nbt)
    {
        nbt.putInt(NBTConstants.CALL_COUNT, this.callCount);
        nbt.putFloat(NBTConstants.EXPLOSION_SIZE, this.size);

        if (world instanceof ServerLevel && exploder != null) //don't save the exploder if there is none to save. TODO: do we even need to save it at all?
        {
            nbt.putUUID(NBTConstants.BLAST_EXPLODER_ENT_ID, this.exploder.getUUID());
        }
    }

    public boolean isMovable()
    {
        return false;
    }

    @Override
    public Level world()
    {
        return world;
    }

    @Override
    public Level getWorld() {
        return world;
    }

    @Override
    public double x()
    {
        return this.location.x();
    }

    @Override
    public double y()
    {
        return this.location.y();
    }

    @Override
    public double z()
    {
        return this.location.z();
    }

    @Override
    public Entity getBlastSource()
    {
        return exploder;
    }

    protected void createAndStartThread(ThreadExplosion thread)
    {
        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            CreateICBM.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread set");
        }

        if (this.thread != null && !this.thread.isComplete)
        {
            CreateICBM.logger().info("Blast#createAndStartThread(" + thread + ") -> Error new thread was set before last finished\nLast: " + thread);
        }

        //Store thread instance
        this.thread = thread;

        //Reset thread state
        this.threadComplete = false;

        //Start thread
        this.thread.start();

        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            CreateICBM.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread started: " + thread.isAlive());
        }
    }

    /**
     * Captures the loaded chunks around the blast for {@link #getBlockStateThreaded(BlockPos)}. Call on the
     * server thread right before handing work to a worker thread.
     *
     * @param radius - blocks the worker reads around the blast center
     */
    protected void captureChunkSnapshot(double radius)
    {
        // Workers use it for resistance checks, create it here instead of lazily on the worker thread
        asVanillaExplosion();
        chunkSnapshot = ChunkSnapshot.capture(world, location.x(), location.z(), (int) Math.ceil(radius) + 2);
    }

    /**
     * Drops the chunk references once the worker is done reading
     */
    protected void releaseChunkSnapshot()
    {
        chunkSnapshot = null;
    }

    /**
     * Block state lookup for worker threads, reads the chunks captured by {@link #captureChunkSnapshot(double)}.
     * In 1.21 that hands each read to the server thread and waits for it, see {@link ChunkSnapshot}.
     */
    public BlockState getBlockStateThreaded(BlockPos pos)
    {
        final ChunkSnapshot snapshot = chunkSnapshot;
        return snapshot != null ? snapshot.getBlockState(pos) : world.getBlockState(pos);
    }

    /**
     * Worker thread version of {@link Level#isLoaded(BlockPos)}, see {@link #getBlockStateThreaded(BlockPos)}
     */
    public boolean isLoadedThreaded(BlockPos pos)
    {
        final ChunkSnapshot snapshot = chunkSnapshot;
        return snapshot != null ? snapshot.isLoaded(pos) : world.isLoaded(pos);
    }

    /**
     * Runs a task on the server thread, used by worker threads to hand results back to the world.
     * MinecraftServer#execute runs the task right away on the calling worker thread once the server is stopping, so the
     * task is always queued and is dropped when the server stopped or the level was replaced before it runs.
     *
     * @param task - task to run
     */
    protected void scheduleOnServerThread(Runnable task)
    {
        if (world instanceof ServerLevel serverLevel)
        {
            final MinecraftServer server = serverLevel.getServer();
            server.tell(new TickTask(server.getTickCount(), () -> {
                if (!server.isRunning() || server.getLevel(serverLevel.dimension()) != serverLevel)
                {
                    return;
                }
                task.run();
            }));
        }
    }

    protected boolean isThreadCompleted()
    {
        return threadComplete || thread != null && thread.isComplete;
    }

    /**
     * Called from the explosive thread to mark as completed
     *
     * @param exThread
     */
    public synchronized void markThreadCompleted(ThreadExplosion exThread) //This method is a work around for thread instance going null
    {
        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            CreateICBM.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Thread responded that is has completed, Blast: " + this);
        }
        if (thread == null || thread == exThread)
        {
            threadComplete = true;
        }
        else
        {
            CreateICBM.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Error thread attempted to mark for complete but did not match current thread \nCurrent: " + thread + "\nBlast: " + this);
        }
    }

    public void addThreadResult(BlockPos pos)
    {
        getThreadResults().add(pos);
    }

    protected ConcurrentLinkedQueue getThreadResults()
    {
        if (threadResults == null)
        {
            threadResults = new ConcurrentLinkedQueue();
        }
        return threadResults;
    }

    public ThreadExplosion getThread()
    {
        return thread;
    }

    //================================================
    //=====             Properties              ======
    //================================================

    @Override
    public IBlastInit setBlastSource(Entity entity)
    {
        checkBuilt();
        this.exploder = entity;
        this.vanillaExplosion = null;
        return this;
    }

    @Override
    public Blast setBlastSize(double power)
    {
        checkBuilt();
        this.size = (float) power;
        this.vanillaExplosion = null;
        return this;
    }

    @Override
    public Blast scaleBlast(double scale)
    {
        checkBuilt();
        this.size *= scale;
        this.vanillaExplosion = null;
        return this;
    }

    @Override
    public Blast setBlastWorld(Level world)
    {
        checkBuilt();
        this.world = world;
        this.vanillaExplosion = null;
        return this;
    }

    @Override
    public Blast setBlastPosition(double posX, double posY, double posZ)
    {
        checkBuilt();
        setPosition(posX, posY, posZ);
        return this;
    }

    @Override
    public IBlastInit setExplosiveData(IExplosiveData data)
    {
        checkBuilt();
        this.explosiveData = data;
        return this;
    }

    @Override
    public IBlastInit setCustomData(@Nonnull CompoundTag customData)
    {
        return this;
    }

    @Override
    public boolean isCompleted()
    {
        return !isAlive;
    }

    @Override
    public IBlastInit setEntityController(Entity entityController)
    {
        this.controller = entityController;
        return this;
    }

    @Nullable
    @Override
    public Entity getEntity()
    {
        return controller;
    }

    private final void checkBuilt()
    {
        if (hasBuilt)
        {
            throw new RuntimeException("Can not init properties of a blast after it has built");
        }
    }

    @Override
    public Blast buildBlast()
    {
        if (hasBuilt)
        {
            throw new RuntimeException("Blast has already been built");
        }
        NeoForge.EVENT_BUS.post(new BlastBuildEvent(this));
        hasBuilt = true;
        return this;
    }

    @Override
    public void clearBlast()
    {
       endBlast();
    }

    protected void endBlast() {
        isAlive = false;
        stopBlastSounds();
        WorkTickingActionHandler.remove(this);

        if (getThread() != null)
        {
            getThread().kill();
        }
        if (controller != null)
        {
            controller.discard();
        }
    }
}
