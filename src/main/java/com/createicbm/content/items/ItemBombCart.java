package com.createicbm.content.items;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.content.entity.EntityBombCart;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.capability.ex.CapabilityExplosiveStack;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.prefab.item.ISubItemProvider;
import com.createicbm.prefab.item.ItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBombCart extends ItemBase implements ICapabilityHolderItem, ISubItemProvider
{
    public ItemBombCart()
    {
        super(new Item.Properties().stacksTo(3));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == CreateICBMAPI.EXPLOSIVE_ITEM_CAPABILITY)
        {
            return (T) new CapabilityExplosiveStack(stack);
        }
        return null;
    }

    /**
     * Places the cart on a rail
     */
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final Level worldIn = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockState iblockstate = worldIn.getBlockState(pos);

        if (!BaseRailBlock.isRail(iblockstate))
        {
            return InteractionResult.FAIL;
        }
        else
        {
            final ItemStack itemstack = context.getItemInHand();

            if (!worldIn.isClientSide())
            {
                final RailShape railShape = iblockstate.getBlock() instanceof BaseRailBlock rail
                    ? rail.getRailDirection(iblockstate, worldIn, pos, null)
                    : RailShape.NORTH_SOUTH;
                double d0 = 0.0D;

                if (railShape.isAscending())
                {
                    d0 = 0.5D;
                }

                final EntityBombCart entityminecart = new EntityBombCart(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.0625D + d0, (double) pos.getZ() + 0.5D, itemstack);

                if (itemstack.has(DataComponents.CUSTOM_NAME))
                {
                    entityminecart.setCustomName(itemstack.getHoverName());
                }

                worldIn.addFreshEntity(entityminecart);
            }

            itemstack.shrink(1);
            return InteractionResult.sidedSuccess(worldIn.isClientSide());
        }
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        return ExplosiveItemHelper.getVariantKey("bombcart", itemstack);
    }

    @Override
    public String getDescriptionId()
    {
        return "bombcart";
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        ExplosiveItemHelper.addSubItems(this, CreateICBMAPI.EX_MINECART_REGISTRY, items);
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //TODO change over to a hook
        ItemReg.itemBlockExplosive.get().getDetailedInfo(stack, player, list);
    }
}
