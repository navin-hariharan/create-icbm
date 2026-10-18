package com.createicbm.api.refs;

import com.createicbm.api.CreateICBMAPI;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds references related to ICBM entities
 * Created by Robin Seifert on 1/7/19.
 */
public final class ICBMEntities
{

    //TODO refactor all entity names to match vanilla `_` style
    public static final ResourceLocation BLOCK_GRAVITY = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "block.gravity");
    public static final ResourceLocation BLOCK_FRAGMENT = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "block.fragment");
    public static final ResourceLocation BLOCK_EXPLOSIVE = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "block.explosive");

    public static final ResourceLocation MISSILE_EXPLOSIVE = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "explosive_missile");
    public static final ResourceLocation MISSILE_GENERIC = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "generic_missile");
    public static final ResourceLocation MISSILE_SAM = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "surface_to_air_missile");
    public static final ResourceLocation MISSILE_DELIVERY = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "delivery_missile");

    public static final ResourceLocation EXPLOSION = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "holder.explosion");
    public static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "beam.light");
    public static final ResourceLocation BOMB_CART = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "cart.bomb");
    public static final ResourceLocation MISSILE_SEAT = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "holder.seat");

    public static final ResourceLocation REDMATTER = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "redmatter");

    public static final ResourceLocation SMOKE = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "smoke");

    public static final ResourceLocation PHANTOM_ZOMBIE = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "phantom_zombie");
    public static final ResourceLocation PHANTOM_HUSK = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "phantom_husk");
    public static final ResourceLocation PHANTOM_SPIDER = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "phantom_spider");
    public static final ResourceLocation PHANTOM_CREEPER = ResourceLocation.fromNamespaceAndPath(CreateICBMAPI.ID, "phantom_creeper");
}
