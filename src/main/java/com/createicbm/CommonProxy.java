package com.createicbm;

import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.lib.network.packet.PacketSpawnAirParticle;
import com.createicbm.lib.network.packet.PacketSpawnBlockExplosion;
import com.createicbm.lib.transform.vector.Pos;
import com.createicbm.mods.ModInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;

/**
 * Side split hooks. Replaced on the physical client by {@code com.createicbm.client.ClientProxy}.
 */
public class CommonProxy
{
    /** Called from the mod constructor */
    public void preInit(IEventBus modBus)
    {
        ModInteraction.preInit();
    }

    /** Called during common setup */
    public void init()
    {
        ModInteraction.init();
        ModInteraction.postInit();
    }

    /** Only the client knows about the keyboard, servers never see shift held */
    public boolean isShiftHeld()
    {
        return false;
    }

    public void spawnSmoke(Level world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int age)
    {

    }

    public void spawnAirParticle(Level world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        //TODO allow client settings to sync to server to not received these packets
        PacketSpawnAirParticle.sendToAllClients(world, x, y, z, v, v1, v2, red, green, blue, scale, ticksToLive);
    }

    /**
     * Spawns a vanilla explosion particle when destroying blocks. Server side this will sync a packet per
     * block destroyed. Client side it will spawn 2 particles depending on data.
     *
     * @param world to spawn inside
     * @param sourceX of the blast
     * @param sourceY of the blast
     * @param sourceZ of the blast
     * @param blastScale of the blast
     * @param blockPos of the block being destroyed
     */
    public void spawnExplosionParticles(Level world, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos blockPos)
    {
        //TODO allow client settings to sync to server to not received these packets
        PacketSpawnBlockExplosion.sendToAllClients(world, sourceX, sourceY, sourceZ, blastScale, blockPos);
    }

    public void spawnMissileSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) //TODO refactor to be packet based or wired to each flight logic type
    {

    }

    /** Client only, keeps the missile's looping engine sound going */
    public void onMissileEngineTick(EntityMissile<?> missile)
    {

    }

    public void spawnPadSmoke(Entity entity, IMissileFlightLogic flightLogic, int ticksInAir) {

    }
}
