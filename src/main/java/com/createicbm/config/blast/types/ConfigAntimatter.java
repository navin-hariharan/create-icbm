package com.createicbm.config.blast.types;

import com.createicbm.lib.config.Config;

public class ConfigAntimatter {

    @Config.Name("break_unbreakable")
    @Config.Comment("Should antimatter ignore hardness checks for unbreakable blocks (bedrock, wards)")
    public boolean damageUnbreakable = true;

    @Config.Name("damage_on_redmatter_kill")
    @Config.Comment("Whether or not antimatter damages blocks/entities when detonating and killing redmatter")
    public boolean damageOnRedmatterKill = false;

    @Config.Comment("Size (meters) of the blast")
    @Config.Name("size")
    @Config.RangeInt(min = 1)
    public int size = 55;

    @Config.Comment("Damage to apply to entities. Is applied at start and end of blast.")
    @Config.Name("damage")
    @Config.RangeDouble(min = 1)
    public float damage = Integer.MAX_VALUE;

    @Config.Name("flash_extra_range")
    @Config.Comment("Players up to this many meters outside of the blast are blinded by the flash too. 0 turns the flash off.")
    @Config.RangeInt(min = 0, max = 1000)
    public int flashExtraRange = 100;

    @Config.Name("flash_ticks")
    @Config.Comment("Ticks the screen stays fully white for a player at the center, it is shorter further away and then fades out")
    @Config.RangeInt(min = 1, max = 600)
    public int flashTicks = 60;

    @Config.Name("mushroom_cloud")
    @Config.Comment("Raises a mushroom cloud above the blast, drawn on the client of every player nearby")
    public boolean mushroomCloud = true;

    @Config.Name("mushroom_cloud_range")
    @Config.Comment("Players within this many meters of the blast see the mushroom cloud")
    @Config.RangeInt(min = 16, max = 2000)
    public int mushroomCloudRange = 512;

    @Config.Name("edit_ticks")
    @Config.Comment("Ticks the block removal is spread over so the crater opens up smoothly instead of in a few lumps. 0 removes blocks as fast as the block edit budget allows.")
    @Config.RangeInt(min = 0, max = 1200)
    public int editTicks = 120;
}
