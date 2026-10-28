package com.createicbm.lib.network.packet;

import com.createicbm.CreateICBM;
import com.createicbm.client.fx.MushroomCloudFx;
import com.createicbm.lib.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Starts a mushroom cloud on the clients of nearby players. Only this small packet is sent, every client draws the
 * cloud itself, so the size of the cloud costs the server and the network nothing.
 */
public class PacketMushroomCloud implements IPacket<PacketMushroomCloud>
{
    private double x;
    private double y;
    private double z;
    private float radius;
    private int ticks;

    public PacketMushroomCloud()
    {
        //Needed to construct the packet
    }

    public PacketMushroomCloud(double x, double y, double z, float radius, int ticks)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.ticks = ticks;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(radius);
        buffer.writeVarInt(ticks);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        radius = buffer.readFloat();
        ticks = buffer.readVarInt();
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        MushroomCloudFx.start(x, y, z, radius, ticks);
    }

    /**
     * @param range players closer than this to the blast get it
     */
    public static void send(ServerLevel level, double x, double y, double z, float radius, int ticks, double range)
    {
        CreateICBM.packetHandler.sendToAllAround(new PacketMushroomCloud(x, y, z, radius, ticks), level, x, y, z, range);
    }
}
