package com.createicbm.content.missile.entity.delivery;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.content.missile.entity.delivery.gui.ContainerDeliveryMissile;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.prefab.item.ItemICBMBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Delivery Missile, carries one stack of items (what one chest slot holds) to its target. See
 * {@link EntityDeliveryMissile} for the landing.
 * <p>
 * Shift + right click with the missile in hand opens a one slot menu ({@link ContainerDeliveryMissile}). It also
 * loads like a bundle: right click a stack onto the missile in an inventory, or right click the held missile onto a
 * stack. Right click with an empty hand takes the stack back out. The cargo is kept in the vanilla
 * {@link DataComponents#CONTAINER} component.
 * <p>
 * Anything a chest slot holds can be carried, shulker boxes included, except a loaded delivery missile (directly or
 * inside another container item) so missiles can't be nested without end.
 */
public class ItemDeliveryMissile extends ItemICBMBase implements ICapabilityHolderItem
{
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);
    /** Container items looked into when checking for nested missiles, deeper is refused */
    private static final int NESTING_CHECK_DEPTH = 8;

    public ItemDeliveryMissile()
    {
        super("delivery_missile", new Item.Properties().stacksTo(1));
    }

    /** @return copy of the carried stack, empty if there is none */
    public static ItemStack getCargo(ItemStack missile)
    {
        final ItemContainerContents contents = missile.get(DataComponents.CONTAINER);
        return contents != null ? contents.copyOne() : ItemStack.EMPTY;
    }

    public static void setCargo(ItemStack missile, ItemStack cargo)
    {
        if (cargo.isEmpty())
        {
            missile.remove(DataComponents.CONTAINER);
        }
        else
        {
            missile.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(cargo)));
        }
    }

    /** @return true if the stack may be loaded as cargo */
    public static boolean canCarry(ItemStack stack)
    {
        return !stack.isEmpty() && !holdsLoadedMissile(stack, NESTING_CHECK_DEPTH);
    }

    private static boolean holdsLoadedMissile(ItemStack stack, int depth)
    {
        if (stack.getItem() instanceof ItemDeliveryMissile && stack.has(DataComponents.CONTAINER))
        {
            return true;
        }
        final ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        final BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if ((contents != null || bundle != null) && depth <= 0)
        {
            return true;
        }
        if (contents != null)
        {
            if (contents.stream().anyMatch(inner -> !inner.isEmpty() && holdsLoadedMissile(inner, depth - 1)))
            {
                return true;
            }
        }
        if (bundle != null)
        {
            for (ItemStack inner : bundle.itemsCopy())
            {
                if (holdsLoadedMissile(inner, depth - 1))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** Shift + right click opens the cargo slot */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        final ItemStack missile = player.getItemInHand(hand);
        if (!player.isSecondaryUseActive() || missile.getCount() != 1)
        {
            return InteractionResultHolder.pass(missile);
        }
        if (!level.isClientSide())
        {
            player.openMenu(new SimpleMenuProvider((windowId, inventory, p) -> new ContainerDeliveryMissile(windowId, inventory, hand), missile.getHoverName()),
                buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(missile, level.isClientSide());
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == CreateICBMAPI.MISSILE_STACK_CAPABILITY)
        {
            return (T) new CapabilityDeliveryMissile(stack);
        }
        return null;
    }

    /** Missile on the cursor, right clicked onto a slot: load that slot's stack, or unload into an empty slot */
    @Override
    public boolean overrideStackedOnOther(ItemStack missile, Slot slot, ClickAction action, Player player)
    {
        if (missile.getCount() != 1 || action != ClickAction.SECONDARY)
        {
            return false;
        }

        final ItemStack cargo = getCargo(missile);
        final ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty())
        {
            if (cargo.isEmpty())
            {
                return false;
            }
            setCargo(missile, slot.safeInsert(cargo));
            playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
            return true;
        }

        if (!canCarry(slotStack))
        {
            return false;
        }
        if (cargo.isEmpty() || ItemStack.isSameItemSameComponents(cargo, slotStack))
        {
            final int space = cargo.isEmpty() ? slotStack.getMaxStackSize() : cargo.getMaxStackSize() - cargo.getCount();
            if (space > 0)
            {
                final ItemStack taken = slot.safeTake(slotStack.getCount(), space, player);
                if (!taken.isEmpty())
                {
                    taken.grow(cargo.getCount());
                    setCargo(missile, taken);
                    playSound(player, SoundEvents.BUNDLE_INSERT);
                }
            }
            return true;
        }
        return false;
    }

    /** Stack on the cursor, right clicked onto the missile: load it, or swap it with the cargo */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack missile, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access)
    {
        if (missile.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player))
        {
            return false;
        }

        final ItemStack cargo = getCargo(missile);
        if (other.isEmpty())
        {
            if (cargo.isEmpty())
            {
                return false;
            }
            access.set(cargo);
            setCargo(missile, ItemStack.EMPTY);
            playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
        }
        else if (!canCarry(other))
        {
            return false;
        }
        else if (cargo.isEmpty())
        {
            setCargo(missile, other.split(other.getCount()));
            playSound(player, SoundEvents.BUNDLE_INSERT);
        }
        else if (ItemStack.isSameItemSameComponents(cargo, other))
        {
            final int move = Math.min(other.getCount(), cargo.getMaxStackSize() - cargo.getCount());
            if (move > 0)
            {
                cargo.grow(other.split(move).getCount());
                setCargo(missile, cargo);
                playSound(player, SoundEvents.BUNDLE_INSERT);
            }
        }
        else
        {
            final ItemStack load = other.copy();
            access.set(cargo);
            setCargo(missile, load);
            playSound(player, SoundEvents.BUNDLE_INSERT);
        }
        return true;
    }

    private static void playSound(Player player, SoundEvent sound)
    {
        player.playSound(sound, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    /** Fill bar like the bundle's */
    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return !getCargo(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        final ItemStack cargo = getCargo(stack);
        return cargo.isEmpty() ? 0 : Math.min(1 + 12 * cargo.getCount() / cargo.getMaxStackSize(), 13);
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        final ItemStack cargo = getCargo(stack);
        if (cargo.isEmpty())
        {
            list.add(Component.translatable(getDescriptionId() + ".empty").withStyle(ChatFormatting.GRAY));
        }
        else
        {
            list.add(Component.translatable(getDescriptionId() + ".cargo", cargo.getCount(), cargo.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
    }

    /** A destroyed item entity drops its cargo, like a bundle or shulker box */
    @Override
    public void onDestroyed(ItemEntity itemEntity)
    {
        final ItemStack cargo = getCargo(itemEntity.getItem());
        if (!cargo.isEmpty())
        {
            itemEntity.getItem().remove(DataComponents.CONTAINER);
            ItemUtils.onContainerDestroyed(itemEntity, List.of(cargo));
        }
    }
}
