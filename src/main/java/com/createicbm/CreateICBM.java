package com.createicbm;

import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.entity.phantom.PhantomLogic;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.reg.IExplosiveCustomization;
import com.createicbm.api.reg.events.*;
import com.createicbm.client.ICBMCreativeTab;
import com.createicbm.client.ICBMSounds;
import com.createicbm.command.ICBMCommands;
import com.createicbm.config.*;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.blocks.ConfigBlocks;
import com.createicbm.config.machines.ConfigEmpTower;
import com.createicbm.config.machines.ConfigLauncher;
import com.createicbm.config.machines.ConfigRadar;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.actions.emp.EmpHandler;
import com.createicbm.content.blocks.emptower.TileEMPTower;
import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.blocks.launcher.cruise.TileCruiseLauncher;
import com.createicbm.content.blocks.launcher.screen.TileLauncherScreen;
import com.createicbm.content.blocks.radarstation.TileRadarStation;
import com.createicbm.content.entity.flyingblock.FlyingBlock;
import com.createicbm.content.gas.ProtectiveArmorHandler;
import com.createicbm.content.items.behavior.BombCartDispenseBehavior;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.entity.anti.SAMTargetData;
import com.createicbm.lib.capability.chicken.CapSpaceChicken;
import com.createicbm.content.missile.logic.flight.*;
import com.createicbm.content.missile.logic.flight.move.MoveByFacingLogic;
import com.createicbm.content.missile.logic.flight.move.MoveByVec3Logic;
import com.createicbm.content.missile.logic.targeting.BallisticTargetingData;
import com.createicbm.content.missile.logic.targeting.BasicTargetData;
import com.createicbm.content.potion.ContagiousPoison;
import com.createicbm.content.radioactive.RadioactiveHandler;
import com.createicbm.content.reg.*;
import com.createicbm.lib.actions.ActionSystem;
import com.createicbm.lib.buildable.BuildableObjectRegistry;
import com.createicbm.lib.config.ConfigLoader;
import com.createicbm.lib.energy.system.EnergySystem;
import com.createicbm.lib.energy.system.EnergySystemFE;
import com.createicbm.lib.explosive.reg.*;
import com.createicbm.lib.network.netty.PacketManager;
import com.createicbm.lib.radar.RadarRegistry;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.lib.thread.WorkerThreadManager;
import com.createicbm.lib.tracker.EventTracker;
import com.createicbm.lib.world.ProjectileBlockInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.function.Supplier;

/**
 * @author Dark(DarkGuardsman, Robin).
 * <p>
 * Orginal author and creator of the mod: Calclavia
 */
@Mod(ICBMConstants.DOMAIN)
public class CreateICBM
{
    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static CreateICBM INSTANCE;

    /** Client proxy on the physical client, common proxy on dedicated servers */
    public static CommonProxy proxy;

    public static final int MAP_HEIGHT = 255;

    @Deprecated
    private static final Logger logger = LogManager.getLogger(ICBMConstants.DOMAIN);

