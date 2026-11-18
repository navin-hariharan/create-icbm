package com.createicbm.content.missile.logic;

import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.content.missile.entity.explosive.EntityExplosiveMissile;
import com.createicbm.content.missile.logic.targeting.BallisticTargetingData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TargetRangeDet {
    /** Blocks under a target that are checked for ground. A target with nothing but open air there is in the air. */
    private static final int AIR_TARGET_CLEARANCE = 4;

    private final EntityExplosiveMissile missile;

    public TargetRangeDet(EntityExplosiveMissile missile) {
        this.missile = missile;
    }

    public void update() {

        if(missile.level().isClientSide() || missile.getMissileCapability().getTargetData() == null) {
            return;
        }

        double offset = 0;
        if (missile.getMissileCapability().getTargetData() instanceof BallisticTargetingData) {
            offset = ((BallisticTargetingData) missile.getMissileCapability().getTargetData() ).getImpactHeightOffset();
        }
        // PERF: CLUSTER is not registered (null) and getExplosiveData never returns null, skip the item cap lookup per tick
        else if(ICBMExplosives.CLUSTER != null && missile.explosive.getExplosiveData() == ICBMExplosives.CLUSTER) {
            offset = 10; //TODO handle as fuse settings
        }

        if(offset > 0)
        {
            final IMissileTarget target = missile.getMissileCapability().getTargetData();
            double deltaX = target.getX() - missile.getX();
            double deltaY = target.getY() - missile.getY();
            double deltaZ = target.getZ() - missile.getZ();

            //Validate we are near flat distance of the target
            if (inRange(offset, deltaX) && inRange(offset, deltaZ)) // TODO account for next tick motion
            {
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                if(distance <= offset && !isInAir(target))
                {
                    missile.detonate(missile.getX(), missile.getY(), missile.getZ()); //TODO make lambda driven, so we can reuse fuses on other entities
                    missile.discard();
                }
            }
        }
    }

    /**
     * The fuse only sets a missile off just above a target on the ground. A target in open air, such as the spot a
     * flying Create Aeronautics ship fired from (Advanced Radar counter-strike), gets no fuse: the missile flies on
     * and goes off on whatever it hits. That is the ship if it is still there (ship blocks are not in the world's
     * block grid, so a ship doesn't count as ground here), otherwise whatever lies below.
     */
    private boolean isInAir(IMissileTarget target)
    {
        final Level level = missile.level();
        final BlockPos at = BlockPos.containing(target.getX(), target.getY(), target.getZ());
        for (int down = 0; down <= AIR_TARGET_CLEARANCE; down++)
        {
            final BlockPos pos = at.below(down);
            // Unloaded counts as ground, so the fuse works as it always did
            if (!level.isLoaded(pos))
            {
                return false;
            }
            final BlockState state = level.getBlockState(pos);
            // Water counts as ground too: a strike at a boat goes off at the surface, not on the sea bed
            if (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    private boolean inRange(double range, double value)
    {
        return value <= range && value >= -range;
    }
}
