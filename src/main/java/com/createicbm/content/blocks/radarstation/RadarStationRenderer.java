package com.createicbm.content.blocks.radarstation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.createicbm.client.create.ICBMPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Radar station: Create input shaft (when Flywheel is off) and the dish, which turns with the shaft.
 * Client only, register for BlockReg.TILE_RADAR_STATION.
 */
public class RadarStationRenderer extends KineticBlockEntityRenderer<TileRadarStation>
{
    public RadarStationRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TileRadarStation be, BlockState state)
    {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
    }

    @Override
    protected void renderSafe(TileRadarStation radar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        super.renderSafe(radar, partialTicks, poseStack, buffer, packedLight, packedOverlay);

        // Dish turns at the shaft speed, same angle maths as Create's own rotating parts
        final BlockState state = radar.getBlockState();
        final float angle = getAngleForBe(radar, radar.getBlockPos(), Direction.Axis.Y);
        // The dish sits in the block above, light it from there
        final int dishLight = radar.getLevel() != null ? LevelRenderer.getLightColor(radar.getLevel(), radar.getBlockPos().above()) : packedLight;
        CachedBuffers.partial(ICBMPartialModels.RADAR_DISH, state)
            .rotateCentered(angle, Direction.UP)
            .light(dishLight)
            .renderInto(poseStack, buffer.getBuffer(RenderType.cutoutMipped()));
    }

    @Override
    public AABB getRenderBoundingBox(TileRadarStation tile)
    {
        // Dish reaches a block above the station
        return new AABB(tile.getBlockPos()).expandTowards(0, 1, 0).inflate(0.5, 0, 0.5);
    }
}
