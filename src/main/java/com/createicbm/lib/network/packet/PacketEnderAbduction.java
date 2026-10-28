package com.createicbm.lib.network.packet;

import com.createicbm.CreateICBM;
import com.createicbm.client.fx.EnderAbductionFx;
import com.createicbm.lib.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Drives the client side of the ender missile cutscene (camera, controls and screen effects), sent to the player
 * that is being taken only.
 */
public class PacketEnderAbduction implements IPacket<PacketEnderAbduction>
{
    public static final byte START = 0;
    public static final byte WARP = 1;
    public static final byte ARRIVE = 2;
    public static final byte END = 3;

    private byte action;
    private int entityId;

    public PacketEnderAbduction()
    {
        //Needed to construct the packet
    }

    public PacketEnderAbduction(byte action, int entityId)
    {
        this.action = action;
        this.entityId = entityId;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeByte(action);
        buffer.writeVarInt(entityId);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        action = buffer.readByte();
        entityId = buffer.readVarInt();
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        EnderAbductionFx.handle(action, entityId);
    }

    public static void send(ServerPlayer player, byte action, int entityId)
    {
        CreateICBM.packetHandler.sendToPlayer(new PacketEnderAbduction(action, entityId), player);
    }
}
