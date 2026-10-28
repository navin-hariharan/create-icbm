package com.createicbm.content.blast.threaded;

import com.createicbm.CreateICBM;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.ConfigDebug;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.blast.types.ConfigAntimatter;
import com.createicbm.content.blast.BlastHelpers;
import com.createicbm.content.blast.redmatter.EntityRedmatter;
import com.createicbm.lib.network.packet.PacketMushroomCloud;
import com.createicbm.lib.network.packet.PacketScreenFlash;
import com.createicbm.lib.transform.BlockEditHandler;
import com.createicbm.lib.transform.DistanceCountingSort;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.createicbm.content.blast.helpers.BlastBlockHelpers;
import java.util.concurrent.ThreadLocalRandom;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlastAntimatter extends BlastThreaded
{
    /** Ticks the mushroom cloud takes to rise */
    private static final int CLOUD_TICKS = 20 * 18;

    /** Players this close hear the detonation at full volume, the others in view distance a distant boom */
    private static final double NEAR_SOUND_RADIUS = 100;

    private final BlockState replaceState = Blocks.AIR.defaultBlockState();

    @Override
    protected double getNearSoundRadius()
    {
        return NEAR_SOUND_RADIUS;
    }

    /** The detonation is timed to the flash and cloud, it builds up to the shockwave a second in */
    @Override
    protected ICBMSounds getNearSound()
    {
        return ICBMSounds.ANTIMATTER_BLAST;
    }

    @Override
    protected void playExplodeSound()
    {
        // Played by BlastSounds together with the flash, to everybody in range and with the delay of distance
    }

    @Override
    public boolean setupBlast()
    {
        super.setupBlast();
        final boolean continueBlast = this.doDamageEntities(this.getBlastRadius() * 2, ConfigBlast.antimatter.damage); //TODO config for radius
        if (continueBlast)
        {
            announce();
        }
        return continueBlast;
    }

    /**
     * The flash and the mushroom cloud, only small packets are sent, every client draws its own cloud
     */
    private void announce()
    {
        if (!(world instanceof ServerLevel level))
        {
            return;
        }
        final ConfigAntimatter config = ConfigBlast.antimatter;
        final double x = this.location.x();
        final double y = this.location.y();
        final double z = this.location.z();
        final double radius = this.getBlastRadius();

        // Blinding flash for everybody within the blast and the extra range around it, shorter the further away
        if (config.flashExtraRange > 0)
        {
            final double range = radius + config.flashExtraRange;
            final double rangeSq = range * range;
            for (ServerPlayer player : level.players())
            {
                final double distanceSq = player.distanceToSqr(x, y, z);
                if (distanceSq <= rangeSq)
                {
                    final double closeness = 1.0 - Math.sqrt(distanceSq) / range;
                    final int fullTicks = Math.max(8, (int) (config.flashTicks * (0.25 + 0.75 * closeness)));
                    final int fadeTicks = 30 + (int) (60 * closeness);
                    PacketScreenFlash.send(player, fullTicks, fadeTicks);
                }
            }
            level.sendParticles(ParticleTypes.FLASH, x, y, z, 3, 1.5, 1.5, 1.5, 0);
        }

        if (config.mushroomCloud)
        {
            PacketMushroomCloud.send(level, x, y, z, (float) radius, CLOUD_TICKS, Math.max(config.mushroomCloudRange, radius + config.flashExtraRange));
        }
    }

    @Override
    public void destroyBlock(BlockPos blockPos)
    {
        if (willEditBlock(blockPos, world.getBlockState(blockPos)))
        {
            world.setBlock(blockPos, replaceState, ConfigBlast.BLAST_DO_BLOCKUPDATES ? 3 : 2);
        }
    }

    @Override
    protected boolean willEditBlock(BlockPos pos, BlockState state)
    {
        return !state.isAir() && (ConfigBlast.antimatter.damageUnbreakable || state.getDestroySpeed(world, pos) >= 0);
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        long time = System.currentTimeMillis();
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) -> {
            if (shouldEditPos(x, y, z))
            {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            CreateICBM.logger().info("Antimatter: thread calculations ran in " + time + "ms");
        }
        return false;
    }

    @Override
    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        //TODO have threads take a copy of chunks for use before running
        //      OR replace thread system with an entity that ticks over time to collect data
        if (world instanceof ServerLevel)
        {
            long time = System.currentTimeMillis();

            //Sort distance, the middle first so the crater opens up from the center
            sortFromCenter(edits);

            time = System.currentTimeMillis() - time;
            if(ConfigDebug.DEBUG_EXPLOSIVES) {
                CreateICBM.logger().info("Antimatter: sorting blocks ran in " + time + "ms");
            }

            time = System.currentTimeMillis();

            final List<BlockPos> removeFirst = edits.stream()
                    .filter(this::isLoadedThreaded)
                    .filter(this::isFluid)
                    .collect(Collectors.toList());

            time = System.currentTimeMillis() - time;
            if(ConfigDebug.DEBUG_EXPLOSIVES) {
                CreateICBM.logger().info("Antimatter: filtering first edits ran in " + time + "ms");
            }

            //Schedule edits to run in the world
            scheduleOnServerThread(() -> scheduledTask(removeFirst, edits));
        }
    }

    /**
     * Orders the positions from the middle of the blast outwards. Squared distances only come in a few thousand
     * values, so they are counted into buckets instead of compared 700 000 times over.
     */
    private void sortFromCenter(List<BlockPos> edits)
    {
        final int count = edits.size();
        final BlockPos[] positions = edits.toArray(new BlockPos[0]);
        final int centerX = this.xi();
        final int centerY = this.yi();
        final int centerZ = this.zi();
        final int[] keys = new int[count];
        for (int i = 0; i < count; i++)
        {
            final long dx = positions[i].getX() - centerX;
            final long dy = positions[i].getY() - centerY;
            final long dz = positions[i].getZ() - centerZ;
            final long distanceSq = dx * dx + dy * dy + dz * dz;
            keys[i] = distanceSq > DistanceCountingSort.MAX_KEY ? -1 : (int) distanceSq;
        }
        final int[] order = DistanceCountingSort.order(keys);
        if (order == null)
        {
            edits.sort(buildSorter());
            return;
        }
        for (int i = 0; i < count; i++)
        {
            edits.set(i, positions[order[i]]);
        }
    }

    private void scheduledTask(List<BlockPos> removeFirst, List<BlockPos> edits) {

        //Worker is done reading
        releaseChunkSnapshot();

        long time = System.currentTimeMillis();

        // Water and falling blocks go first so they cannot flow or drop into the crater while it opens. Spread over
        // a quarter of the time, in one tick this was every water block of an ocean at once. Each batch is checked
        // against claims when applied.
        final int paceTicks = ConfigBlast.antimatter.editTicks;
        if (!removeFirst.isEmpty())
        {
            BlockEditHandler.queueBatches(world, removeFirst, this::applyEdits, null, paceTicks / 4);
        }

        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            CreateICBM.logger().info("Antimatter: queueing first edits ran in " + time + "ms,  edits:" + removeFirst.size());
        }

        time = System.currentTimeMillis();

        //Queue the rest, even the ones from the previous, spread over time so the crater opens up smoothly
        BlockEditHandler.queueBatches(world, edits, this::applyEdits, null, paceTicks);

        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            CreateICBM.logger().info("Antimatter: remove#second ran in " + time + "ms,  edits:" + edits.size());
        }

        //Notify blast we have entered world again
        onPostThreadJoinWorld();
    }

    /** Called from the worker thread */
    protected boolean isFluid(BlockPos blockPos)
    {
        BlockState state = getBlockStateThreaded(blockPos);
        return BlastBlockHelpers.isWaterMaterial(state) || state.getBlock() instanceof FallingBlock;
    }

    protected boolean shouldEditPos(int x, int y, int z)
    {
        final double distSQ = x * x + y * y + z * z;
        final double blastSQ = this.getBlastRadius() * this.getBlastRadius();

        final int featherEdge = (int) Math.floor(blastSQ * 0.05f);
        final int delta = (int) Math.floor(blastSQ - distSQ);

        if (delta < featherEdge)
        {
            final double p2 = 1 - (delta / (double) featherEdge);
            return ThreadLocalRandom.current().nextFloat() > p2;
        }
        return true;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        this.doDamageEntities(this.getBlastRadius() * 2, ConfigBlast.antimatter.damage); //TODO config for radius
    }

    @Override
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem) {

        if (!ConfigBlast.antimatter.damageOnRedmatterKill)
        {
            final List<Entity> allEntities = getEntities(radius * 2); //TODO config for radius
            final long killed = allEntities.stream().filter(e -> e instanceof EntityRedmatter).peek(this::onDamageEntity).count();
            if(killed > 0) {
                return false;
            }
            return this.doDamageEntities(allEntities, radius, power, destroyItem);
        }
        return super.doDamageEntities(radius, power, destroyItem);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityRedmatter)
        {
            //TODO implement a proper event instead of BlastCancel
            entity.discard();
            return true;
        }

        return false;
    }

    @Override
    protected float getShipDamageRadius()
    {
        return getBlastRadius();
    }
}
