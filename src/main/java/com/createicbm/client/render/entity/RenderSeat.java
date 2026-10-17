package com.createicbm.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.createicbm.content.entity.EntityPlayerSeat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class RenderSeat extends EntityRenderer<EntityPlayerSeat>
{
    public RenderSeat(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityPlayerSeat seat, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(seat, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPlayerSeat entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
