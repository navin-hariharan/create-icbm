package com.createicbm.prefab.tile;

import com.createicbm.lib.tile.ITick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/9/2017.
 */
public class TileMachine extends BlockEntity
{
    protected int ticks = -1;

    protected final List<ITick> tickActions = new ArrayList<>();

    public TileMachine(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    /** Called each tick by the block entity ticker */
    public void update()
    {
        //Increase tick
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1)
        {
            ticks = 0;
        }

        // PERF: plain loop, the forEach lambda captured this and was allocated every tick for every machine
        final boolean server = isServer();
        for (int i = 0; i < tickActions.size(); i++)
        {
            tickActions.get(i).update(ticks, server);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata(registries);
    }

    public boolean isServer()
    {
        return level != null && !level.isClientSide();
    }

    public boolean isClient()
    {
        return level != null && level.isClientSide();
    }

    @Deprecated
    public Direction getRotation()
    {
        final BlockState state = getBlockState();
        if (state.hasProperty(BlockICBM.ROTATION_PROP))
        {
            return state.getValue(BlockICBM.ROTATION_PROP);
        }
        return Direction.NORTH;
    }

    public double getDistanceSq(double x, double y, double z)
    {
        final double dx = getBlockPos().getX() + 0.5D - x;
        final double dy = getBlockPos().getY() + 0.5D - y;
        final double dz = getBlockPos().getZ() + 0.5D - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
