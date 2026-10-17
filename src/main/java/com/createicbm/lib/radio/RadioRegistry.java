package com.createicbm.lib.radio;

import com.createicbm.CreateICBM;
import com.createicbm.api.data.IBoundBox;
import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.lib.data.BoundBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;

/**
 * Map based system for radio waves being transmitted in an area
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 4/20/2016.
 */
public final class RadioRegistry
{
    /** Used only for event calls */
    public static final RadioRegistry INSTANCE = new RadioRegistry();

    /** Used to indicate the object has full map radio range */
    public static final IBoundBox<BlockPos> INFINITE = new BoundBlockPos(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    /** Level id to radio maps */
    private static final HashMap<ResourceKey<Level>, RadioMap> RADIO_MAPS = new HashMap();

    public static String EMPTY_HZ = "";

    /**
     * Adds an entity to the map
     *
     * @param receiver - entity
     * @return true if added
     */
    public static boolean add(IRadioReceiver receiver)
    {
        if(receiver.getWorld() == null)
        {
            return false;
        }
        return getRadarMapForDim(receiver.getWorld().dimension()).add(receiver);
    }

    /**
     * Removes an entity from the map
     *
     * @param tile - entity
     * @return true if removed
     */
    public static boolean remove(IRadioReceiver tile)
    {
        if(tile.getWorld() == null)
        {
            return false;
        }
        if (RADIO_MAPS.containsKey(tile.getWorld().dimension()))
        {
            RadioMap map = getRadarMapForDim(tile.getWorld().dimension());
            return map.remove(tile);
        }
        return false;
    }

    /**
     * Called to send a message over the network
     *
     * @param sender posting message
     * @param packet containing message
     */
    public static void popMessage(Level world, IRadioSender sender, IRadioMessage packet)
    {
        if(world == null || world.isClientSide()) {
            CreateICBM.logger().error("RadarRegistry: Invalid world : " + world, new RuntimeException());
            return;
        }
        if (RADIO_MAPS.containsKey(world.dimension()))
        {
            RadioMap map = getRadarMapForDim(world.dimension());
            map.popMessage(sender, packet);
        }
    }

    /**
     * Gets a radio map for a dimension
     *
     * @param dimID - dimension key
     * @return existing mpa, or new map if one does not exist
     */
    public static RadioMap getRadarMapForDim(ResourceKey<Level> dimID)
    {
        if (!RADIO_MAPS.containsKey(dimID))
        {
            RadioMap map = new RadioMap(dimID);
            RADIO_MAPS.put(dimID, map);
            return map;
        }
        return RADIO_MAPS.get(dimID);
    }

    @SubscribeEvent
    public void worldUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide())
        {
            final ResourceKey<Level> dim = level.dimension();
            if (RADIO_MAPS.containsKey(dim))
            {
                getRadarMapForDim(dim).unloadAll();
                RADIO_MAPS.remove(dim);
            }
        }
    }
}
