package com.createicbm.mods.bluemap;

import com.createicbm.CreateICBM;
import com.createicbm.api.events.MissileEvent;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.config.ConfigBlueMap;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.blocks.mail.UnopenedMail;
import com.createicbm.content.missile.entity.CapabilityMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.entity.delivery.EntityDeliveryMissile;
import com.createicbm.content.missile.tracker.MissileTrackerData;
import com.createicbm.content.missile.tracker.MissileTrackerHandler;
import com.createicbm.content.missile.tracker.MissileTrackerWorld;
import com.createicbm.mods.ModProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shows flying missiles on BlueMap web maps, with who launched them, and a mushroom cloud where they hit. Mail a
 * Delivery Missile left shows as a parcel until it is opened. BlueMap is optional: nothing here touches it unless it is installed, the API calls
 * live in {@link BlueMapMarkers}.
 * <p>
 * Missiles are looked up every {@link ConfigBlueMap#UPDATE_TICKS} on the server thread, including the ones simulated
 * in unloaded chunks, which are placed along their path to the target.
 */
public class BlueMapProxy extends ModProxy
{
    public static final String MOD_ID = "bluemap";

    public static final BlueMapProxy INSTANCE = new BlueMapProxy();

    /** Missiles at most this far from a straight up climb point their icon at the target instead */
    private static final double MIN_HORIZONTAL_SPEED = 0.05;

    /** Name, sender and target per flight id, so simulated missiles keep them */
    private final Map<UUID, Flight> flights = new HashMap<>();

    /** Impact markers still shown, per flight id */
    private final Map<UUID, Impact> impacts = new LinkedHashMap<>();

    private boolean loaded = false;
    private boolean cleared = true;
    private boolean warned = false;
    private int ticks = 0;

    private record Flight(String name, @Nullable String sender, @Nullable Vec3 target)
    {
    }

    private record Impact(ResourceKey<Level> level, double x, double y, double z, String icon, String label, String detail, int until)
    {
    }

    @Override
    public void init()
    {
        if (!isModLoaded(MOD_ID))
        {
            return;
        }
        try
        {
            BlueMapMarkers.register(
                (message, error) -> CreateICBM.logger().warn(message, error),
                () -> text("bluemap.create_icbm.marker_set"),
                () -> ConfigBlueMap.ENABLED
            );
        }
        catch (RuntimeException | LinkageError e)
        {
            // A BlueMap version with an API this was not made for, the game runs on without the markers
            CreateICBM.logger().error("BlueMap found but its API could not be used, missiles won't be shown on its maps", e);
            return;
        }
        loaded = true;
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, this::onServerTick);
        NeoForge.EVENT_BUS.addListener(MissileEvent.Detonated.class, this::onDetonated);
        NeoForge.EVENT_BUS.addListener(MissileEvent.PostImpact.class, this::onImpact);
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, this::onServerStopped);
        CreateICBM.logger().info("BlueMap found, missiles will be shown on its maps");
    }

    /**
     * Runs a marker update so that nothing it throws reaches the server tick or the missile that posted an event. A
     * missing BlueMap class turns the markers off, anything else is logged once and tried again next time.
     */
    private void guard(String what, Runnable action)
    {
        if (!loaded)
        {
            return;
        }
        try
        {
            action.run();
        }
        catch (LinkageError e)
        {
            loaded = false;
            CreateICBM.logger().error("BlueMap: " + what + " failed, missiles won't be shown on its maps anymore", e);
        }
        catch (RuntimeException e)
        {
            if (!warned)
            {
                warned = true;
                CreateICBM.logger().warn("BlueMap: " + what + " failed, further failures are only logged at debug level", e);
            }
            else
            {
                CreateICBM.logger().debug("BlueMap: " + what + " failed", e);
            }
        }
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    private void onServerTick(ServerTickEvent.Post event)
    {
        guard("updating the missile markers", () -> update(event.getServer()));
    }

    private void update(MinecraftServer server)
    {
        if (!ConfigBlueMap.ENABLED || !BlueMapMarkers.isEnabled())
        {
            if (!cleared)
            {
                cleared = true;
                impacts.clear();
                flights.clear();
                BlueMapMarkers.clear();
            }
            return;
        }
        if (++ticks < ConfigBlueMap.UPDATE_TICKS)
        {
            return;
        }
        ticks = 0;
        cleared = false;

        final int now = server.getTickCount();
        impacts.values().removeIf(impact -> impact.until() <= now);

        final Set<UUID> seen = new HashSet<>();
        final List<String> updatedMaps = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels())
        {
            if (!BlueMapMarkers.isMapped(level))
            {
                continue;
            }
            final List<BlueMapMarkers.Pin> pins = new ArrayList<>();
            collectMissiles(level, pins, seen);
            collectSimulated(level, pins, seen);
            for (Map.Entry<UUID, Impact> entry : impacts.entrySet())
            {
                final Impact impact = entry.getValue();
                if (impact.level().equals(level.dimension()))
                {
                    pins.add(new BlueMapMarkers.Pin("impact-" + entry.getKey(), impact.x(), impact.y(), impact.z(),
                        impact.icon(), impact.label(), impact.detail()));
                }
            }
            UnopenedMail.get(level).forEach((pos, sender) -> pins.add(mailPin(pos, sender)));
            updatedMaps.addAll(BlueMapMarkers.show(level, pins));
        }
        // Maps of levels that are gone or no longer mapped
        BlueMapMarkers.clearExcept(updatedMaps);

        // Forget flights that ended, unless their impact is still shown
        flights.keySet().removeIf(id -> !seen.contains(id) && !impacts.containsKey(id));
    }

    /** Missiles flying in loaded chunks */
    private void collectMissiles(ServerLevel level, List<BlueMapMarkers.Pin> pins, Set<UUID> seen)
    {
        for (Entity entity : level.getAllEntities())
        {
            if (entity instanceof EntityMissile<?> missile && missile.isAlive() && missile.getMissileCapability().canRunFlightLogic())
            {
                final CapabilityMissile cap = missile.getMissileCapability();
                final UUID id = cap.getFlightId();
                final Flight flight = flights.computeIfAbsent(id, key -> flightOf(missile));
                seen.add(id);

                final Vec3 motion = missile.getDeltaMovement();
                double dx = motion.x;
                double dz = motion.z;
                if (dx * dx + dz * dz < MIN_HORIZONTAL_SPEED * MIN_HORIZONTAL_SPEED && flight.target() != null)
                {
                    dx = flight.target().x - missile.getX();
                    dz = flight.target().z - missile.getZ();
                }
                pins.add(new BlueMapMarkers.Pin("missile-" + id, missile.getX(), missile.getY(), missile.getZ(),
                    BlueMapMarkers.missileIcon(direction(dx, dz)), flight.name(), flightDetail(flight)));
            }
        }
    }

    /** Missiles simulated in unloaded chunks, placed along the straight line from where they left to the target */
    private void collectSimulated(ServerLevel level, List<BlueMapMarkers.Pin> pins, Set<UUID> seen)
    {
        final MissileTrackerWorld tracker = MissileTrackerHandler.getOrCreateHandler(level, false);
        if (tracker == null)
        {
            return;
        }
        tracker.forEachSimulated(data -> {
            if (!seen.add(data.flightId))
            {
                return;
            }
            final Flight flight = flights.computeIfAbsent(data.flightId, key -> flightOf(level, data));
            final double progress = data.totalTicks > 0 ? Mth.clamp(1 - data.ticksLeftToTarget / (double) data.totalTicks, 0, 1) : 1;
            final double dx = data.targetPos.x() - data.startX;
            final double dz = data.targetPos.z() - data.startZ;
            pins.add(new BlueMapMarkers.Pin("missile-" + data.flightId,
                data.startX + dx * progress, ConfigMissile.SIMULATION_ENTER_HEIGHT, data.startZ + dz * progress,
                BlueMapMarkers.missileIcon(direction(dx, dz)), flight.name(), flightDetail(flight)));
        });
    }

    private void onDetonated(MissileEvent.Detonated event)
    {
        guard("adding an impact marker", () -> addDetonation(event));
    }

    private void addDetonation(MissileEvent.Detonated event)
    {
        if (event.entityMissile instanceof EntityMissile<?> missile && missile.level() instanceof ServerLevel level)
        {
            final Flight flight = flights.computeIfAbsent(missile.getMissileCapability().getFlightId(), key -> flightOf(missile));
            addImpact(level, missile, event.x, event.y, event.z, "impact",
                text("bluemap.create_icbm.impact", flight.name()),
                "<b>" + html(text("bluemap.create_icbm.impact", flight.name())) + "</b><br>"
                    + html(text("bluemap.create_icbm.launched_by", senderText(flight))) + "<br>"
                    + html(text("bluemap.create_icbm.hit_at", Mth.floor(event.x), Mth.floor(event.y), Mth.floor(event.z))));
        }
    }

    private void onImpact(MissileEvent.PostImpact event)
    {
        guard("adding a delivery marker", () -> addDelivery(event));
    }

    private void addDelivery(MissileEvent.PostImpact event)
    {
        // Also posted for hits that do not stop the missile, a delivered missile is gone
        if (event.entityMissile instanceof EntityDeliveryMissile missile && !missile.isAlive() && missile.level() instanceof ServerLevel level)
        {
            if (missile.hasLeftMail())
            {
                // The parcel stays on the map until the mail is opened, see mailPin. Show it on the next tick
                ticks = ConfigBlueMap.UPDATE_TICKS;
                return;
            }
            final Flight flight = flights.computeIfAbsent(missile.getMissileCapability().getFlightId(), key -> flightOf(missile));
            final Vec3 hit = event.hit.getLocation();
            addImpact(level, missile, hit.x, hit.y, hit.z, "parcel",
                text("bluemap.create_icbm.delivered", flight.name()),
                "<b>" + html(text("bluemap.create_icbm.delivered", flight.name())) + "</b><br>"
                    + html(text("bluemap.create_icbm.sent_by", senderText(flight))) + "<br>"
                    + html(text("bluemap.create_icbm.landed_at", Mth.floor(hit.x), Mth.floor(hit.y), Mth.floor(hit.z))));
        }
    }

    private void addImpact(ServerLevel level, EntityMissile<?> missile, double x, double y, double z, String icon, String label, String detail)
    {
        if (!ConfigBlueMap.ENABLED || ConfigBlueMap.IMPACT_SECONDS <= 0 || !BlueMapMarkers.isEnabled())
        {
            return;
        }
        final int until = level.getServer().getTickCount() + ConfigBlueMap.IMPACT_SECONDS * 20;
        impacts.put(missile.getMissileCapability().getFlightId(), new Impact(level.dimension(), x, y, z, icon, label, detail, until));

        // Show it on the next tick rather than waiting for the next update
        ticks = ConfigBlueMap.UPDATE_TICKS;
    }

    private void onServerStopped(ServerStoppedEvent event)
    {
        flights.clear();
        impacts.clear();
        cleared = true;
        ticks = 0;
    }

    private static Flight flightOf(EntityMissile<?> missile)
    {
        final CapabilityMissile cap = missile.getMissileCapability();
        final LauncherOwner sender = cap.getSender();
        final IMissileTarget target = cap.getTargetData();
        return new Flight(missile.getName().getString(),
            sender != null && !sender.isEmpty() ? sender.name() : null,
            target != null ? target.getPosition() : null);
    }

    /** A simulated missile has no entity, one is made from its saved data to read its name and sender */
    private static Flight flightOf(ServerLevel level, MissileTrackerData data)
    {
        final Vec3 target = new Vec3(data.targetPos.x(), data.targetPos.y(), data.targetPos.z());
        try
        {
            final Entity entity = EntityType.create(data.missileData, level).orElse(null);
            if (entity instanceof EntityMissile<?> missile)
            {
                final Flight flight = flightOf(missile);
                return new Flight(flight.name(), flight.sender(), target);
            }
        }
        catch (RuntimeException e)
        {
            CreateICBM.logger().debug("BlueMap: could not read simulated missile " + data.flightId, e);
        }
        return new Flight(text("bluemap.create_icbm.missile"), null, target);
    }

    /** Mail nobody has opened yet, shown until someone does */
    private static BlueMapMarkers.Pin mailPin(BlockPos pos, String sender)
    {
        final String label = text("bluemap.create_icbm.mail", sender.isEmpty() ? text("bluemap.create_icbm.unknown_sender") : sender);
        final String detail = "<b>" + html(label) + "</b><br>"
            + html(text("bluemap.create_icbm.mail_waiting", pos.getX(), pos.getY(), pos.getZ()));
        return new BlueMapMarkers.Pin("mail-" + pos.asLong(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            "parcel", label, detail);
    }

    private static String flightDetail(Flight flight)
    {
        final StringBuilder html = new StringBuilder();
        html.append("<b>").append(html(flight.name())).append("</b><br>");
        html.append(html(text("bluemap.create_icbm.launched_by", senderText(flight))));
        if (flight.target() != null)
        {
            html.append("<br>").append(html(text("bluemap.create_icbm.target",
                Mth.floor(flight.target().x), Mth.floor(flight.target().y), Mth.floor(flight.target().z))));
        }
        return html.toString();
    }

    private static String senderText(Flight flight)
    {
        return flight.sender() != null ? flight.sender() : text("bluemap.create_icbm.unknown_sender");
    }

    /**
     * @return one of 16 directions, 0 is north (-z) and they turn clockwise like on the map
     */
    private static int direction(double dx, double dz)
    {
        final double degrees = Math.toDegrees(Math.atan2(dx, -dz));
        return Math.floorMod((int) Math.round(degrees / 22.5), 16);
    }

    private static String text(String key, Object... args)
    {
        return Component.translatable(key, args).getString();
    }

    private static String html(String text)
    {
        final StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++)
        {
            final char c = text.charAt(i);
            switch (c)
            {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '&' -> out.append("&amp;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}
