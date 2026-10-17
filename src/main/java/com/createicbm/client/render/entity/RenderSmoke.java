package com.createicbm.client.render.entity;

import com.createicbm.content.entity.EntitySmoke;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/** Smoke entity only spawns particles, nothing is drawn */
public class RenderSmoke extends EntityRenderer<EntitySmoke>
{
    public RenderSmoke(EntityRendererProvider.Context context)
    {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySmoke entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
