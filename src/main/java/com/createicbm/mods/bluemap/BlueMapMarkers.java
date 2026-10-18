package com.createicbm.mods.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Everything that touches the BlueMap API. Only loaded when BlueMap is installed, see {@link BlueMapProxy}, and only
 * takes plain values so the game side never loads a BlueMap class.
 * <p>
 * BlueMap calls {@link #onEnable(BlueMapAPI)} on its own thread, the markers are updated from the server thread.
 * Marker sets and their marker maps are concurrent, markers are replaced rather than changed.
 * <p>
 * This only adds a marker set to each map and puts the icons in the map's asset storage. It never touches BlueMap's
 * tiles, textures or settings.
 */
final class BlueMapMarkers
{
    static final String MARKER_SET_ID = "create_icbm_missiles";

    /** Folder of the icons in each map's asset storage and in this mod's jar */
    private static final String ASSET_FOLDER = "create_icbm/";
    private static final String ICON_RESOURCES = "/assets/create_icbm/bluemap/";

    /** How often each map's icons are checked, a purge deletes them along with the rest of the map */
    private static final long ICON_CHECK_MILLIS = 60_000;

    /**
     * Web page script an earlier build of this mod wrote into BlueMap's web root, relative to it. It is deleted, BlueMap
     * already drops it from its settings when it reloads since nothing registers it anymore.
     */
    private static final String OLD_SCRIPT = "assets/create_icbm/missile-refresh.js";

    /** Icon name to the pixel that sits on the marker position */
    private static final Map<String, int[]> ICONS = new LinkedHashMap<>();

    static
    {
        for (int i = 0; i < 16; i++)
        {
            ICONS.put(missileIcon(i), new int[]{16, 16});
        }
        ICONS.put("impact", new int[]{24, 41});
        ICONS.put("parcel", new int[]{16, 16});
    }

    /**
     * A marker to show
     *
     * @param id     - marker id, unique in the marker set
     * @param icon   - icon name, see {@link #ICONS}
     * @param label  - shown in BlueMap's marker list
     * @param detail - html shown when the marker is clicked
     */
    record Pin(String id, double x, double y, double z, String icon, String label, String detail)
    {
    }

    private static volatile BlueMapAPI api;

    /** Our marker set per map id */
    private static final Map<String, MarkerSet> SETS = new ConcurrentHashMap<>();

    /** Icon urls per map id and icon name */
    private static final Map<String, String> ICON_URLS = new ConcurrentHashMap<>();

    /** When the icons of each map id were last checked */
    private static final Map<String, Long> ICONS_CHECKED = new ConcurrentHashMap<>();

    private static BiConsumer<String, Throwable> warn = (message, error) -> {};
    private static Supplier<String> setLabel = () -> "Missiles";
    private static BooleanSupplier enabled = () -> true;

    private BlueMapMarkers()
    {
    }

    /**
     * @param warn     - logs a warning
     * @param setLabel - name of the marker set shown on the map
     * @param enabled  - whether markers should be shown at all
     */
    static void register(BiConsumer<String, Throwable> warn, Supplier<String> setLabel, BooleanSupplier enabled)
    {
        BlueMapMarkers.warn = warn;
        BlueMapMarkers.setLabel = setLabel;
        BlueMapMarkers.enabled = enabled;
        BlueMapAPI.onEnable(BlueMapMarkers::onEnable);
        BlueMapAPI.onDisable(BlueMapMarkers::onDisable);
    }

    static String missileIcon(int direction)
    {
        return String.format("missile_%02d", direction);
    }

    static boolean isEnabled()
    {
        return api != null;
    }

    private static void onEnable(BlueMapAPI blueMap)
    {
        // Nothing may escape into BlueMap, it only catches exceptions and an error would stop it from loading
        try
        {
            resetState();
            removeOldScript(blueMap);
            if (enabled.getAsBoolean())
            {
                for (BlueMapMap map : blueMap.getMaps())
                {
                    setFor(map);
                    checkIcons(map);
                }
            }
            api = blueMap;
        }
        catch (RuntimeException | LinkageError e)
        {
            warn.accept("BlueMap: could not set up the missile markers, missiles won't be shown on the map", e);
        }
    }

    private static void onDisable(BlueMapAPI blueMap)
    {
        api = null;
        try
        {
            // BlueMap saves the markers of every map right after this, flying missiles would stay on the page
            for (BlueMapMap map : blueMap.getMaps())
            {
                map.getMarkerSets().remove(MARKER_SET_ID);
            }
        }
        catch (RuntimeException | LinkageError e)
        {
            warn.accept("BlueMap: could not remove the missile markers", e);
        }
        finally
        {
            resetState();
        }
    }

    private static void resetState()
    {
        SETS.clear();
        ICON_URLS.clear();
        ICONS_CHECKED.clear();
    }

    /** Our marker set on the map, added the first time it is needed */
    private static MarkerSet setFor(BlueMapMap map)
    {
        return SETS.computeIfAbsent(map.getId(), id -> {
            final MarkerSet set = MarkerSet.builder()
                .label(setLabel.get())
                .toggleable(true)
                .defaultHidden(false)
                .build();
            map.getMarkerSets().put(MARKER_SET_ID, set);
            return set;
        });
    }

    /** Writes the icons on first use, and again when a purge deleted them */
    private static void checkIcons(BlueMapMap map)
    {
        final long now = System.currentTimeMillis();
        final Long last = ICONS_CHECKED.get(map.getId());
        if (last != null && now - last < ICON_CHECK_MILLIS)
        {
            return;
        }
        ICONS_CHECKED.put(map.getId(), now);
        try
        {
            if (last == null || !map.getAssetStorage().assetExists(ASSET_FOLDER + "impact.png"))
            {
                writeIcons(map);
            }
        }
        catch (IOException e)
        {
            warn.accept("BlueMap: could not check the icons of map " + map.getId(), e);
        }
    }

    /** Copies the icons into the map's storage, where the web page loads them from */
    private static void writeIcons(BlueMapMap map)
    {
        for (String icon : ICONS.keySet())
        {
            final String asset = ASSET_FOLDER + icon + ".png";
            try (InputStream in = BlueMapMarkers.class.getResourceAsStream(ICON_RESOURCES + icon + ".png"))
            {
                if (in == null)
                {
                    warn.accept("BlueMap: missing icon " + icon, null);
                    continue;
                }
                try (OutputStream out = map.getAssetStorage().writeAsset(asset))
                {
                    in.transferTo(out);
                }
                ICON_URLS.put(map.getId() + "|" + icon, map.getAssetStorage().getAssetUrl(asset));
            }
            catch (IOException | RuntimeException e)
            {
                warn.accept("BlueMap: could not write icon " + icon + " for map " + map.getId(), e);
            }
        }
    }

    /** Deletes the web page script an earlier build wrote, see {@link #OLD_SCRIPT} */
    private static void removeOldScript(BlueMapAPI blueMap)
    {
        try
        {
            final Path file = blueMap.getWebApp().getWebRoot().resolve(OLD_SCRIPT);
            if (Files.deleteIfExists(file))
            {
                final Path folder = file.getParent();
                try (var rest = Files.list(folder))
                {
                    if (rest.findAny().isEmpty())
                    {
                        Files.delete(folder);
                    }
                }
            }
        }
        catch (IOException | RuntimeException e)
        {
            warn.accept("BlueMap: could not delete " + OLD_SCRIPT + " from BlueMap's web folder, it can be deleted by hand", e);
        }
    }

    /**
     * Replaces our markers on every map of the level's world
     *
     * @param level - the server level, as BlueMap knows it
     * @param pins  - every marker that should be shown, markers not in here are removed
     * @return ids of the maps that were updated, empty when BlueMap is off or does not map this level
     */
    static List<String> show(Object level, Collection<Pin> pins)
    {
        final BlueMapAPI blueMap = api;
        if (blueMap == null)
        {
            return List.of();
        }
        final Optional<BlueMapWorld> world = blueMap.getWorld(level);
        if (world.isEmpty())
        {
            return List.of();
        }
        final Set<String> ids = new HashSet<>();
        for (Pin pin : pins)
        {
            ids.add(pin.id());
        }
        final List<String> updated = new ArrayList<>();
        for (BlueMapMap map : world.get().getMaps())
        {
            final MarkerSet set = pins.isEmpty() ? SETS.get(map.getId()) : setFor(map);
            if (set == null)
            {
                continue;
            }
            if (!pins.isEmpty())
            {
                checkIcons(map);
            }
            final Map<String, Marker> markers = set.getMarkers();
            for (Pin pin : pins)
            {
                markers.put(pin.id(), toMarker(map, pin));
            }
            markers.keySet().retainAll(ids);
            updated.add(map.getId());
        }
        return updated;
    }

    /**
     * @return true when BlueMap is on and has a map of the level
     */
    static boolean isMapped(Object level)
    {
        final BlueMapAPI blueMap = api;
        return blueMap != null && blueMap.getWorld(level).map(world -> !world.getMaps().isEmpty()).orElse(false);
    }

    /** Removes our markers from every map not in the given ids, such as the maps of a level that was unloaded */
    static void clearExcept(Collection<String> mapIds)
    {
        SETS.forEach((id, set) -> {
            if (!mapIds.contains(id))
            {
                set.getMarkers().clear();
            }
        });
    }

    /** Removes our marker sets from every map, used when markers are turned off */
    static void clear()
    {
        final BlueMapAPI blueMap = api;
        if (blueMap != null)
        {
            for (BlueMapMap map : blueMap.getMaps())
            {
                map.getMarkerSets().remove(MARKER_SET_ID);
            }
        }
        resetState();
    }

    private static POIMarker toMarker(BlueMapMap map, Pin pin)
    {
        final int[] anchor = ICONS.getOrDefault(pin.icon(), new int[]{16, 16});
        final String url = ICON_URLS.get(map.getId() + "|" + pin.icon());
        final POIMarker.Builder builder = POIMarker.builder()
            .label(pin.label())
            .detail(pin.detail())
            .position(pin.x(), pin.y(), pin.z());
        if (url != null)
        {
            builder.icon(url, anchor[0], anchor[1]);
        }
        return builder.build();
    }
}
