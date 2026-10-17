package com.createicbm.config.blast.types;

import com.createicbm.lib.config.Config;

public class ConfigAnvil {

    @Config.Name("fragments")
    @Config.Comment("Amount of rotation in yaw and pitch to spawn fragments. total = fragments * fragments")
    @Config.RangeInt(min = 1)
    public int fragments = 25;

    @Config.Name("damage")
    @Config.Comment("Damage to apply to entities")
    @Config.RangeInt(min = 0)
    public int damage = 30;
}
