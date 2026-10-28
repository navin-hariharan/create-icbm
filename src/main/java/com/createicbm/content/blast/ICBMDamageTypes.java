package com.createicbm.content.blast;

import com.createicbm.ICBMConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * <p> Each key needs a JSON file at {@code data/create_icbm/damage_type/<path>.json}.
 * <pre>
 * CHEMICAL           chemical           icbm.chemical                  no_knockback
 * CONTAGIOUS         contagious         icbm.contagious                no_knockback
 * RADIOACTIVE_BLOCK  radioactive_block  create_icbm:radioactive_block  no_knockback
 * PROJECTILE         projectile         create_icbm:projectile         is_projectile, no_knockback
 * MISSILE            missile            missile                        - (SAM hitting its target)
 * </pre>
 */
public final class ICBMDamageTypes
{
    public static final ResourceKey<DamageType> CHEMICAL = key("chemical");
    public static final ResourceKey<DamageType> CONTAGIOUS = key("contagious");
    public static final ResourceKey<DamageType> REDMATTER = key("redmatter");
    public static final ResourceKey<DamageType> RADIOACTIVE_BLOCK = key("radioactive_block");
    public static final ResourceKey<DamageType> FRAGMENTS = key("fragments");
    public static final ResourceKey<DamageType> PROJECTILE = key("projectile");
    public static final ResourceKey<DamageType> MISSILE = key("missile");

    private ICBMDamageTypes()
    {
    }

    private static ResourceKey<DamageType> key(String path)
    {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, path));
    }

    /**
     * Resolves the damage type holder from the level's registries
     */
    public static Holder<DamageType> holder(Level level, ResourceKey<DamageType> key)
    {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key)
    {
        return new DamageSource(holder(level, key));
    }

    /**
     * Builds a damage source with entities
     *
     * @param level  to pull the registry from
     * @param key    damage type
     * @param direct entity that hit (projectile, fragment), may be null
     * @param cause  entity responsible (shooter), may be null
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity direct, @Nullable Entity cause)
    {
        return new DamageSource(holder(level, key), direct, cause);
    }

    // DamageSource#getSourcePosition falls back to the direct entity's position.
}
