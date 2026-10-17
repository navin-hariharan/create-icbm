package com.createicbm.content.missile.entity.delivery.gui;

import com.createicbm.content.missile.entity.delivery.ItemDeliveryMissile;
import com.createicbm.content.reg.MenuReg;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One slot menu for the Delivery Missile held in a hand, opened with shift + right click. The slot is a mirror of
 * the missile's cargo: every change is written straight back to the missile stack, so closing the menu has
 * nothing to hand back.
 * <p>
 * The missile itself is locked in place while the menu is open (no pick up, number key or offhand swap), and the
 * server closes the menu if that exact stack leaves the hand.
 */
public class ContainerDeliveryMissile extends AbstractContainerMenu
{
    public static final int CARGO_X = 80;
    public static final int CARGO_Y = 26;
    public static final int INVENTORY_X = 7;
    public static final int INVENTORY_Y = 51;
    /** Swap button the client sends for the offhand key */
    private static final int OFFHAND_BUTTON = 40;

    private final InteractionHand hand;
    /** Stack the menu was opened for, written to directly so the cargo can't end up anywhere else */
    private final ItemStack missile;
    private final SimpleContainer cargo = new SimpleContainer(1);
    /** Hotbar index (or offhand) holding the missile, number key swaps with it are blocked */
    private final int lockedButton;

    public ContainerDeliveryMissile(int windowId, Inventory playerInventory, InteractionHand hand)
    {
        super(MenuReg.DELIVERY_MISSILE.get(), windowId);
        this.hand = hand;
        this.missile = playerInventory.player.getItemInHand(hand);
        this.lockedButton = hand == InteractionHand.MAIN_HAND ? playerInventory.selected : OFFHAND_BUTTON;

        cargo.setItem(0, ItemDeliveryMissile.getCargo(missile));
        cargo.addListener(container -> {
            if (missile.getItem() instanceof ItemDeliveryMissile)
            {
                ItemDeliveryMissile.setCargo(missile, container.getItem(0).copy());
            }
        });

        addSlot(new Slot(cargo, 0, CARGO_X, CARGO_Y)
        {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return ItemDeliveryMissile.canCarry(stack);
            }

            @Override
            public boolean allowModification(Player player)
            {
                // A bundle or delivery missile in the slot could otherwise be filled through it, skipping the nesting check
                final ItemStack held = getItem();
                return super.allowModification(player) && !(held.getItem() instanceof ItemDeliveryMissile) && !held.has(DataComponents.BUNDLE_CONTENTS);
            }
        });

        for (int row = 0; row < 3; ++row)
        {
            for (int column = 0; column < 9; ++column)
            {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, INVENTORY_X + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column)
        {
            if (hand == InteractionHand.MAIN_HAND && column == lockedButton)
            {
                addSlot(new LockedSlot(playerInventory, column, INVENTORY_X + column * 18, INVENTORY_Y + 58));
            }
            else
            {
                addSlot(new Slot(playerInventory, column, INVENTORY_X + column * 18, INVENTORY_Y + 58));
            }
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player)
    {
        // Number key or offhand swaps would move the missile out from under the menu
        if (clickType == ClickType.SWAP && button == lockedButton)
        {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        final Slot slot = slots.get(index);
        if (!slot.hasItem())
        {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getItem();
        final ItemStack original = stack.copy();
        if (index == 0)
        {
            if (!moveItemStackTo(stack, 1, slots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else if (!moveItemStackTo(stack, 0, 1, false))
        {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty())
        {
            slot.setByPlayer(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount())
        {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return !missile.isEmpty() && player.getItemInHand(hand) == missile;
    }

    /** Hotbar slot holding the open missile */
    private static class LockedSlot extends Slot
    {
        LockedSlot(Inventory inventory, int index, int x, int y)
        {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player)
        {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return false;
        }
    }
}
