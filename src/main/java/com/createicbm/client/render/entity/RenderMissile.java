package com.createicbm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.createicbm.client.render.entity.item.RenderItemImp;
import com.createicbm.content.missile.entity.EntityMissile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderMissile extends RenderItemImp<EntityMissile>
{
    public static RenderMissile INSTANCE;

    public RenderMissile(EntityRendererProvider.Context context)
    {
        super(context);
        INSTANCE = this;
    }

    @Override
    protected ItemStack getRenderItem(EntityMissile entity, int index) {
        return entity.toStack();
    }

    @Override
    protected void translate(@Nullable EntityMissile entity, ItemStack itemstack, BakedModel iBakedModel, PoseStack poseStack, float partialTicks, int index) {
        //Translate to center of entity collider
        if(entity != null) {
            poseStack.translate(0, 0.2, 0); //TODO handle in JSON
        }
    }

    @Override
    protected float getYaw(@Nonnull EntityMissile entityMissile, float providedYaw, float partialTicks) {
        return providedYaw - 180; //TODO handle offset in JSON
    }

    @Override
    protected float getPitch(@Nonnull EntityMissile entityMissile, float providedPitch, float partialTicks) {
        return  providedPitch - 90; //TODO handle offset in JSON
    }

    @Override
    protected void rotate(@Nullable EntityMissile entityMissile, ItemStack itemstack, PoseStack poseStack, float entityYaw, float entityPitch, float partialTicks, int index) {
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(entityPitch));

        //Translate to rotation point of model TODO handle in JSON
        if(entityMissile != null) {
            poseStack.translate(0, -0.8, 0);
        }
    }

    /**
     * Renders a missile stack outside of an entity, used by launcher block entity renderers.
     *
     * @param x - offset from the current pose origin
     * @param y - offset from the current pose origin
     * @param z - offset from the current pose origin
     */
    public void renderItem(ItemStack missileStack, Level level, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           double x, double y, double z, float yaw, float pitch, float partialTicks)
    {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        this.renderItem(missileStack, level, poseStack, buffer, packedLight, yaw, pitch, partialTicks);
        poseStack.popPose();
    }

    @Override
    public void render(@Nonnull EntityMissile entityMissile, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(entityMissile, entityYaw, partialTicks, poseStack, buffer, packedLight);

        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) //TODO fix so we can see motion vector
        {
            final Vec3 motion = entityMissile.getDeltaMovement();
            final Vec3 normal = motion.lengthSqr() > 0 ? motion.normalize() : new Vec3(0, 1, 0);
            final PoseStack.Pose pose = poseStack.last();
            final VertexConsumer lines = buffer.getBuffer(RenderType.lines());
            lines.addVertex(pose, 0, 0, 0).setColor(0, 255, 0, 255).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
            lines.addVertex(pose, (float) (motion.x * 2.0D), (float) (motion.y * 2.0D), (float) (motion.z * 2.0D))
                .setColor(0, 255, 0, 255).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
        }
    }
}
