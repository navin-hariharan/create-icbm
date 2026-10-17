package com.createicbm.client.create;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.createicbm.content.blocks.launcher.base.BlockLauncherBase;
import com.createicbm.content.reg.BlockReg;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

/**
 * Flywheel visuals for the input shaft of each ICBM machine, the instanced counterpart of the shaft drawn by the
 * machine renderers when Flywheel is off. The renderers keep running for the rest of each machine (missile, dish,
 * coil), so vanilla rendering is never skipped.
 */
public final class ShaftVisuals
{
    private ShaftVisuals()
    {
    }

    /** Client setup */
    public static void register()
    {
        shaftHalf(BlockReg.TILE_LAUNCHER_BASE.get(), be -> be.getBlockState().getValue(BlockLauncherBase.ROTATION_PROP).getOpposite());
        shaftHalf(BlockReg.TILE_CRUISE_LAUNCHER.get(), be -> Direction.DOWN);
        shaftHalf(BlockReg.TILE_RADAR_STATION.get(), be -> Direction.DOWN);
        shaftHalf(BlockReg.TILE_EMP_TOWER.get(), be -> Direction.DOWN);
    }

    /** A half shaft from the block centre out through the given face, turning with the block entity */
    private static <T extends KineticBlockEntity> void shaftHalf(BlockEntityType<T> type, Function<T, Direction> side)
    {
        SimpleBlockEntityVisualizer.builder(type)
            .factory((context, blockEntity, partialTick) -> new OrientedRotatingVisual<>(context, blockEntity, partialTick,
                Direction.SOUTH, side.apply(blockEntity), Models.partial(AllPartialModels.SHAFT_HALF)))
            .skipVanillaRender(blockEntity -> false)
            .apply();
    }
}
