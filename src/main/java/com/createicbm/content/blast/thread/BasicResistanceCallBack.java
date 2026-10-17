package com.createicbm.content.blast.thread;

import com.createicbm.jlib.data.vector.IPos3D;
import com.createicbm.content.blast.Blast;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Deprecated
public class BasicResistanceCallBack implements IThreadCallBack
{
    public final Blast blast;

    public BasicResistanceCallBack(Blast blast)
    {
        this.blast = blast;
    }

    @Override
    public float getResistance(Level world, IPos3D blastCenter, BlockPos pos, Entity source, BlockState state)
    {
        if (state.getBlock() instanceof LiquidBlock)
        {
            return 0.25f;
        }
        else
        {
            return blast.getBlockResistance(pos, state);
        }
    }
}
