package com.createicbm.content.potion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.createicbm.lib.transform.vector.Pos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;

/**
 * The 1.21 registry contract registers the poisons as mob effects
 * ({@code CreateICBM.chemicalPotion}/{@code contagiousPotion}), so it extends {@link MobEffect}. The effect itself is
 * never applied to entities, {@link #poisonEntity} still applies vanilla effects exactly as before.
 *
 * @deprecated remove at some point and apply potion effects directly
 */
@Deprecated
public abstract class Poison extends MobEffect
{
    static HashMap<String, Poison> poisons = new HashMap();
    static BiMap<String, Integer> poisonIDs = HashBiMap.create();
    private static int maxID = 0;

    protected String name;

    public static Poison getPoison(String name)
    {
        return poisons.get(name);
    }

    public static Poison getPoison(int id)
    {
        return poisons.get(getName(id));
    }

    public static String getName(int fluidID)
    {
        return poisonIDs.inverse().get(fluidID);
    }

    public static int getID(String name)
    {
        return poisonIDs.get(name);
    }

    public Poison(String name, int color)
    {
        super(MobEffectCategory.HARMFUL, color);
        this.name = name;
        poisons.put(name, this);
        poisonIDs.put(name, ++maxID);
    }

    public String getName()
    {
        return this.name;
    }

    public final int getID()
    {
        return getID(this.getName());
    }

    /**
     * Called to poison this specific entity with this specific type of poison.
     *
     * @param entity
     * @param amplifier
     * @param emitPosition
     */
    public void poisonEntity(Pos emitPosition, LivingEntity entity, int amplifier)
    {
        if (!isEntityProtected(emitPosition, entity, amplifier))
        {
            doPoisonEntity(emitPosition, entity, amplifier);
        }
    }

    public void poisonEntity(Pos emitPosition, LivingEntity entity)
    {
        this.poisonEntity(emitPosition, entity, 0);
    }

    public boolean isEntityProtected(Pos emitPosition, LivingEntity entity, int amplifier)
    {
        if (entity instanceof Player && ((Player) entity).getAbilities().instabuild)
        {
            return true;
        }
        return false;
    }

    protected abstract void doPoisonEntity(Pos emitPosition, LivingEntity entity, int amplifier);
}
