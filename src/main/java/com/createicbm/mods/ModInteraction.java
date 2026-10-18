package com.createicbm.mods;

import com.createicbm.mods.bluemap.BlueMapProxy;
import com.createicbm.mods.mekanism.MekProxy;

public class ModInteraction {

    public static void preInit() {
        MekProxy.INSTANCE.preInit();
        BlueMapProxy.INSTANCE.preInit();
    }

    public static void init() {
        MekProxy.INSTANCE.init();
        BlueMapProxy.INSTANCE.init();
    }

    public static void postInit() {
        MekProxy.INSTANCE.postInit();
        BlueMapProxy.INSTANCE.postInit();
    }
}
