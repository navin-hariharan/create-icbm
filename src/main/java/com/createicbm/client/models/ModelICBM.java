package com.createicbm.client.models;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Base for ICBM models.
 */
public abstract class ModelICBM extends Model
{
    public ModelICBM()
    {
        this(RenderType::entitySolid);
    }

    public ModelICBM(Function<ResourceLocation, RenderType> renderType)
    {
        super(renderType);
    }

    /** Rendering goes through renderToBuffer now. */
    public void render(float f5)
    {

    }
}
