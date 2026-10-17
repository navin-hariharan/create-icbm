package com.createicbm.content.blocks.launcher.cruise;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.createicbm.ICBMConstants;
import com.createicbm.client.models.CruiseLauncherTopModel;
import com.createicbm.client.render.entity.RenderMissile;
import net.minecraft.client.renderer.MultiBufferSource;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/10/2017.
 * <p> Client only, register for BlockReg.TILE_CRUISE_LAUNCHER. Needs layer {@link CruiseLauncherTopModel#LAYER}.
 */
public class TESRCruiseLauncher extends KineticBlockEntityRenderer<TileCruiseLauncher>
{
    public static final ResourceLocation TEXTURE_FILE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "textures/models/cruise_launcher_top.png");

    private final CruiseLauncherTopModel model;

    public TESRCruiseLauncher(BlockEntityRendererProvider.Context context)
    {
        super(context);
        this.model = new CruiseLauncherTopModel(context.bakeLayer(CruiseLauncherTopModel.LAYER));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TileCruiseLauncher be, BlockState state)
    {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
    }

    @Override
    protected void renderSafe(TileCruiseLauncher launcher, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        // Input shaft, skipped by Create when Flywheel draws it
        super.renderSafe(launcher, partialTicks, poseStack, buffer, packedLight, packedOverlay);

        float yaw = (float) launcher.currentAim.yaw();
        float pitch = (float) launcher.currentAim.pitch();

        // Render top of launcher
        poseStack.pushPose();
        poseStack.translate(0.5F, 2.5F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
        model.render(poseStack, buffer.getBuffer(model.renderType(TEXTURE_FILE)), packedLight, packedOverlay,
            -(float)Math.toRadians(yaw), -(float)Math.toRadians(pitch));
        poseStack.popPose();

        // Render held missile
        if (!launcher.cachedMissileStack.isEmpty() && RenderMissile.INSTANCE != null) {
            RenderMissile.INSTANCE.renderItem(launcher.cachedMissileStack, launcher.getLevel(), poseStack, buffer, packedLight,
                0.5, 2, 0.5, yaw, pitch - 90, partialTicks);
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileCruiseLauncher launcher)
    {
        final BlockPos pos = launcher.getBlockPos();
        return new AABB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
    }
}
