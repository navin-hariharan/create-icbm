package com.createicbm.content.blast.threaded;

import com.createicbm.CreateICBM;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.blast.BlastMutation;
import com.createicbm.content.blast.BlastRadioactiveBlockSwaps;
import com.createicbm.lib.network.packet.PacketMushroomCloud;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.lib.transform.vector.Location;
import com.createicbm.lib.transform.vector.Pos;
import net.minecraft.world.level.block.LiquidBlock;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BlastNuclear extends BlastThreaded {
    /** Players further away than this do not get the smoke cloud particles */
    private static final double CLOUD_VIEW_DISTANCE = 256;

    /** Ticks the mushroom cloud takes to rise, a little quicker than the antimatter one */
    private static final int CLOUD_TICKS = 20 * 14;

    /** Players this close hear the blast itself at full volume, the others in view distance a distant boom */
    private static final double NEAR_SOUND_RADIUS = 50;

    private double energy;

    public BlastNuclear() {
    }

    public BlastNuclear setEnergy(double energy) {
        this.energy = energy;
        return this;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        //How many steps to go per rotation
        final int steps = (int) Math.ceil(Math.PI * this.getBlastRadius());

        double x;
        double y;
        double z;

        double dx;
        double dy;
        double dz;

        double power;

        double yaw;
        double pitch;

        final int lineDensityScale = 2;
        for (int yawSlices = 0; yawSlices < lineDensityScale * steps; yawSlices++) {
            for (int pitchSlice = 0; pitchSlice < steps; pitchSlice++) {
                //Calculate power
                power = this.energy - (this.energy * ThreadLocalRandom.current().nextFloat() / 2);

                //Get angles for rotation steps
                yaw = (Math.PI / steps) * yawSlices;
                pitch = (Math.PI / steps) * pitchSlice;

                //Figure out vector to move for trace (cut in half to improve trace skipping blocks)
                dx = sin(pitch) * cos(yaw) * 0.5;
                dy = cos(pitch) * 0.5;
                dz = sin(pitch) * sin(yaw) * 0.5;

                //Reset position to current
                x = this.x();
                y = this.y();
                z = this.z();

                BlockPos prevPos = null;

                //Trace from start to end
                while (location.distance(x, y, z) <= this.getBlastRadius() && power > 0) //TODO replace distance check with SQ version
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 5; //TODO why the magic numbers?

                    //Convert double position to int position as block pos
                    final BlockPos blockPos = BlockPos.containing(x, y, z);

                    //Only do action one time per block (not a perfect solution, but solves double hit on the same block in the same line)
                    if (!Objects.equals(prevPos, blockPos)) {
                        if (!isLoadedThreaded(blockPos)) //TODO: find better fix for non main thread loading
                            continue;

                        //Get block state and block from position
                        final BlockState state = getBlockStateThreaded(blockPos);

                        //Ignore air blocks
                        if (!state.isAir()) {
                            //Consume power based on block
                            power -= getResistance(blockPos, state);

                            //Only break block that can be broken && If we still have power. Then break the block
                            if (state.getDestroySpeed(world, blockPos) >= 0 && power > 0f) {
                                edits.accept(blockPos);
                            }
                        }
                    }

                    //Note previous block
                    prevPos = blockPos;

                    //Move forward
                    x += dx;
                    y += dy;
                    z += dz;
                }
            }
        }
        return false;
    }

    public float getResistance(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LiquidBlock) {
            return 0.25f;
        } else {
            return getBlockResistance(pos, state);
        }
    }

    @Override
    protected double getNearSoundRadius() {
        return NEAR_SOUND_RADIUS;
    }

    @Override
    protected ICBMSounds getNearSound() {
        return ICBMSounds.EXPLOSION;
    }

    @Override
    protected void playExplodeSound() {
        // Played by BlastSounds, to everybody in range and with the delay of distance
    }

    @Override
    public boolean setupBlast() {
        super.setupBlast();
        ICBMDiagnostics.info("NUCLEAR_SETUP_BEGIN dim={} center=({}, {}, {}) radius={} energy={} mushroomCloudEnabled={} thread={}",
            world() == null ? "unknown" : world().dimension().location(), x(), y(), z(), getBlastRadius(), energy,
            ConfigBlast.nuclear.mushroomCloud, Thread.currentThread().getName());
        if (this.world() != null) {
            if (ConfigBlast.nuclear.mushroomCloud && world instanceof ServerLevel level) {
                // Same cloud as the antimatter blast, a bit smaller, every client draws its own from this one packet
                final double radius = this.getBlastRadius() * ConfigBlast.nuclear.mushroomCloudScale;
                PacketMushroomCloud.send(level, location.x(), location.y(), location.z(), (float) radius, CLOUD_TICKS, ConfigBlast.nuclear.mushroomCloudRange);
            } else {
                // Players close enough to see the cloud, collected once as the cloud is a few thousand particles
                final List<ServerPlayer> viewers = getCloudViewers();

                // Spawn nuclear cloud.
                for (int y = 0; y < 26; y++) {
                    int r = 4;

                    if (y < 8) {
                        r = Math.max(Math.min((8 - y) * 2, 10), 4);
                    } else if (y > 15) {
                        r = Math.max(Math.min((y - 15) * 2, 15), 5);
                    }

                    for (int x = -r; x < r; x++) {
                        for (int z = -r; z < r; z++) {
                            double distance = Math.sqrt(x * x + z * z);

                            if (r > distance && r - 3 < distance) {
                                Location spawnPosition = location.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                                float xDiff = (float) (spawnPosition.x() - location.x());
                                float zDiff = (float) (spawnPosition.z() - location.z());
                                // Sent to clients instead, count 0 makes the offsets the particle velocity.
                                sendCloudParticle(viewers, spawnPosition.x(), spawnPosition.y(), spawnPosition.z(),
                                    xDiff * 0.3 * world().random.nextFloat(), -world().random.nextFloat(), zDiff * 0.3 * world().random.nextFloat()); //(float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, //0, 8F, 1.2F);
                            }
                        }
                    }
                }
            }

            this.doDamageEntities((float)ConfigBlast.nuclear.entityDamageScale, (float) (this.energy * ConfigBlast.nuclear.entityDamageMultiplier));

        }

        return true;
    }

    private List<ServerPlayer> getCloudViewers()
    {
        if (world instanceof ServerLevel serverLevel)
        {
            return serverLevel.players().stream()
                .filter(player -> player.distanceToSqr(location.x(), location.y(), location.z()) <= CLOUD_VIEW_DISTANCE * CLOUD_VIEW_DISTANCE)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Sends one smoke particle of the cloud to the viewers. Count 0 makes the client use the offsets as velocity.
     */
    private void sendCloudParticle(List<ServerPlayer> viewers, double x, double y, double z, double velX, double velY, double velZ)
    {
        for (ServerPlayer player : viewers)
        {
            // Long distance, the cloud is meant to be watched from far away
            ((ServerLevel) world).sendParticles(player, ParticleTypes.SMOKE, true, x, y, z, 0, velX, velY, velZ, 1.0);
        }
    }

    @Override
    public boolean doExplode(int callCount) {
        super.doExplode(callCount);
        int r = this.callCount;

        if (this.world().isClientSide()) {
            for (int x = -r; x < r; x++) {
                for (int z = -r; z < r; z++) {
                    double distance = Math.sqrt(x * x + z * z);

                    if (distance < r && distance > r - 1) {
                        Location targetPosition = this.location.add(new Pos(x, 0, z));

                        if (this.world().random.nextFloat() < Math.max(0.001 * r, 0.05)) {
                            world.addParticle(ParticleTypes.SMOKE, targetPosition.x(), targetPosition.y(), targetPosition.z(), 0, 0, 0); //5F, 1F);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onBlastCompleted() {
        ICBMDiagnostics.info("NUCLEAR_PRIMARY_BLAST_COMPUTE_COMPLETE dim={} center=({}, {}, {}) radius={} energy={} thread={}",
            world() == null ? "unknown" : world().dimension().location(), x(), y(), z(), getBlastRadius(), energy, Thread.currentThread().getName());
        super.onBlastCompleted();
        if (world() != null && !world().isClientSide()) {
            try {
                //Attack entities with concussion wave
                this.doDamageEntities((float)ConfigBlast.nuclear.entityDamageScale, (float) (this.energy * ConfigBlast.nuclear.entityDamageMultiplier));

                //Place radioactive blocks
                final double radiationScale = ConfigBlast.nuclear.rotScale;
                ICBMDiagnostics.info("NUCLEAR_RADIOACTIVE_SWAP_BEGIN dim={} center=({}, {}, {}) scale={} thread={}",
                    world().dimension().location(), location.x(), location.y(), location.z(), radiationScale, Thread.currentThread().getName());
                final long radioactiveSwapStartNanos = System.nanoTime();
                final IActionStatus radioactiveSwapStatus = new BlastRadioactiveBlockSwaps()
                    .setBlastWorld(world())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(location.x(), location.y(), location.z())
                    .setBlastSize((float) radiationScale)
                    .setExplosiveData(ICBMExplosives.ROT)
                    .buildBlast().doAction();
                ICBMDiagnostics.info("NUCLEAR_RADIOACTIVE_SWAP_DISPATCHED dim={} center=({}, {}, {}) scale={} elapsedMs={} status={} thread={}",
                    world().dimension().location(), location.x(), location.y(), location.z(), radiationScale,
                    (System.nanoTime() - radioactiveSwapStartNanos) / 1_000_000.0, radioactiveSwapStatus, Thread.currentThread().getName());

                new BlastMutation()
                    .setBlastWorld(world())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(location.x(), location.y(), location.z())
                    .setBlastSize((float)ConfigBlast.nuclear.mutationScale)
                    .setExplosiveData(ICBMExplosives.MUTATION)
                    .buildBlast().doAction();

                // TODO trigger blast wave that hits all entities with radiation damage not behind protection (basically line of sight)
                // TODO spawn a radioactive gas cloud, can recycle the gas blast and set it to expand quickly
                // TODO throw projectiles containing radioactive material. On impact have the projectiles place radioactive dust in a nearby area shotgun pattern
                // TODO have radioactive dust fall from sky in a radius around the blast

                //Play audio
                ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 10.0F, (1.0F + (this.world().random.nextFloat() - this.world().random.nextFloat()) * 0.2F) * 0.7F, true);

            } catch (Exception e) {
                String msg = String.format("BlastNuclear#doPostExplode() ->  Unexpected error while running post detonation code " +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = %s",
                    world, getThread(), size, location);
                CreateICBM.logger().error(msg, e);
            }
        }
    }

    @Override
    protected float getShipDamageRadius()
    {
        return getBlastRadius();
    }
}
