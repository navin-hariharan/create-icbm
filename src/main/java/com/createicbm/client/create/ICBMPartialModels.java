package com.createicbm.client.create;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.createicbm.ICBMConstants;
import net.minecraft.resources.ResourceLocation;

/**
 * Moving model parts drawn by the machine renderers, loaded by Flywheel like Create's AllPartialModels.
 * {@link #init()} must run during mod construction, before models load.
 */
public final class ICBMPartialModels
{
    /** Radar dish, spins around the vertical axis through the block centre */
    public static final PartialModel RADAR_DISH = block("radar_station/dish");
    public static final PartialModel ADVANCED_RADAR_DISH = block("advanced_radar_station/dish");

    private ICBMPartialModels()
    {
    }

    private static PartialModel block(String path)
    {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "block/" + path));
    }

    /** Loads the class so the partials above are created in time */
    public static void init()
    {
    }
}
