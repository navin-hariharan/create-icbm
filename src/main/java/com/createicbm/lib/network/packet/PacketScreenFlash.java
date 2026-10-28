package com.createicbm.lib.network.packet;

import com.createicbm.CreateICBM;
import com.createicbm.client.fx.ScreenFlash;
import com.createicbm.lib.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Turns the player's screen white for a moment, sent by flashbang missiles
 */
public class PacketScreenFlash implements IPacket<PacketScreenFlash>
{
    private int ticks;
    private int fadeTicks = 10;

    public PacketScreenFlash()
    {
        //Needed for forge to construct the packet
    }

    public PacketScreenFlash(int ticks)
    {
        this.ticks = ticks;
    }

    public PacketScreenFlash(int ticks, int fadeTicks)
    {
        this.ticks = ticks;
        this.fadeTicks = fadeTicks;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeVarInt(ticks);
        buffer.writeVarInt(fadeTicks);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        ticks = buffer.readVarInt();
        fadeTicks = buffer.readVarInt();
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        ScreenFlash.start(ticks, fadeTicks);
    }

    public static void send(ServerPlayer player, int ticks)
    {
        CreateICBM.packetHandler.sendToPlayer(new PacketScreenFlash(ticks), player);
    }

    public static void send(ServerPlayer player, int ticks, int fadeTicks)
    {
        CreateICBM.packetHandler.sendToPlayer(new PacketScreenFlash(ticks, fadeTicks), player);
    }
}
