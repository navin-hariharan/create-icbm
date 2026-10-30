package com.createicbm.client.create;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.createicbm.ICBMConstants;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.content.reg.ItemReg;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Ponder scenes (hold W over an item) for the shaft driven machines and for arming missiles and bomb carts. Schematics live in
 * {@code assets/create_icbm/ponder/}, scene text in the lang file under {@code create_icbm.ponder.*}.
 */
public class ICBMPonderPlugin implements PonderPlugin
{
    @Override
    public String getModId()
    {
        return ICBMConstants.DOMAIN;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper)
    {
        helper.addStoryBoard(ItemReg.itemLaunchBase.getId(), "launch_pad", ICBMPonderScenes::launchPad);
        helper.addStoryBoard(ItemReg.itemLaunchScreen.getId(), "launch_pad", ICBMPonderScenes::launchPad);
        helper.addStoryBoard(ItemReg.itemLaunchSupport.getId(), "launch_pad", ICBMPonderScenes::launchPad);
        helper.addStoryBoard(ItemReg.itemLaunchConnector.getId(), "launch_pad", ICBMPonderScenes::launchPad);
        helper.addStoryBoard(ItemReg.itemCruiseLauncher.getId(), "cruise_turret", ICBMPonderScenes::cruiseTurret);
        helper.addStoryBoard(ItemReg.itemRadarStation.getId(), "radar_station", ICBMPonderScenes::radarStation);
        helper.addStoryBoard(ItemReg.itemAdvancedRadar.getId(), "radar_station", ICBMPonderScenes::radarStation);

        // Warheads are deployed onto missile modules and minecarts
        helper.addStoryBoard(ItemReg.itemExplosiveMissile.getId(), "arming", ICBMPonderScenes::arming);
        helper.addStoryBoard(ItemReg.itemBlockExplosive.getId(), "arming", ICBMPonderScenes::arming);
        helper.addStoryBoard(ItemReg.itemBombCart.getId(), "arming", ICBMPonderScenes::arming);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper)
    {
        // List the machines next to Create's own under "Kinetic Appliances"
        helper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
            .add(BlockReg.blockLaunchBase.getId())
            .add(BlockReg.blockCruiseLauncher.getId())
            .add(BlockReg.blockRadarStation.getId())
            .add(BlockReg.blockAdvancedRadar.getId());
    }
}
