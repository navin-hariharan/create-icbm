package com.createicbm.content.blast;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.actions.status.ImmutableStatus;
import net.minecraft.resources.ResourceLocation;

@Deprecated
public class BlastStatus {
    public static ImmutableStatus SETUP_ERROR = ImmutableStatus.error(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "blast.setup.error"));
}
