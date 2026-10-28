package com.createicbm.content.blast.ender;

import com.createicbm.CreateICBM;
import com.createicbm.api.explosion.IBlastTickable;
import com.createicbm.content.blast.Blast;
import com.createicbm.lib.transform.vector.Pos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BlastEnder extends Blast implements IBlastTickable //TODO handle save/load
{
    /** Reach of the vortex in blocks */
    public static final int RADIUS = 20;

    public int duration = 20 * 8;

    /** Random spot on the surface that everything pulled into the middle is sent to, picked once per blast */
    private Vec3 teleportTarget;

    @Override
    public boolean doExplode(int callCount) //TODO break into smaller methods
    {
        if (this.world().isClientSide())
        {
            // PERF: this scanned the whole cube around the blast every tick (64 000 block lookups at the default
            // size) to find a few dozen spots on the shell. Picks that many random spots on the shell instead.
            final double r = this.getBlastRadius() - ((double) this.callCount / (double) this.duration) * this.getBlastRadius();
            if (r >= 1)
            {
                final int attempts = (int) Math.min(160, 4 * Math.PI * r * r * Math.max(0.001 * r, 0.01));
                final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int i = 0; i < attempts; i++)
                {
                    final double theta = this.world().random.nextDouble() * Math.PI * 2;
                    final double cosPhi = this.world().random.nextDouble() * 2 - 1;
                    final double sinPhi = Math.sqrt(1 - cosPhi * cosPhi);
                    final double distance = r - this.world().random.nextDouble();
                    final double x = location.x() + Math.cos(theta) * sinPhi * distance;
                    final double y = location.y() + cosPhi * distance;
                    final double z = location.z() + Math.sin(theta) * sinPhi * distance;
                    if (!this.world().getBlockState(pos.set(x, y, z)).isAir())
                    {
                        continue;
                    }
                    world.addParticle(ParticleTypes.PORTAL, x, y, z, (x - location.x()) * 0.6, (y - location.y()) * 0.6, (z - location.z()) * 0.6);
                }
            }
        }

        if (this.world() instanceof ServerLevel serverLevel)
        {
            sendDenseParticles(serverLevel);
        }

        int radius = (int) this.getBlastRadius();
        AABB bounds = new AABB(location.x() - radius, location.y() - radius, location.z() - radius, location.x() + radius, location.y() + radius, location.z() + radius);
        List<Entity> allEntities = world().getEntitiesOfClass(Entity.class, bounds);
        boolean explosionCreated = false;

        for (Entity entity : allEntities)
        {
            // Players in the cutscene and the giant endermen are not pulled around
            if (entity != this.controller && !EnderAbduction.isParticipant(entity))
            {

                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();
                double motionX = 0, motionY = 0, motionZ = 0;

                int r = (int) this.getBlastRadius();
                if (xDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                motionX -= (r - xDifference) * Math.abs(xDifference) * 0.0006;

                r = (int) this.getBlastRadius();
                if (entity.getY() > location.y())
                {
                    r = (int) -this.getBlastRadius();
                }
                motionY += (r - yDifference) * Math.abs(yDifference) * 0.0011;

                r = (int) this.getBlastRadius();
                if (zDifference < 0)
                {
                    r = (int) -this.getBlastRadius();
                }

                motionZ -= (r - zDifference) * Math.abs(zDifference) * 0.0006;
                entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
                markVelocityChanged(entity);

                if (new Pos(entity.getX(), entity.getY(), entity.getZ()).distance(location) < 4)
                {
                    if (!explosionCreated && callCount % 5 == 0)
                    {
                        if (world() instanceof ServerLevel serverLevel)
                        {
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY(), entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        explosionCreated = true;
                    }

                    // Where entities go is decided by the server only
                    if (!(this.world() instanceof ServerLevel serverWorld))
                    {
                        continue;
                    }

                    try
                    {
                        // Players are taken by a giant enderman, the cutscene picks its own destination
                        if (entity instanceof ServerPlayer player && EnderAbduction.start(player))
                        {
                            continue;
                        }

                        // Pick a random spot within 150 blocks of the blast the first time something is teleported
                        if (this.teleportTarget == null)
                        {
                            final double centerX = this.controller != null ? this.controller.getX() : location.x();
                            final double centerZ = this.controller != null ? this.controller.getZ() : location.z();
                            final int checkX = this.world().random.nextInt(300) - 150 + (int) centerX;
                            final int checkZ = this.world().random.nextInt(300) - 150 + (int) centerZ;

                            // Uses the surface instead, loading or generating the chunk first so the height is known.
                            final LevelChunk chunk = this.world().getChunk(SectionPos.blockToSectionCoord(checkX), SectionPos.blockToSectionCoord(checkZ));
                            this.teleportTarget = EnderTargets.findStandingSpot(serverWorld, chunk, checkX, checkZ, location.yi(), false);
                        }

                        this.world().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);

                        if (entity instanceof ServerPlayer)
                        {
                            ((ServerPlayer) entity).connection.teleport(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z, entity.getYRot(), entity.getXRot());
                        }
                        else
                        {
                            entity.setPos(this.teleportTarget.x, this.teleportTarget.y, this.teleportTarget.z);
                        }

                    } catch (Exception e)
                    {
                        CreateICBM.logger().error("Failed to teleport entity to the End.", e);
                    }
                }
            }
        }

        this.world().playSound(null, this.location.x(), this.location.y(), this.location.z(), SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 2F, world().random.nextFloat() * 0.4F + 0.8F);

        return this.callCount > this.duration;
    }

    /**
     * Dense portal particles around the vortex for every player that can see it. The client side blast only exists
     * for players close by, so the server sends them: one packet per player and tick, long distance enabled so they
     * show out to the view distance.
     */
    private void sendDenseParticles(ServerLevel level)
    {
        final double reach = level.getServer().getPlayerList().getViewDistance() * 16.0 + getBlastRadius();
        final double reachSq = reach * reach;
        final double r = Math.max(2.0, getBlastRadius() * (1.0 - (double) this.callCount / (double) this.duration));
        final double x = location.x();
        final double y = location.y();
        final double z = location.z();
        for (ServerPlayer player : level.players())
        {
            if (player.distanceToSqr(x, y, z) > reachSq)
            {
                continue;
            }
            // Cloud of the whole vortex, thicker towards the middle, and a dense core pulled inwards
            level.sendParticles(player, ParticleTypes.PORTAL, true, x, y + 1, z, 140, r * 0.55, r * 0.55, r * 0.55, 0.9);
            level.sendParticles(player, ParticleTypes.REVERSE_PORTAL, true, x, y + 1, z, 60, r * 0.3, r * 0.3, r * 0.3, 0.25);
            if (this.callCount % 4 == 0)
            {
                level.sendParticles(player, ParticleTypes.PORTAL, true, x, y + 1, z, 100, 1.2, r * 1.4, 1.2, 1.5);
            }
        }
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        if (!this.world().isClientSide())
        {
            for (int i = 0; i < 8; i++) //TODO check for safe location to spawn
            {
                EnderMan enderman = new EnderMan(EntityType.ENDERMAN, world());
                enderman.setPos(this.location.x(), this.location.y(), this.location.z());
                this.world().addFreshEntity(enderman);
            }
        }
    }

    @Override
    public float getBlastRadius()
    {
        return RADIUS;
    }

    @Override
    public boolean isMovable()
    {
        return true;
    }

    /** Not an explosion, so no distant boom for people who can see it */
    @Override
    protected boolean playsDistantSound()
    {
        return false;
    }
}
