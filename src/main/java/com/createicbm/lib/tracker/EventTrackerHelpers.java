package com.createicbm.lib.tracker;

import com.createicbm.ICBMConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Function;

public class EventTrackerHelpers {
    public static final ResourceLocation TILE_POS = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "tile.pos");
    public static final ResourceLocation TILE_POS_X = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "tile.pos.x");
    public static final ResourceLocation TILE_POS_Y = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "tile.pos.y");
    public static final ResourceLocation TILE_POS_Z = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "tile.pos.z");

    public static final ResourceLocation WORLD_ID = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "level.id");
    public static final ResourceLocation WORLD_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "level.name");
    public static final ResourceLocation WORLD_SIDE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "level.side");

    public static String SIDE_CLIENT = "client";
    public static String SIDE_SERVER = "server";

    public static Function<EventTrackerEntry, String> stringAt(int index) {
        return (entry) -> (String) entry.getData()[index];
    }

    public static String getWorldId(Level world) {
        return Optional.ofNullable(world).map(w -> w.dimension().location().toString()).orElse(null);
    }

    public static String getSide(Level world) {
        return Optional.ofNullable(world).map((w) -> w.isClientSide() ? SIDE_CLIENT : SIDE_SERVER).orElse(null);
    }
}
