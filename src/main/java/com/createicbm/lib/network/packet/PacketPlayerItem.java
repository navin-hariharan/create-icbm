package com.createicbm.lib.network.packet;

import com.createicbm.lib.network.IPacketIDReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * @author tgame14
 * @since 26/05/14
 */
@Deprecated
public class PacketPlayerItem extends PacketBase<PacketPlayerItem>
{
    public int slotId;
    public int id = 0;

    public PacketPlayerItem()
    {
        //Needed to construct the packet
    }

    public PacketPlayerItem(int slotId)
    {
        this.slotId = slotId;
    }

    public PacketPlayerItem(Player player)
    {
        this(player.getInventory().selected);
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(slotId);
        buffer.writeInt(id);
        super.encodeInto(buffer);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        slotId = buffer.readInt();
        id = buffer.readInt();
        super.decodeInto(buffer);
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        handleServerSide(player);
    }

    @Override
    public void handleServerSide(Player player)
    {
        final Item item;
        if (slotId < 0)
        {
            item = BuiltInRegistries.ITEM.byId(Math.abs(this.slotId));
        }
        else if (slotId < player.getInventory().getContainerSize())
        {
            item = player.getInventory().getItem(this.slotId).getItem();
        }
        else
        {
            // Sent by a broken or modified client
            return;
        }
        if (item instanceof IPacketIDReceiver receiver && receiver.shouldReadPacket(player, null, this))
        {
            receiver.read(data(), id, player, this);
        }
    }
}
