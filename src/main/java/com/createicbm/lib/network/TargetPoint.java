package com.createicbm.lib.network;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * @param dimension level the point is in
 * @param x position
 * @param y position
 * @param z position
 * @param range distance from the point players must be within
 */
public record TargetPoint(ResourceKey<Level> dimension, double x, double y, double z, double range)
{
}
