package com.createicbm.content.blast;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.api.explosion.IBlastTickable;
import com.createicbm.content.blast.thread.ThreadSmallExplosion;
import com.createicbm.content.blast.threaded.BlastThreaded;
import com.createicbm.content.entity.flyingblock.EntityFlyingBlock;
import com.createicbm.content.entity.flyingblock.FlyingBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.function.Consumer;

public class BlastAntiGravitational extends BlastThreaded implements IBlastTickable {

    public static final int RUNNING_TICKS = ICBMConstants.TICKS_MIN * 2;
    protected ThreadSmallExplosion thread;
    protected Set<EntityFlyingBlock> flyingBlocks = new HashSet<EntityFlyingBlock>();
    List<BlockPos> results;

    int searchIndex = 0;

    @Override
    public boolean setupBlast() {
        if (!this.world().isClientSide()) {
            this.thread = new ThreadSmallExplosion(this, (int) this.getBlastRadius(), this.exploder);
            this.thread.start();
        }

        return true;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) -> {
            edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
        });
        return false;
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (world() != null && !this.world().isClientSide()) {
            if (this.thread != null)
            {
                if (this.thread.isComplete) {
                    if (results == null) {
                        results = new ArrayList(getThreadResults());
                        filterBlockEdits(results);
                        Collections.shuffle(results);
                        results.sort(Comparator.comparingInt(r -> -r.getY()));
                        this.threadResults.clear();
                    }

                    if(searchIndex >= results.size()) {
                        searchIndex = 0;
                    }

                    // Search until we find a single block
                    for (; searchIndex < results.size(); searchIndex++) {
                        final BlockPos targetPosition = results.get(searchIndex);

                        if (FlyingBlock.spawnFlyingBlock(world, targetPosition, (entity) -> {
                            entity.yawChange = 50 * world().random.nextFloat();
                            entity.pitchChange = 100 * world().random.nextFloat();
                            entity.setDeltaMovement(entity.getDeltaMovement().add(0, Math.max(1 * world().random.nextFloat(), 1), 0));

                            double deltaX = targetPosition.getX() - this.location.getX();
                            double deltaZ = targetPosition.getZ() - this.location.getZ();
                            double mag = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                            if (mag > 1.0E-6) {
                                deltaX /= mag;
                                deltaZ /= mag;
                            } else {
                                // Straight above or below the center, there is no horizontal direction
                                deltaX = 0;
                                deltaZ = 0;
                            }

                            entity.setDeltaMovement(entity.getDeltaMovement().add(deltaX * (1 - world().random.nextFloat()), 0, deltaZ * (1 - world().random.nextFloat())));

                            entity.setGravity(0);
                            entity.setInAirKillTime(RUNNING_TICKS * 2);
                        }, entityFlyingBlock -> {
                            flyingBlocks.add(entityFlyingBlock);
                        })) {
                           break;
                        }
                    }
                }
            } else {
                String msg = String.format("BlastAntiGravitational#doPostExplode() -> Failed to run due to null thread" +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = ",
                    world, thread, size, location);
                CreateICBM.logger().error(msg);
            }
        }

        int radius = (int) this.getBlastRadius();
        final int affectHeight = Math.max(radius, 100); //TODO config affect height
        AABB bounds = new AABB(location.x() - radius, location.y() - radius, location.z() - radius, location.x() + radius, location.y() + affectHeight, location.z() + radius);
        List<Entity> allEntities = world().getEntitiesOfClass(Entity.class, bounds);

        for (Entity entity : allEntities) {
            if (entity.getY() < affectHeight + location.y()) {
                if (entity.getDeltaMovement().y < 0.4) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.15, 0));
                    markVelocityChanged(entity);
                }
            }
        }

        return this.callCount > RUNNING_TICKS;
    }

    @Override
    protected void onBlastCompleted() {
        flyingBlocks.forEach(EntityFlyingBlock::restoreGravity);
    }

    /** Not an explosion, so no distant boom for people who can see it */
    @Override
    protected boolean playsDistantSound()
    {
        return false;
    }
}
