package com.createicbm.content.blast.ender;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Finds a place to stand in a loaded chunk, shared by the instant teleport and the abduction cutscene
 */
public final class EnderTargets
{
    private EnderTargets()
    {
    }

    /**
     * @param chunk   the loaded chunk that holds x and z
     * @param startY  where to start looking down from in a dimension with a roof (the nether)
     * @param strict  true to refuse lava and the void, false to take whatever there is
     * @return the spot to stand (block corner, centered on x and z) or null when strict and the spot is unsafe
     */
    @Nullable
    public static Vec3 findStandingSpot(ServerLevel level, LevelChunk chunk, int x, int z, int startY, boolean strict)
    {
        int topY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1;

        // Surface of a level with a roof (nether) is the roof, look for a two block gap below the start height instead
        if (level.dimensionType().hasCeiling())
        {
            final int minY = level.getMinBuildHeight();
            final int fromY = Math.min(startY, minY + level.dimensionType().logicalHeight() - 2);
            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, fromY, z);
            boolean found = false;
            for (; pos.getY() > minY; pos.move(Direction.DOWN))
            {
                final BlockPos below = pos.below();
                if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(pos.above()).isAir()
                    && chunk.getBlockState(below).isFaceSturdy(chunk, below, Direction.UP))
                {
                    topY = pos.getY();
                    found = true;
                    break;
                }
            }
            if (!found && strict)
            {
                return null;
            }
        }

        if (strict)
        {
            final BlockPos below = new BlockPos(x, topY - 1, z);
            if (topY - 1 <= level.getMinBuildHeight() || chunk.getBlockState(below).getFluidState().is(FluidTags.LAVA))
            {
                return null;
            }
        }
        return new Vec3(x + 0.5, topY, z + 0.5);
    }
}
