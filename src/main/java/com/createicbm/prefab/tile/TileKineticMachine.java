package com.createicbm.prefab.tile;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import com.createicbm.ICBMConstants;
import com.createicbm.config.ConfigMain;
import com.createicbm.lib.energy.storage.EnergyActionType;
import com.createicbm.lib.tile.ITick;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Create addon version of {@link TileMachine}: the machine sits on a Create kinetic network and takes its power from
 * shaft rotation instead of Forge Energy.
 * <p>
 * Same tick action API as {@link TileMachine}. Saving goes through {@link #write(CompoundTag, HolderLookup.Provider, boolean)}
 * and {@link #read(CompoundTag, HolderLookup.Provider, boolean)} (Create's SmartBlockEntity owns saveAdditional and
 * setRemoved), and {@link #invalidate()} replaces setRemoved for cleanup on unload or removal.
 */
public abstract class TileKineticMachine extends KineticBlockEntity
{
    /** Speed Create treats as a typical network, charge times in the config are given at this speed */
    public static final float REFERENCE_RPM = 64f;

    protected int ticks = -1;

    protected final List<ITick> tickActions = new ArrayList<>();

    /** Leftover fraction of a charge unit between ticks, so slow shafts still charge */
    private float chargeRemainder = 0;

    /** Ticks between full syncs while a charge buffer fills, see {@link #onChargeChanged(EnergyActionType)} */
    public static final int CHARGE_SYNC_TICKS = 10;

    /** Charge was added without a full {@link #setChanged()}, flushed by {@link #update()} */
    private boolean chargeSyncPending = false;

    public TileKineticMachine(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void tick()
    {
        super.tick();
        update();
    }

    /** Called each tick after Create's kinetic update, both sides */
    public void update()
    {
        //Increase tick
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1)
        {
            ticks = 0;
        }

        if (chargeSyncPending && ticks % CHARGE_SYNC_TICKS == 0)
        {
            chargeSyncPending = false;
            setChanged();
        }

        // PERF: plain loop, the forEach lambda captured this and was allocated every tick for every machine
        final boolean server = isServer();
        for (int i = 0; i < tickActions.size(); i++)
        {
            tickActions.get(i).update(ticks, server);
        }
    }

    /** Create's "rotation indicator" particles, shown when the block is placed */
    public void queueRotationIndicators()
    {
        effects.queueRotationIndicators();
    }

    /** Minimum speed (RPM, either direction) this machine needs to run, 0 for any rotation */
    public float getMinimumSpeed()
    {
        return 0;
    }

    /**
     * True when the shaft turns fast enough and the network is not overstressed (Create reports 0 speed then).
     * Always true when the server turned off the rotation requirement.
     */
    public boolean isRunning()
    {
        if (!ConfigMain.REQUIRES_POWER)
        {
            return true;
        }
        final float speed = Math.abs(getSpeed());
        return speed > 0 && speed >= getMinimumSpeed();
    }

    /**
     * Charge units earned this tick from rotation.
     *
     * @param unitsPerRpmTick charge units per RPM per tick
     * @return whole units to add, fractions carry over to the next tick
     */
    protected int takeRotationCharge(float unitsPerRpmTick)
    {
        if (!isRunning() || !ConfigMain.REQUIRES_POWER)
        {
            return 0;
        }
        chargeRemainder += Math.abs(getSpeed()) * unitsPerRpmTick;
        final int whole = (int) chargeRemainder;
        chargeRemainder -= whole;
        return whole;
    }

    /**
     * Change callback for a charge buffer. Charging from the shaft changes the buffer every tick, so those changes only
     * mark the chunk for saving and the full {@link #setChanged()} (description packet to every nearby player) runs at
     * most every {@link #CHARGE_SYNC_TICKS}. Other changes (firing, loading) still call it right away.
     */
    protected void onChargeChanged(EnergyActionType type)
    {
        if (type != EnergyActionType.ADD || !isServer())
        {
            setChanged();
        }
        else
        {
            chargeSyncPending = true;
            level.blockEntityChanged(getBlockPos());
        }
    }

    /** Units per RPM per tick that fill {@code capacity} in {@code seconds} at {@link #REFERENCE_RPM} */
    public static float rateFor(int capacity, double seconds)
    {
        return (float) (capacity / (Math.max(0.05, seconds) * 20.0 * REFERENCE_RPM));
    }

    /** Goggle line helper: "label: value" in Create's goggle style */
    protected static void addGoggleLine(List<Component> tooltip, String key, Object value, ChatFormatting valueColour)
    {
        CreateLang.builder()
            .add(Component.translatable("goggles." + ICBMConstants.PREFIX + key).withStyle(ChatFormatting.GRAY))
            .text(" ")
            .add(Component.literal(String.valueOf(value)).withStyle(valueColour))
            .forGoggles(tooltip, 1);
    }

    /** Goggle header in Create's style */
    protected static void addGoggleHeader(List<Component> tooltip, String key)
    {
        CreateLang.builder()
            .add(Component.translatable("goggles." + ICBMConstants.PREFIX + key))
            .forGoggles(tooltip);
    }

    /** Charge as a short percentage text */
    protected static String percent(int stored, int max)
    {
        if (max <= 0)
        {
            return "100%";
        }
        return Math.round(100f * stored / max) + "%";
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
