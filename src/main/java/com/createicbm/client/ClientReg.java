package com.createicbm.client;

import com.createicbm.client.fx.EnderAbductionFx;
import com.createicbm.client.fx.MushroomCloudFx;
import com.createicbm.client.fx.ScreenFlash;
import com.createicbm.client.sound.MissileEngineSound;
import com.createicbm.client.sound.TrackedSounds;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.mail.TESRMail;
import com.createicbm.client.mapper.ExplosiveModels;
import com.createicbm.client.models.ModelChickenHelmet;
import com.createicbm.client.models.CruiseLauncherTopModel;
import com.createicbm.client.render.entity.RenderBombCart;
import com.createicbm.client.render.entity.RenderExBlock;
import com.createicbm.client.render.entity.RenderExplosion;
import com.createicbm.client.render.entity.RenderFragments;
import com.createicbm.client.render.entity.RenderLightBeam;
import com.createicbm.client.render.entity.RenderMissile;
import com.createicbm.client.render.entity.RenderSeat;
import com.createicbm.client.render.entity.RenderSmoke;
import com.createicbm.client.render.entity.layer.LayerChickenHelmet;
import com.createicbm.content.blast.redmatter.render.RenderRedmatter;
import com.createicbm.client.create.ICBMPartialModels;
import com.createicbm.client.create.ICBMPonderPlugin;
import com.createicbm.client.create.ShaftVisuals;
import com.createicbm.content.blocks.radarstation.RadarStationRenderer;
import com.createicbm.content.blocks.launcher.base.TESRLauncherBase;
import com.createicbm.content.blocks.launcher.base.gui.GuiLauncherBase;
import com.createicbm.content.blocks.launcher.cruise.TESRCruiseLauncher;
import com.createicbm.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import com.createicbm.content.blocks.launcher.screen.gui.GuiLauncherScreen;
import com.createicbm.content.blocks.radarstation.gui.GuiRadarStation;
import com.createicbm.content.missile.entity.delivery.gui.GuiDeliveryMissile;
import com.createicbm.content.entity.flyingblock.RenderEntityBlock;
import com.createicbm.content.items.ItemRocketLauncher;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.content.reg.MenuReg;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.createmod.ponder.foundation.PonderIndex;

