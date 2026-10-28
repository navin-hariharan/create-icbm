package com.createicbm.api.reg;

import com.createicbm.api.EnumTier;
import com.createicbm.api.actions.IActionData;

import javax.annotation.Nonnull;

/**
 * Stores data about an explosive
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/4/19.
 *
 * @deprecated will be replaced by {@link IActionData}
 */
@Deprecated
public interface IExplosiveData extends Comparable<IExplosiveData>, IActionData
{
    /**
     * Assigned ID of the explosive. Is
     * saved to config file and automatically
     * assigned for new explosives.
     *
     * @return ID
     */
    int getRegistryID();

    /**
     * Tier of the explosive.
     *
     * @return tier of the explosive
     */
    @Deprecated
    @Nonnull
    default EnumTier getTier() {
        // Only for legacy explosives, moving forward tier is visual only and has no functionality
        return EnumTier.NONE;
    }
}
