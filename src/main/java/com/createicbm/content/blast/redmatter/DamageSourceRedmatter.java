package com.createicbm.content.blast.redmatter;

import com.createicbm.content.blast.ICBMDamageTypes;
import com.createicbm.content.blast.redmatter.logic.RedmatterLogic;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Damage sources are data driven in 1.21, this now builds one from {@link ICBMDamageTypes#REDMATTER}
 * (armor/effect/enchant bypass comes from the damage type tags). True source and damage location are the redmatter
 * entity.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 5/23/2020.
 */
public final class DamageSourceRedmatter
{
    private DamageSourceRedmatter()
    {
    }

    public static DamageSource create(Level level, RedmatterLogic blastRedmatter)
    {
        final Entity host = blastRedmatter != null ? blastRedmatter.host : null; //TODO see if we can get trigger source
        return ICBMDamageTypes.source(level, ICBMDamageTypes.REDMATTER, host, host);
    }

    //TODO add handling for death messages
}
