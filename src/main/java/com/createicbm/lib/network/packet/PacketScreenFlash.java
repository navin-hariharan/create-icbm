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

    public PacketScreenFlash()
    {
        //Needed for forge to construct the packet
    }

    public PacketScreenFlash(int ticks)
    {
        this.ticks = ticks;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeVarInt(ticks);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        ticks = buffer.readVarInt();
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        ScreenFlash.start(ticks);
    }

    public static void send(ServerPlayer player, int ticks)
    {
        CreateICBM.packetHandler.sendToPlayer(new PacketScreenFlash(ticks), player);
    }
}
