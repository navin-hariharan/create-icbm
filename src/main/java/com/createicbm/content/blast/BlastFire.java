package com.createicbm.content.blast;

import com.createicbm.client.ICBMSounds;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.core.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlastFire extends Blast
{
    public BlastFire()
    {
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isClientSide())
        {
            int radius = (int) this.getBlastRadius();
            // Collected first so claim and protection mods can take out what they guard, see filterBlockEdits
            final Map<BlockPos, BlockState> fireEdits = new LinkedHashMap<>();

            for (int x = 0; x < radius; ++x) //TODO replace with edge-raytracer
            {
                for (int y = 0; y < radius; ++y)
                {
                    for (int z = 0; z < radius; ++z)
                    {
                        if (x == 0 || x == radius - 1 || y == 0 || y == radius - 1 || z == 0 || z == radius - 1)
                        {
                            double xStep = x / (radius - 1.0F) * 2.0F - 1.0F;
                            double yStep = y / (radius - 1.0F) * 2.0F - 1.0F;
                            double zStep = z / (radius - 1.0F) * 2.0F - 1.0F;
                            double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);

                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;

                            float energy = radius * (0.7F + world().random.nextFloat() * 0.6F);
                            double posX = location.x();
                            double posY = location.y();
                            double posZ = location.z();

                            for (float stepAmount = 0.3F; energy > 0.0F; energy -= stepAmount * 0.75F)
                            {
                                BlockPos targetPosition = BlockPos.containing(posX, posY, posZ);
                                double distanceFromCenter = location.distance(targetPosition);
                                BlockState blockState = world().getBlockState(targetPosition);
                                Block block = blockState.getBlock();

                                if (!blockState.isAir())
                                {
                                    energy -= (getBlockResistance(targetPosition, blockState) + 0.3F) * stepAmount;
                                }

                                if (energy > 0.0F)
                                {
                                    // Set fire by chance and distance
                                    double chance = radius - (Math.random() * distanceFromCenter);

                                    if (chance > distanceFromCenter * 0.55)
                                    {
                                        boolean canReplace = blockState.canBeReplaced() || blockState.isAir();
                                        final BlockState fireState = BaseFireBlock.getState(world(), targetPosition);

                                        if (canReplace && fireState.canSurvive(world(), targetPosition))
                                        {
                                            fireEdits.put(targetPosition, fireState);
                                        }
                                        else if (block == Blocks.ICE)
                                        {
                                            fireEdits.put(targetPosition, Blocks.AIR.defaultBlockState());
                                        }
                                    }
                                }

                                posX += xStep * stepAmount;
                                posY += yStep * stepAmount;
                                posZ += zStep * stepAmount;
                            }
                        }
                    }
                }
            }

            filterBlockEdits(fireEdits.keySet());
            fireEdits.forEach((pos, state) ->
            {
                if (state.isAir())
                {
                    world.removeBlock(pos, false);
                }
                else
                {
                    world.setBlock(pos, state, 3);
                }
            });
        }

        ICBMSounds.EXPLOSION_FIRE.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D, 4.0F, (1.0F + (world().random.nextFloat() - world().random.nextFloat()) * 0.2F) * 1F, true);
        return true;
    }
}
