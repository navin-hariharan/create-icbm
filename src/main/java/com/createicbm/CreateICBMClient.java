package com.createicbm;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client only mod entry point, FML never constructs it on a dedicated server.
 * NeoForge 21.1 only shows a "Config" button in the mods list when the mod registers {@link IConfigScreenFactory},
 * so the generic {@link ConfigurationScreen} is registered here for all ICBM config files.
 */
@Mod(value = ICBMConstants.DOMAIN, dist = Dist.CLIENT)
public class CreateICBMClient
{
    public CreateICBMClient(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
