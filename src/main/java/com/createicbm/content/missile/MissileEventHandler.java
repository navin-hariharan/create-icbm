package com.createicbm.content.missile;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.events.MissileRideEvent;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.tracker.ISimulatedFlight;
import com.createicbm.content.missile.tracker.MissileTrackerHandler;
import com.createicbm.lib.radar.RadarMap;
import com.createicbm.lib.radar.RadarRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 8/4/2019.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileEventHandler
{
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event)
    {
        if (event.isDismounting()
                && event.getEntityBeingMounted() != null
                && event.getEntityMounting() instanceof Player)
        {
            IMissile missile = event.getEntityBeingMounted().getCapability(CreateICBMAPI.MISSILE_CAPABILITY);
            if(missile != null)
            {
                event.setCanceled(NeoForge.EVENT_BUS.post(new MissileRideEvent.Stop(missile, (Player) event.getEntityMounting())).isCanceled());
            }
        }
    }

    // In 1.21 entities are unloaded (setRemoved UNLOADED_TO_CHUNK) well before ChunkEvent.Unload fires, so missiles
    // found here are usually no longer alive and simulateMissile skips them. Flight logics now simulate before entering
    // a chunk where entities do not tick, see MissileTrackerHandler#canEntitiesTick
    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event)
    {
        if (event.getLevel() instanceof Level world && !world.isClientSide())
        {
            final ChunkPos chunk = event.getChunk().getPos();
            final RadarMap map = RadarRegistry.getRadarMapForWorld(world);
            if (map != null)
            {
                // Collect missiles we are about to unload, using list to avoid concurrent mod from radar remove TODO have radar system track removals in list and apply next tick
                final List<EntityMissile<?>> unloading = new LinkedList();
                map.collectEntitiesInChunk(chunk.x, chunk.z, (radarEntity -> {
                    if (MissileTrackerHandler.canSimulate(radarEntity.entity))
                    {
                        unloading.add((EntityMissile<?>) radarEntity.entity);
                    }
                }));

                unloading.stream()
                    .filter(missile -> missile.isAlive() && missile.getMissileCapability().getFlightLogic() instanceof ISimulatedFlight)
                    .forEach(MissileTrackerHandler::simulateMissile);
            }
        }
    }
}
