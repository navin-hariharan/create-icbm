package com.createicbm.content.items;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.content.reg.ComponentReg;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.capability.ex.CapabilityExplosiveStack;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.lib.capability.missile.CapabilityMissileStack;
import com.createicbm.prefab.item.ISubItemProvider;
import com.createicbm.prefab.item.ItemBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMissile extends ItemBase implements ICapabilityHolderItem, ISubItemProvider
{
    public ItemMissile()
    {
        super(new Item.Properties().stacksTo(1));
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
        else if (capability == CreateICBMAPI.MISSILE_STACK_CAPABILITY)
        {
            return (T) new CapabilityMissileStack(stack);
        }
        return null;
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        return ExplosiveItemHelper.getVariantKey("missile", itemstack);
    }

    @Override
    public String getDescriptionId()
    {
        return "missile";
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        ExplosiveItemHelper.addSubItems(this, CreateICBMAPI.EX_MISSILE_REGISTRY, items);

        //TODO fix work around for missile module not counting as a missile
        final ItemStack module = ExplosiveItemHelper.create(this, 24);
        if (module.has(ComponentReg.EXPLOSIVE.get()) && items.stream().noneMatch(s -> ItemStack.isSameItemSameComponents(s, module)))
        {
            items.add(module);
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, @Nullable Player player)
    {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        //TODO add hook
        ItemReg.itemBlockExplosive.get().getDetailedInfo(stack, player, list);
        final IExplosive explosive = CreateICBMHelpers.getExplosive(stack);
        if(explosive != null) { //TODO make shift-key display?
            explosive.collectInformation(line -> list.add(Component.literal(line)));
        }
    }
}
