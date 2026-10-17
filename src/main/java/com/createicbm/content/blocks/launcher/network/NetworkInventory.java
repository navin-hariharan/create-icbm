package com.createicbm.content.blocks.launcher.network;

import com.createicbm.api.CreateICBMAPI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class NetworkInventory implements IItemHandler {

    private final LauncherNetwork network;
    private final List<SlotHolder> slots = new ArrayList();

    /** True when the network changed since the slots were last built */
    private boolean dirty = true;

    /**
     * Marks the slot list for a rebuild on next access. PERF: building a network adds launchers one at a time, so
     * rebuilding right away looked up the item handler of every launcher for each one added (quadratic on load).
     */
    public void markDirty() {
        dirty = true;
    }

    private List<SlotHolder> slots() {
        if (dirty) {
            buildInventory();
        }
        return slots;
    }

    public void buildInventory() {
        dirty = false;
        slots.clear();

        network.getComponents().stream().filter(LauncherNode::isAcceptsItems).forEach((node) -> {
            final IItemHandler handler = getHandler(node.getSelf());
            if(handler != null) {
                final int slotCount = handler.getSlots();
                for(int i = 0; i < slotCount; i++) {
                    slots.add(new SlotHolder(handler, i));
                }
            }
        });
    }

    @Override
    public int getSlots() {
        return slots().size();
    }

    private IItemHandler getHandler(BlockEntity tile) {
        if(LauncherNetwork.getTileCapability(tile, CreateICBMAPI.MISSILE_LAUNCHER_CAPABILITY, null) != null) {
            return LauncherNetwork.getTileCapability(tile, Capabilities.ItemHandler.BLOCK, null);
        }
        return null;
    }

    /**
     * The slot list shrinks when a launcher leaves the network, while storage mods (Tom's Storage, pipes) may still
     * hold an index from before. Out of range slots read as empty and refuse items instead of throwing.
     */
    @Nullable
    private SlotHolder getHolder(int slot) {
        final List<SlotHolder> current = slots();
        return slot >= 0 && slot < current.size() ? current.get(slot) : null;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        final SlotHolder holder = getHolder(slot);
        return holder != null ? holder.handler.getStackInSlot(holder.getSlotIndex()) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        final SlotHolder holder = getHolder(slot);
        return holder != null ? holder.handler.insertItem(holder.getSlotIndex(), stack, simulate) : stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        final SlotHolder holder = getHolder(slot);
        return holder != null ? holder.handler.extractItem(holder.getSlotIndex(), amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        final SlotHolder holder = getHolder(slot);
        return holder != null ? holder.handler.getSlotLimit(holder.getSlotIndex()) : 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        final SlotHolder holder = getHolder(slot);
        return holder != null && holder.handler.isItemValid(holder.getSlotIndex(), stack);
    }

    @Data
    @AllArgsConstructor
    private static class SlotHolder {
        private final IItemHandler handler;
        private final int slotIndex;
    }
}
