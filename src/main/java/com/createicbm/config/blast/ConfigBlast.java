package com.createicbm.config.blast;

import com.createicbm.ICBMConstants;
import com.createicbm.config.blast.types.*;
import com.createicbm.lib.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 2/22/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "create_icbm/blast")
@Config.LangKey("config.create_icbm:blast.title")
public class ConfigBlast
{
    @Config.LangKey("config.create_icbm:blast.condensed.title")
    @Config.Comment("Settings for condensed explosive")
    public static ConfigCondensed condensed = new ConfigCondensed();

    @Config.LangKey("config.create_icbm:blast.shrapnel.title")
    @Config.Comment("Settings for shrapnel explosive")
    public static ConfigShrapnel shrapnel = new ConfigShrapnel();

    @Config.LangKey("config.create_icbm:blast.incendiary.title")
    @Config.Comment("Settings for incendiary explosive")
    public static ConfigExBasics incendiary = new ConfigExBasics(14);

    @Config.LangKey("config.create_icbm:blast.debilitation.title")
    @Config.Comment("Settings for debilitation explosive")
    public static ConfigDebilitation debilitation = new ConfigDebilitation();

    @Config.LangKey("config.create_icbm:blast.chemical.title")
    @Config.Comment("Settings for chemical explosive")
    public static ConfigChemical chemical = new ConfigChemical();

    @Config.LangKey("config.create_icbm:blast.anvil.title")
    @Config.Comment("Settings for anvil explosive")
    public static ConfigAnvil anvil = new ConfigAnvil();

    @Config.LangKey("config.create_icbm:blast.repulsive.title")
    @Config.Comment("Settings for repulsive explosive")
    public static ConfigExBasics repulsive = new ConfigExBasics(2);

    @Config.LangKey("config.create_icbm:blast.attractive.title")
    @Config.Comment("Settings for attractive explosive")
    public static ConfigExBasics attractive = new ConfigExBasics(2);

    @Config.LangKey("config.create_icbm:blast.fragmentation.title")
    @Config.Comment("Settings for fragmentation explosive")
    public static ConfigFragment fragmentation = new ConfigFragment();

    @Config.LangKey("config.create_icbm:blast.contagious.title")
    @Config.Comment("Settings for contagious explosive")
    public static ConfigContagious contagious = new ConfigContagious();

    @Config.LangKey("config.create_icbm:blast.sonic.title")
    @Config.Comment("Settings for sonic explosive")
    public static ConfigExBasics sonic = new ConfigExBasics(15);

    @Config.LangKey("config.create_icbm:blast.breaching.title")
    @Config.Comment("Settings for breaching explosive")
    public static ConfigBreaching breaching = new ConfigBreaching();

    @Config.LangKey("config.create_icbm:blast.thermobaric.title")
    @Config.Comment("Settings for thermobaric explosive")
    public static ConfigThermobaric thermobaric = new ConfigThermobaric();

    @Config.LangKey("config.create_icbm:blast.nuclear.title")
    @Config.Comment("Settings for nuclear explosive")
    public static ConfigNuclear nuclear = new ConfigNuclear();

    @Config.LangKey("config.create_icbm:blast.emp.title")
    @Config.Comment("Settings for emp explosive")
    public static ConfigEmp emp = new ConfigEmp();

    @Config.LangKey("config.create_icbm:blast.exothermic.title")
    @Config.Comment("Settings for exothermic explosive")
    public static ConfigExBasics exothermic = new ConfigExBasics(30);

    @Config.LangKey("config.create_icbm:blast.endothermic.title")
    @Config.Comment("Settings for endothermic explosive")
    public static ConfigExBasics endothermic = new ConfigExBasics(30);

    @Config.LangKey("config.create_icbm:blast.antigravitational.title")
    @Config.Comment("Settings for antigravitational explosive")
    public static ConfigExBasics antigravitational = new ConfigExBasics(30);

    @Config.LangKey("config.create_icbm:blast.ender.title")
    @Config.Comment("Settings for ender explosive")
    public static ConfigExBasics ender = new ConfigExBasics(30);

    @Config.LangKey("config.create_icbm:blast.antimatter.title")
    @Config.Comment("Settings for antimatter explosive")
    public static ConfigAntimatter antimatter = new ConfigAntimatter();

    @Config.LangKey("config.create_icbm:blast.redmatter.title")
    @Config.Comment("Set for redmatter blast")
    public static ConfigRedmatter redmatter = new ConfigRedmatter();

    @Config.LangKey("config.create_icbm:blast.colorful.title")
    @Config.Comment("Set for colorful blast")
    public static ConfigExBasics colorful = new ConfigExBasics(10);

    @Config.LangKey("config.create_icbm:blast.flashbang.title")
    @Config.Comment("Settings for the flashbang missile")
    public static ConfigFlashbang flashbang = new ConfigFlashbang();

    @Config.LangKey("config.create_icbm:blast.recon.title")
    @Config.Comment("Settings for the recon missile")
    public static ConfigRecon recon = new ConfigRecon();

    @Config.LangKey("config.create_icbm:blast.phantom.title")
    @Config.Comment("Settings for the phantom enemy missile")
    public static ConfigPhantom phantom = new ConfigPhantom();

    @Config.LangKey("config.create_icbm:blast.darkness.title")
    @Config.Comment("Settings for the darkness missile")
    public static ConfigDarkness darkness = new ConfigDarkness();

    @Config.Name("blast_do_block_updates")
    @Config.Comment("Whether or not the big explosions trigger block updates for all blocks.\nSetting this to false leads to performance improvements, especially when dealing with a lot of water.")
    public static boolean BLAST_DO_BLOCKUPDATES = false;

    @Config.Name("blast_ms_per_tick")
    @Config.Comment("Server time in milliseconds per tick that explosions of missiles, bomb carts and primed explosives may use.\nWhen many go off in the same tick the rest wait for the next ticks instead of freezing the server. 0 runs every explosion right away.")
    @Config.RangeInt(min = 0, max = 1000)
    public static int BLAST_MS_PER_TICK = 15;

    @Config.Name("block_edit_ms_per_tick")
    @Config.Comment("Server time in milliseconds per tick, shared by all dimensions, that big threaded blasts (nuclear, antimatter...) may spend removing and placing blocks.\nLower keeps the tick rate steady, higher finishes big blasts sooner.")
    @Config.RangeInt(min = 1, max = 1000)
    public static int BLOCK_EDIT_MS_PER_TICK = 20;

    @Config.Name("block_effects_per_blast")
    @Config.Comment("Most debris effect packets a TNT style blast (condensed, repulsive, attractive...) sends, it was one per destroyed block. 0 turns the debris effect off.")
    @Config.RangeInt(min = 0, max = 100000)
    public static int BLOCK_EFFECTS_PER_BLAST = 64;

    @Config.Name("block_effects_per_tick")
    @Config.Comment("Most debris effect packets all blasts together send in one tick, the rest are skipped.")
    @Config.RangeInt(min = 0, max = 1000000)
    public static int BLOCK_EFFECTS_PER_TICK = 1024;

    @Deprecated //Move to sub-config
    @Config.Name("allow_day_night_switch")
    @Config.Comment("Allows explosives to change time of day")
    public static boolean ALLOW_DAY_NIGHT = true;

    @Deprecated //Move to each type
    @Config.LangKey("config.create_icbm:blast.fusetimes.title")
    @Config.Comment("Set the time between ignition and explosion seperately for each bomb cart and explosive here.")
    public static ConfigFuseTimes FUSE_TIMES = new ConfigFuseTimes();

}