    public static final PacketManager packetHandler = new PacketManager(ICBMConstants.DOMAIN);

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ICBMConstants.DOMAIN);
    public static final DeferredHolder<MobEffect, ContagiousPoison> chemicalPotion = EFFECTS.register("chemical", () -> new ContagiousPoison("Chemical", 0, false));
    public static final DeferredHolder<MobEffect, ContagiousPoison> contagiousPotion = EFFECTS.register("contagious", () -> new ContagiousPoison("Contagious", 1, true));

    public static final EventTracker MAIN_TRACKER = new EventTracker();

    /** Set once common setup has run, config reload listeners are ignored before that */
    private static volatile boolean setupDone = false;

    public CreateICBM(IEventBus modBus, ModContainer container)
    {
        INSTANCE = this;
        proxy = createProxy();

        // Configs first
        registerConfigs(container);

        // Content registries
        ComponentReg.register(modBus);
        AttachmentReg.register(modBus);
        BlockReg.register(modBus);
        ItemReg.register(modBus);
        EntityReg.register(modBus);
        MenuReg.register(modBus);
        EFFECTS.register(modBus);
        ICBMSounds.register(modBus);
        ICBMCreativeTab.register(modBus);

        // Packet codexes, ids come from registration order so they must all be registered here, in a fixed order
        TileEMPTower.register();
        TileRadarStation.register();
        TileLauncherBase.register();
        TileLauncherScreen.register();
        TileCruiseLauncher.register();
        EntityMissile.register();
        CapSpaceChicken.register();

        // Mod bus listeners
        modBus.addListener(CapabilityReg::registerCapabilities);
        modBus.addListener(PacketManager::registerPayloads);
        modBus.addListener(ModConfigEvent.Loading.class, ConfigLoader::onConfigEvent);
        modBus.addListener(ModConfigEvent.Reloading.class, ConfigLoader::onConfigEvent);
        modBus.addListener(this::commonSetup);

        // Game bus listeners
        NeoForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        NeoForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        NeoForge.EVENT_BUS.addListener(CreateICBM::registerCommands);
        NeoForge.EVENT_BUS.addListener(LauncherOwner::onBlockPlaced);
        NeoForge.EVENT_BUS.addListener(PhantomLogic::onMobGriefing);
        NeoForge.EVENT_BUS.addListener(CreateICBM::serverStarting);
        NeoForge.EVENT_BUS.addListener(CreateICBM::serverStopping);

        EnergySystem.register(new EnergySystemFE());
        proxy.preInit(modBus);
    }

    private static CommonProxy createProxy()
    {
        if (FMLEnvironment.dist.isClient())
        {
            try
            {
                // Loaded reflectively so the dedicated server never touches client classes
                return (CommonProxy) Class.forName("com.createicbm.client.ClientProxy").getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e)
            {
                throw new RuntimeException("Failed to create ICBM client proxy", e);
            }
        }
        return new CommonProxy();
    }

    private static void registerConfigs(ModContainer container)
    {
        ConfigLoader.register(container, ConfigMain.class);
        ConfigLoader.register(container, ConfigBlast.class);
        ConfigLoader.register(container, ConfigBlocks.class);
        ConfigLoader.register(container, ConfigDebug.class);
        ConfigLoader.register(container, ConfigFlyingBlocks.class);
        ConfigLoader.register(container, ConfigJEI.class);
        ConfigLoader.register(container, ConfigBlueMap.class);
        ConfigLoader.register(container, ConfigThread.class);
        ConfigLoader.register(container, ConfigMissile.class);
        ConfigLoader.register(container, ConfigEmpTower.class);
        ConfigLoader.register(container, ConfigLauncher.class);
        ConfigLoader.register(container, ConfigRadar.class);
        ConfigLoader.register(container, ConfigClient.class);

        ConfigLoader.addReloadListener(() -> {
            if (!setupDone)
            {
                return;
            }
            // NeoForge fires ModConfigEvent.Reloading on its config file-watcher thread (toml edited on disk) or on the
            // client thread (config screen). The handler lists rebuilt here are read by the server thread every tick,
            // so the rebuild is handed to the server thread.
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && !server.isSameThread())
            {
                server.execute(ConfigMain::onConfigReloaded);
            }
            else
            {
                ConfigMain.onConfigReloaded();
            }
        });
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        // Common configs are loaded by now, explosive data reads blast config values
        handleMissileTargetRegistry();
        handleMissileFlightRegistry();
        ActionSystem.setup();
        handleExplosiveCustomizationRegistry();
        handleExRegistry(FMLPaths.CONFIGDIR.get().toFile());

        ProjectileBlockInteraction.register();
        RadioactiveHandler.setup();
        ProtectiveArmorHandler.setup();
        EmpHandler.setup();

        event.enqueueWork(() -> {
            // Dispenser handlers, the vanilla map isn't thread safe
            if (ItemReg.itemBombCart != null)
            {
                DispenserBlock.registerBehavior(ItemReg.itemBombCart.get(), new BombCartDispenseBehavior());
            }

            // Create: stress impacts and item tooltips
            CreateReg.setup();

            // Generate defaults
            FlyingBlock.loadFromConfig();
            setupDone = true;
        });

        proxy.init();
    }

    void handleMissileTargetRegistry()
    {
        CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY = new BuildableObjectRegistry<IMissileTarget>("TARGET_DATA");

        // Default types
        CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY.register(BasicTargetData.REG_NAME, BasicTargetData::new);
        CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY.register(BallisticTargetingData.REG_NAME, BallisticTargetingData::new);
        CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY.register(SAMTargetData.REG_NAME, () -> null); //Can't be restored from save but reserving name

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileTargetRegistryEvent(CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY).lock();
    }

    void handleMissileFlightRegistry()
    {
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = new BuildableObjectRegistry<IMissileFlightLogic>("FLIGHT_LOGIC");

        // Register defaults
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DirectFlightLogic.REG_NAME, DirectFlightLogic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(BallisticFlightLogicOld.REG_NAME, BallisticFlightLogicOld::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(ArcFlightLogic.REG_NAME, ArcFlightLogic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(WarmupFlightLogic.REG_NAME, WarmupFlightLogic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByFacingLogic.REG_NAME, MoveByFacingLogic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByVec3Logic.REG_NAME, MoveByVec3Logic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);
        CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(FollowTargetLogic.REG_NAME, FollowTargetLogic::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileFlightLogicRegistryEvent(CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY).lock();
    }

    void handleExplosiveCustomizationRegistry()
    {
        CreateICBMAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY = new BuildableObjectRegistry<IExplosiveCustomization>("EXPLOSIVE_CUSTOMIZATION");

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ExplosiveCustomizationRegistryEvent(CreateICBMAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) CreateICBMAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY).lock();
    }

    void handleExRegistry(File configMainFolder) //TODO move away from singleton instances for better testing controls
    {
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        CreateICBMAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        CreateICBMAPI.EX_BLOCK_REGISTRY = new ExBlockContentReg();
        CreateICBMAPI.EX_MINECART_REGISTRY = new ExMinecartContentReg();
        CreateICBMAPI.EX_MISSILE_REGISTRY = new ExMissileContentReg();

        //Load data
        if (configMainFolder != null)
        {
            explosiveRegistry.loadReg(new File(configMainFolder, "create_icbm/explosive_reg.json"));
        }

        //Register default content types
        explosiveRegistry.registerContentRegistry(CreateICBMAPI.EX_BLOCK_REGISTRY);
        explosiveRegistry.registerContentRegistry(CreateICBMAPI.EX_MISSILE_REGISTRY);
        explosiveRegistry.registerContentRegistry(CreateICBMAPI.EX_MINECART_REGISTRY);

        //Fire registry events for content types
        NeoForge.EVENT_BUS.post(new ExplosiveContentRegistryEvent(explosiveRegistry));

        //Lock content types, done to prevent errors with adding content
        explosiveRegistry.lockNewContentTypes();

        //Register internal first to reserve slots for backwards compatibility
        ExplosiveInit.init();

        //Fire registry event for explosives
        NeoForge.EVENT_BUS.post(new ExplosiveRegistryEvent(explosiveRegistry));
        explosiveRegistry.lockNewExplosives();

        //Lock all registry, done to prevent errors in data generation for renders and content
        explosiveRegistry.completeLock();

        //Save registry, at this point everything should be registered
        if (configMainFolder != null)
        {
            explosiveRegistry.saveReg();
        }
    }

    public static void registerCommands(RegisterCommandsEvent event)
    {
        ICBMCommands.register(event.getDispatcher());
    }

    public static void serverStarting(ServerStartingEvent event)
    {
        WorkerThreadManager.INSTANCE = new WorkerThreadManager(ConfigThread.THREAD_COUNT);
        WorkerThreadManager.INSTANCE.startThreads();
    }

    public static void serverStopping(ServerStoppingEvent event)
    {
        WorkerThreadManager.INSTANCE.killThreads();
    }

    public static Logger logger()
    {
        return logger;
    }
}
