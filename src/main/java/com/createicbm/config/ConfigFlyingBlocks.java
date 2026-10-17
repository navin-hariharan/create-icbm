package com.createicbm.config;

import com.createicbm.ICBMConstants;
import com.createicbm.lib.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/flying_blocks")
@Config.LangKey("config.create_icbm:flying_blocks")
public class ConfigFlyingBlocks
{
    @Config.Name("enable")
    @Config.Comment("Enables flying blocks, set to false to prevent additional usage in blasts. Doesn't remove existing or prevent other mods from spawning more.")
    public static boolean enabled = true;

    //@Config.Name("block_replacements") TODO implement
    //public String[] replacements = new String[]{"minecraft:water | minecraft:ice"};

    @Config.Name("block_ban_allow")
    public static BanList banAllow = new BanList();

    @Config.Name("damage_scale")
    @Config.Comment("Damage to apply, scaled by velocity")
    public static float damageScale = 2f;

    public static class BanList {

        @Config.Name("ban")
        @Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean ban = true;

        @Config.Name("list")
        @Config.Comment("Block/BlastState names, as mod:block or mod:block[property:value]")
        public String[] blockStates = new String[]{"minecraft:fire"};
    }
}
