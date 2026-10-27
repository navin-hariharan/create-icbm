package com.createicbm.content.blast;

import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseFireBlock;
import com.createicbm.content.blast.helpers.BlastBlockHelpers;

import java.util.List;
import java.util.Set;

public class BlastExothermic extends BlastBeam
{
    public BlastExothermic()
    {
        this.red = 0.7f;
        this.green = 0.3f;
        this.blue = 0;
    }

    @Override
    protected void mutateBlocks(List<BlockPos> edits)
    {
        final double radius = this.getBlastRadius();
        final double radiusDecay = Math.max(1, radius * 0.3); //TODO config
        final double radiusEnsured = Math.max(1, radius * 0.1); //TODO config
        final Set<BlockPos> denied = deniedNeighbourEdits(edits, Direction.UP, Direction.DOWN);
        for (BlockPos targetPosition : edits)
        {
            final double delta_x = location.xi() - targetPosition.getX();
            final double delta_y = location.yi() - targetPosition.getY();
            final double delta_z = location.zi() - targetPosition.getZ();

            final double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);
            final double distanceScale = 1 - (distance / radius);

            BlockState blockState = world.getBlockState(targetPosition);
            Block block = blockState.getBlock();

            //Turn fluids and liquid like blocks to air
            if (BlastBlockHelpers.isWaterMaterial(blockState) || block == Blocks.ICE)
            {
                this.world().removeBlock(targetPosition, false);
            }

            //Closer to center the better the chance of spawning blocks
            if (distance <= radiusDecay || Math.random() < distanceScale)
            {
                //Destroy plants
                if (BlastBlockHelpers.isPlantMaterial(blockState))
                {
                    if (!blockState.canBeReplaced() || canPlaceFire(world(), targetPosition))
                    {
                        this.world().removeBlock(targetPosition, false);
                    }
                    else
                    {
                        this.world().setBlock(targetPosition, BaseFireBlock.getState(world(), targetPosition), 3);
                    }
                }

                //Turn random stone into lava
                else if (BlastBlockHelpers.isRockMaterial(blockState))
                {
                    //Small chance to turn to lava
                    if (this.world().random.nextFloat() > 0.9) //TODO add config
                    {
                        this.world().setBlock(targetPosition, Blocks.LAVA.defaultBlockState(), 3);
                    }
                    //Coin flip to turn to magma
                    else if (this.world().random.nextBoolean()) //TODO add config
                    {
                        if (!denied.contains(targetPosition.below()))
                        {
                            this.world().setBlock(targetPosition.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
                        }
                    }
                    //Coin flip to turn to netherrack
                    else if (this.world().random.nextBoolean() || distance <= radiusEnsured) //TODO add config
                    {
                        placeNetherrack(world, targetPosition, denied);
                    }
                }

                //Sand replacement
                else if (BlastBlockHelpers.isSandMaterial(blockState))
                {
                    if (this.world().random.nextBoolean()) //TODO add config
                    {
                        if (!denied.contains(targetPosition.below()))
                        {
                            this.world().setBlock(targetPosition.below(), Blocks.SOUL_SAND.defaultBlockState(), 3);
                        }
                    }
                    else
                    {
                        placeNetherrack(world, targetPosition, denied);
                    }
                }

                //Ground replacement
                else if (BlastBlockHelpers.isGroundMaterial(blockState))
                {
                    placeNetherrack(world, targetPosition, denied);
                }

                //Randomly place fire TODO move to outside mutate so we always place fire while charging up
                if (Math.random() < distanceScale)
                {
                    tryPlaceFire(world, targetPosition.above(), false, denied);
                }
            }
        }
    }

    private static void placeNetherrack(Level world, BlockPos pos, Set<BlockPos> denied)
    {
        // A refused placement (protected or unchanged block) is normal, nothing to report
        world.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), 3);

        //Place fire randomly above netherrack
        tryPlaceFire(world, pos.above(), true, denied);
    }

    private static void tryPlaceFire(Level world, BlockPos pos, boolean random, Set<BlockPos> denied)
    {
        if (!denied.contains(pos) && (!random || world.random.nextBoolean()))
        {
            //Place fire
            final BlockState blockState = world.getBlockState(pos);
            if (blockState.canBeReplaced() && canPlaceFire(world, pos))
            {
                world.setBlock(pos, BaseFireBlock.getState(world, pos), 3);
            }
        }
    }

    private static boolean canPlaceFire(Level world, BlockPos pos)
    {
        return BaseFireBlock.getState(world, pos).canSurvive(world, pos);
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        //Change time of day
        if (ConfigBlast.ALLOW_DAY_NIGHT && world().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) && world() instanceof ServerLevel serverLevel)
        {
            serverLevel.setDayTime(18_000);
        }
    }
}
