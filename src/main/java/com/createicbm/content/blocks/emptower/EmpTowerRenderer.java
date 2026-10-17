package com.createicbm.content.blocks.emptower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Base EMP tower: Create input shaft (when Flywheel is off) plus the spinning coil from {@link TESREmpTower}.
 * Client only, register for BlockReg.TILE_EMP_TOWER. Extenders keep using {@link TESREmpTower} directly.
 */
public class EmpTowerRenderer extends KineticBlockEntityRenderer<TileEMPTower>
{
    private final TESREmpTower<TileEMPTower> coil;

    public EmpTowerRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
        this.coil = new TESREmpTower<>(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TileEMPTower be, BlockState state)
    {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
    }

    @Override
    protected void renderSafe(TileEMPTower tower, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        super.renderSafe(tower, partialTicks, poseStack, buffer, packedLight, packedOverlay);
        coil.render(tower, partialTicks, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(TileEMPTower tile)
    {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEMPTower tile)
    {
        return AABB.INFINITE;
    }
}
