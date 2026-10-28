package com.createicbm.content.blocks.launcher.screen;

import com.mojang.serialization.MapCodec;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.caps.IGPSData;
import com.createicbm.content.blocks.launcher.network.ILauncherComponent;
import com.createicbm.content.blocks.launcher.network.LauncherNetwork;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.capability.gps.GPSDataHelpers;
import com.createicbm.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockLaunchScreen extends BlockICBM
{
    public static final MapCodec<BlockLaunchScreen> CODEC = simpleCodec(p -> new BlockLaunchScreen());

    public BlockLaunchScreen()
    {
        super("launcherscreen");
        // The screen has no slots of its own (the battery slot went with Forge Energy), and the item handler it
        //  exposes is the network's, so it must not drop that
        this.dropInventory = false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide())
        {
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof TileLauncherScreen)
            {
                final TileLauncherScreen screen = (TileLauncherScreen) tileEntity;
                final IGPSData gpsData = CreateICBMHelpers.getGPSData(stack);
                if (GPSDataHelpers.handlePlayerInteraction(gpsData, player, screen::setTarget))
                {
                    return ItemInteractionResult.SUCCESS;
                }
                else if(stack.getItem() == Items.STONE_AXE) {
                    final LauncherNetwork network = screen.getNetworkNode().getNetwork();
                    player.sendSystemMessage(Component.literal("Network: " + network));
                    player.sendSystemMessage(Component.literal("L: " + (network != null ? network.getLaunchers().size() : 0)));
                }
                else
                {
                    screen.openGui(player);
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherScreen(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_LAUNCHER_SCREEN.get(), (l, p, s, tile) -> tile.update());
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ILauncherComponent)
            {
                ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
