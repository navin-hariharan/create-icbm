package com.createicbm.config.blast.types;

import com.createicbm.lib.config.Config;

public class ConfigFragment {

    @Config.Name("fragments")
    @Config.Comment("Amount of rotation in yaw and pitch to spawn fragments. total = fragments * fragments")
    @Config.RangeInt(min = 1)
    public int fragments = 15;

    @Config.Name("damage")
    @Config.Comment("Damage to apply to entities")
    @Config.RangeInt(min = 0)
    public int damage = 11;

    @Config.Name("explosive_energy")
    @Config.Comment("Energy of TNT explosion triggered on impact")
    @Config.RangeDouble(min = 0)
    public float explosionSize = 1.5f;
}
