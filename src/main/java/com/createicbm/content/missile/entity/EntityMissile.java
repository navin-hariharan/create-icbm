package com.createicbm.content.missile.entity;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.events.MissileEvent;
import com.createicbm.api.events.MissileRideEvent;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.logic.flight.DeadFlightLogic;
import com.createicbm.content.missile.tracker.MissileChunkLoader;
import com.createicbm.lib.EntityTicking;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.lib.RegistryHelper;
import com.createicbm.lib.capability.chicken.CapSpaceChicken;
import com.createicbm.lib.capability.holder.ICapabilityHolderEntity;
import com.createicbm.lib.network.IPacket;
import com.createicbm.lib.network.IPacketIDReceiver;
import com.createicbm.lib.network.lambda.PacketCodexReg;
import com.createicbm.lib.network.lambda.entity.PacketCodexEntity;
import com.createicbm.lib.projectile.EntityProjectile;
import com.createicbm.lib.radar.RadarRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.saving.NbtSaveNode;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public abstract class EntityMissile<E extends EntityMissile<E>> extends EntityProjectile<E> implements IEntityWithComplexSpawn, IPacketIDReceiver, ICapabilityHolderEntity
{
    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();

    @Getter
    private final CapabilityMissile missileCapability = new CapabilityMissile(this); //TODO refactor to use interface so parts can be better customized

    /** Toggle to note the missile has impacted something and already triggered impact logic */
    protected boolean hasImpacted = false;

    protected boolean syncClient = false;

    /** Client only: the looping engine sound following this missile, null while none plays */
    public Object engineSound;
    /** Client only: last tick the engine effects ran, the engine sound stops once they do */
    public int lastEngineTick;

    /** Client only: keeps the missile flying smoothly between the position updates of the server */
    private final MissileInterpolator interpolator = new MissileInterpolator();
    /** Client only: flight logic of this tick when its engine effects are due, they run once the position is final */
    private IMissileFlightLogic pendingEngineLogic;

    /** Set when the missile is removed to be simulated rather than because its flight ended */
    private boolean leftForSimulation = false;

    /** Server only: chunks held ahead of a straight flying missile so it doesn't freeze at the simulation distance */
    @Getter
    private final MissileChunkLoader chunkLoader = new MissileChunkLoader();

    /** Server only: server tick of the last tick this missile ran, a missile an optimization mod stops is rescued */
    private int lastServerTick = -1;

    public EntityMissile(EntityType<? extends EntityMissile> type, Level world)
    {
        super(type, world);
        this.hasHealth = true;
        EntityTicking.keepTicking(this);
    }

    @Nullable
    @Override
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if (capability == CreateICBMAPI.MISSILE_CAPABILITY)
        {
            return (T) getMissileCapability();
        }
        return null;
    }

    public EntityMissile<E> ignore(Entity entity)
    {
        collisionIgnoreList.add(entity);
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && level().getServer() != null)
        {
            lastServerTick = level().getServer().getTickCount();
        }

        if (level().isClientSide() && !isRemoved())
        {
            smoothClientMotion();

            // Smoke and sound follow the smoothed position, the trail is drawn along the way flown this tick
            final IMissileFlightLogic logic = pendingEngineLogic;
            if (logic != null)
            {
                pendingEngineLogic = null;
                CreateICBM.proxy.spawnMissileSmoke(this, logic, ticksInAir);
                CreateICBM.proxy.onMissileEngineTick(this);
            }
        }

        // Chunk tickets are renewed by MissileChunkLoader from the level tick, a frozen missile doesn't reach this
        if(syncClient) {
            this.syncClient = false;
            if(!level().isClientSide()) {
                PACKET_DESC.sendToAllAround(this);
            }
        }
    }

    @Override
    protected void updateMotion()
    {
        if (getMissileCapability().canRunFlightLogic())
        {
            final IMissileFlightLogic logic = getMissileCapability().getFlightLogic();
            if (logic != null) {
                logic.onEntityTick(this, missileCapability, ticksInAir);

                if(logic.shouldRunEngineEffects(this) && level().isClientSide()) {
                    pendingEngineLogic = logic;
                }
            }

            //Trigger events
            CreateICBMAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(getMissileCapability());
        }

        super.updateMotion();
    }

    /**
     * Client only. The server position of a missile arrives every tick or two and is a few ticks old, jumping to it
     * made a fast missile move in 20 steps a second. The missile flies on by itself with its velocity and is only
     * pulled towards the server position, see {@link MissileInterpolator}.
     */
    private void smoothClientMotion()
    {
        if (freezeMotion || !interpolator.hasTarget())
        {
            return;
        }
        final Vec3 motion = getDeltaMovement();
        interpolator.step(getX(), getY(), getZ(), motion.x, motion.y, motion.z);
        setPos(interpolator.x(), interpolator.y(), interpolator.z());
        setYRot(interpolator.yaw(getYRot()));
        setXRot(interpolator.pitch(getXRot()));
        if (interpolator.snapped())
        {
            // A jump (teleport, dimension hop) must not be drawn as a very fast flight
            setOldPosAndRot();
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements)
    {
        if (!interpolator.hasTarget())
        {
            // First update, the missile appears there
            super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements);
        }
        interpolator.setTarget(x, y, z, yaw, pitch);
    }

    @Override
    protected void decreaseMotion()
    {
        if (getMissileCapability().getFlightLogic() == null || getMissileCapability().getFlightLogic().shouldDecreaseMotion(this))
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void handleEntityCollision(@Nonnull HitResult hit, @Nonnull Entity entityHit)
    {
        if(entityHit instanceof Chicken chicken) { //TODO baby zombie for lolz?
            if(getVehicle() == null) {
                ICBMSounds.MEEP.play(entityHit, 2, 1, true);
                entityHit.startRiding(this, true);
                CapSpaceChicken.setSpace(chicken, true);
            }
        }
        else
        {
            onImpactEntity(entityHit, (float) getVelocity().magnitude(), hit);
        }
    }

    /**
     * 1.21 asks the moving entity, so the ignore list only applies to the missile moving into others.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity)
    {
        return !collisionIgnoreList.contains(entity) && super.canCollideWith(entity);
    }

    /**
     * Missiles in flight that are loaded back from a saved chunk were invisible to radar stations and SAMs until
     * relaunched.
     */
    @Override
    public void onAddedToLevel()
    {
        super.onAddedToLevel();
        if (!level().isClientSide())
        {
            // PERF: players tracking the missile were just sent its spawn data, which already holds everything the
            // description packet would resend on the first tick (setFlightLogic during spawn/load sets the flag)
            this.syncClient = false;
            MissileChunkLoader.track(this);
            if (getMissileCapability().canRunFlightLogic())
            {
                RadarRegistry.add(this);
            }
            ICBMDiagnostics.info("MISSILE_ADDED id={} uuid={} type={} dim={} pos={} velocity={} noCulling={} tickCount={} flightLogic={} canRunFlightLogic={}",
                getId(), getUUID(), getClass().getSimpleName(), level().dimension().location(), position(), getDeltaMovement(), noCulling,
                tickCount, getMissileCapability().getFlightLogic() == null ? "none" : getMissileCapability().getFlightLogic().getClass().getSimpleName(),
                getMissileCapability().canRunFlightLogic());
        }
    }

    @Override
    public void remove(RemovalReason reason)
    {
        if (!level().isClientSide())
        {
            ICBMDiagnostics.info("MISSILE_REMOVED id={} uuid={} type={} reason={} dim={} pos={} tickCount={} leftForSimulation={} flightLogic={}",
                getId(), getUUID(), getClass().getSimpleName(), reason, level().dimension().location(), position(), tickCount, leftForSimulation,
                getMissileCapability().getFlightLogic() == null ? "none" : getMissileCapability().getFlightLogic().getClass().getSimpleName());
            RadarRegistry.remove(this);
            MissileChunkLoader.untrack(this);
            if (!leftForSimulation)
            {
                missileCapability.stopLaunchSound();
            }
        }

        super.remove(reason);
    }

    /**
     * Called right before the missile is discarded to fly on in the missile tracker. It hasn't ended, so its launch
     * sound plays out and a delivery missile keeps its cargo.
     */
    public void leaveForSimulation()
    {
        leftForSimulation = true;
    }

    /**
     * @return true if the missile was removed to fly on in the missile tracker, see {@link #leaveForSimulation()}
     */
    public boolean hasLeftForSimulation()
    {
        return leftForSimulation;
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling()
    {
        // Grown on all sides as the model is longer than the hitbox and turns with the flight path
        return this.getBoundingBox().inflate(5);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        double d0 = this.getBoundingBox().getSize() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getViewScale();
        return distance < d0 * d0;
    }

    @Override
    public boolean shouldAlignWithMotion() {
        //Clearing default logic to flight controllers can handle motion
        final IMissileFlightLogic logic = missileCapability.getFlightLogic();
        return logic != null && logic.shouldAlignWithMotion(this);
    }

    @Override
    public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
    {
        //Handle player riding missile
        if (!this.level().isClientSide() && (this.getVehicle() == null || this.getVehicle() == player) && !NeoForge.EVENT_BUS.post(new MissileRideEvent.Start(getMissileCapability(), player)).isCanceled())
        {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public double getMountedYOffset()
    {
        if (getMissileCapability().getFlightLogic() instanceof DeadFlightLogic)
        {
            return getBbHeight() / 10;
        }

        return getBbHeight() / 2 + getDeltaMovement().y;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale)
    {
        return new Vec3(0, getMountedYOffset(), 0);
    }

    @Override
    protected boolean ignoreImpact(HitResult hit)
    {
        // Impacts are the server's call. The client copy only follows the synced position, and impacting it there
        // removed the missile early on screen, ran the warhead's client side and fired the impact events twice.
        if (level().isClientSide())
        {
            return true;
        }
        return NeoForge.EVENT_BUS.post(new MissileEvent.PreImpact(getMissileCapability(), this, hit)).isCanceled();
    }

    @Override
    protected void postImpact(HitResult hit)
    {
        NeoForge.EVENT_BUS.post(new MissileEvent.PostImpact(getMissileCapability(), this, hit));
    }

    @Override
    protected boolean shouldCollideWith(Entity entityHit)
    {
        return super.shouldCollideWith(entityHit) && !isRider(entityHit) && entityHit != shootingEntity;
    }

    public boolean isRider(Entity entity) {
        return entity != null && (entity.getVehicle() == this || isRider(entity.getVehicle()));
    }

    public boolean hasPlayerRiding() {
        return hasPlayerRiding(this);
    }

    public static boolean hasPlayerRiding(Entity entity) {
        // PERF: plain loop, flight logic calls this every tick for every missile
        final List<Entity> passengers = entity.getPassengers();
        for (int i = 0; i < passengers.size(); i++) {
            final Entity e = passengers.get(i);
            if (e instanceof Player || hasPlayerRiding(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Server tick this missile last ran, -1 before its first tick
     */
    public int getLastServerTick()
    {
        return lastServerTick;
    }

    /**
     * Marks the missile as running now, so a missile that was rescued gets a full grace period again
     */
    public void markTicked(int serverTick)
    {
        lastServerTick = serverTick;
    }

    /**
     * Sets the missile off at a position, used when it stopped ticking right above its target
     */
    public void forceImpact(Vec3 location)
    {
        setPos(location.x, location.y, location.z);
        onImpact(new BlockHitResult(location, Direction.UP, BlockPos.containing(location), false));
    }

    @Override
    protected final void onImpact(@Nonnull HitResult impactLocation) {
        if(!hasImpacted) {
            this.hasImpacted = true; //TODO store impact information and move this to projectile
            logImpact(impactLocation.getLocation());
            actionOnImpact(impactLocation);
        }
    }

    protected void actionOnImpact(HitResult impactLocation) {
        this.destroy();
    }

    protected void logImpact(Vec3 impactLocation)
    {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] (%sx, %sy, %sz, %s) impacted at (%s, %s, %s)";
        final String formattedMessage = String.format(formatString,
            this.getId(),
            xi(),
            yi(),
            zi(),
            level().dimension().location(),
            impactLocation.x,
            impactLocation.y,
            impactLocation.z
        );
        CreateICBM.logger().info(formattedMessage);
        ICBMDiagnostics.info("MISSILE_IMPACT_DETAIL id={} type={} ticksInAir={} tickCount={} velocity={} noCulling={} flightLogic={} heldChunks={} dim={}",
            getId(), getClass().getSimpleName(), ticksInAir, tickCount, getDeltaMovement(), noCulling,
            getMissileCapability().getFlightLogic() == null ? "none" : getMissileCapability().getFlightLogic().getClass().getSimpleName(),
            chunkLoader.heldCount(), level().dimension().location());
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        if(ConfigMissile.DAMAGE_LIMIT >= 0) {
            return Math.min(Mth.ceil(velocity * ConfigMissile.DAMAGE_SCALE), ConfigMissile.DAMAGE_LIMIT);
        }
        return Mth.ceil(velocity * ConfigMissile.DAMAGE_SCALE);
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket type) {
        if(id == 1 && buf instanceof RegistryFriendlyByteBuf registryBuf) {
            readSpawnData(registryBuf);
            return true;
        }
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        additionalMissileData.writeNbt(saveData);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final CompoundTag saveData = additionalMissileData.readNbt();
        if (saveData != null)
        {
            SAVE_LOGIC.load(this, saveData);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissile> SAVE_LOGIC = new NbtSaveHandler<EntityMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityMissile, CompoundTag>("missile",
            (missile) -> missile.getMissileCapability().serializeNBT(RegistryHelper.access()),
            (missile, data) -> missile.getMissileCapability().deserializeNBT(RegistryHelper.access(), data)
        ))
        .base();

    public static final PacketCodexEntity<EntityMissile, EntityMissile> PACKET_DESC = (PacketCodexEntity<EntityMissile, EntityMissile>) new PacketCodexEntity<EntityMissile, EntityMissile>(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "missile"), "description")
            .fromServer()
            .nodeNbtCompound(SAVE_LOGIC::save, SAVE_LOGIC::load);

    private static boolean registered = false;

    public static void register()
    {
        if (!registered)
        {
            registered = true;
            PacketCodexReg.register(PACKET_DESC);
        }
    }

    public abstract ItemStack toStack();
}