/**
 * Client registration, all listeners are on the mod bus and added by {@link ClientProxy#preInit(IEventBus)}.
 * Crafting items are flattened so they need no custom model mapping anymore.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class ClientReg
{
    private ClientReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        // Create: partial models must exist before models load
        ICBMPartialModels.init();
        PonderIndex.addPlugin(new ICBMPonderPlugin());
        modBus.addListener(ClientReg::registerEntityRenders);
        modBus.addListener(ClientReg::registerLayerDefinitions);
        modBus.addListener(ClientReg::addLayers);
        modBus.addListener(ClientReg::registerScreens);
        modBus.addListener(ClientReg::registerGuiLayers);
        // The HUD is drawn under open screens, so the flash is drawn over them again
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, event -> ScreenFlash.render(event.getGuiGraphics()));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> MissileEngineSound.onClientTick());
        // Ender missile cutscene
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> EnderAbductionFx.onClientTick());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> MushroomCloudFx.onClientTick());
        NeoForge.EVENT_BUS.addListener(ViewportEvent.ComputeCameraAngles.class, EnderAbductionFx::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(ViewportEvent.ComputeFov.class, EnderAbductionFx::onFov);
        NeoForge.EVENT_BUS.addListener(MovementInputUpdateEvent.class, EnderAbductionFx::onMovementInput);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            MissileEngineSound.stopAll();
            TrackedSounds.clear();
            EnderAbductionFx.clear();
            MushroomCloudFx.clear();
            ScreenFlash.clear();
        });
        modBus.addListener(ClientReg::clientSetup);
        modBus.addListener(ExplosiveModels::onRegisterAdditional);
        modBus.addListener(ExplosiveModels::onModifyBakingResult);
    }

    // ------------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // Item properties
    // ------------------------------------------------------------------

    public static void clientSetup(FMLClientSetupEvent event)
    {
        // Create: Flywheel visuals for the machine input shafts
        ShaftVisuals.register();

        // ItemProperties isn't thread safe
        event.enqueueWork(() -> {
            registerPulling(ItemReg.itemRocketLauncher.get());
            registerPulling(ItemReg.itemBallisticLauncher.get());
        });
    }

    private static void registerPulling(ItemRocketLauncher item)
    {
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
            (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    // ------------------------------------------------------------------
    // Entity and block entity renders
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityReg.BLOCK_EXPLOSIVE.get(), RenderExBlock::new);
        event.registerEntityRenderer(EntityReg.REDMATTER.get(), RenderRedmatter::new);
        event.registerEntityRenderer(EntityReg.BLOCK_GRAVITY.get(), RenderEntityBlock::new);
        event.registerEntityRenderer(EntityReg.EXPLOSION.get(), RenderExplosion::new);
        event.registerEntityRenderer(EntityReg.BEAM.get(), RenderLightBeam::new);
        event.registerEntityRenderer(EntityReg.BLOCK_FRAGMENT.get(), RenderFragments::new);
        event.registerEntityRenderer(EntityReg.MISSILE_SEAT.get(), RenderSeat::new);
        event.registerEntityRenderer(EntityReg.SMOKE.get(), RenderSmoke::new);
        event.registerEntityRenderer(EntityReg.BOMB_CART.get(), RenderBombCart::new);

        // Missiles, RenderMissile sets RenderMissile.INSTANCE in its constructor
        event.registerEntityRenderer(EntityReg.MISSILE_EXPLOSIVE.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_GENERIC.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_SAM.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_DELIVERY.get(), RenderMissile::new);

        // Phantom mobs look exactly like the real ones
        event.registerEntityRenderer(EntityReg.PHANTOM_ZOMBIE.get(), ZombieRenderer::new);
        event.registerEntityRenderer(EntityReg.PHANTOM_HUSK.get(), HuskRenderer::new);
        event.registerEntityRenderer(EntityReg.PHANTOM_SPIDER.get(), SpiderRenderer::new);
        event.registerEntityRenderer(EntityReg.PHANTOM_CREEPER.get(), CreeperRenderer::new);

        // Block entities, raw types as some renders are shared between block entity types
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_LAUNCHER_BASE.get(), ctx -> new TESRLauncherBase(ctx));
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_CRUISE_LAUNCHER.get(), ctx -> new TESRCruiseLauncher(ctx));
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_RADAR_STATION.get(), ctx -> new RadarStationRenderer(ctx));
        event.registerBlockEntityRenderer(BlockReg.TILE_MAIL.get(), TESRMail::new);
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ModelChickenHelmet.LAYER, ModelChickenHelmet::createLayer);
        event.registerLayerDefinition(CruiseLauncherTopModel.LAYER, CruiseLauncherTopModel::createLayer);
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event)
    {
        final EntityRenderer<?> renderer = event.getRenderer(EntityType.CHICKEN);
        if (renderer instanceof ChickenRenderer chickenRenderer)
        {
            chickenRenderer.addLayer(new LayerChickenHelmet(chickenRenderer, event.getEntityModels()));
        }
    }

    // ------------------------------------------------------------------
    // Screens
    // ------------------------------------------------------------------

    public static void registerGuiLayers(RegisterGuiLayersEvent event)
    {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "flashbang"), ScreenFlash::render);
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "ender_abduction"), EnderAbductionFx::render);
    }

    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(MenuReg.LAUNCHER_BASE.get(), GuiLauncherBase::new);
        event.register(MenuReg.CRUISE_LAUNCHER.get(), GuiCruiseLauncher::new);
        event.register(MenuReg.LAUNCHER_SCREEN.get(), GuiLauncherScreen::new);
        event.register(MenuReg.RADAR_STATION.get(), GuiRadarStation::new);
        event.register(MenuReg.DELIVERY_MISSILE.get(), GuiDeliveryMissile::new);
    }
}
