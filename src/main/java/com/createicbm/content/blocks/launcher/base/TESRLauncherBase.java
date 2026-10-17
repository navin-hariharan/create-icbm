package com.createicbm.content.blocks.launcher.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.createicbm.client.render.entity.RenderMissile;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the missile held by the launcher base and, when Flywheel is off, its input shaft. Client only, register
 * for BlockReg.TILE_LAUNCHER_BASE.
 */
public class TESRLauncherBase extends KineticBlockEntityRenderer<TileLauncherBase>
{
    private static final float missileOffset = 2f; //TODO add custom render type for missiles as static blocks so offset can be driven by model
    private static final List<Vec3> FACE_OFFSETS = Arrays.stream(Direction.values())
        .map(direction -> new Vec3(
            0.5 + direction.getStepX() * missileOffset,
            0.5 + direction.getStepY() * missileOffset,
            0.5 + direction.getStepZ() * missileOffset
        )).collect(Collectors.toList());

    public TESRLauncherBase(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TileLauncherBase be, BlockState state)
    {
        // Half shaft on the back of the pad, opposite the launch direction
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state.getValue(BlockLauncherBase.ROTATION_PROP).getOpposite());
    }

    @Override
    protected void renderSafe(TileLauncherBase launcher, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        // Shaft, skipped by Create when Flywheel draws it
        super.renderSafe(launcher, partialTicks, poseStack, buffer, packedLight, packedOverlay);

        //Render missile
        if (!launcher.getMissileStack().isEmpty() && RenderMissile.INSTANCE != null)
        {
            final Vec3 offset = FACE_OFFSETS.get(launcher.getLaunchDirection().ordinal());
            RenderMissile.INSTANCE.renderItem(launcher.getMissileStack(), launcher.getLevel(), poseStack, buffer, packedLight,
                offset.x, offset.y, offset.z,
                launcher.getMissileYaw(true), launcher.getMissilePitch(true), partialTicks);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileLauncherBase tile)
    {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileLauncherBase tile)
    {
        return AABB.INFINITE;
    }
}
