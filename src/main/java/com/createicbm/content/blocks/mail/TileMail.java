package com.createicbm.content.blocks.mail;

import com.createicbm.client.ICBMSounds;
import com.createicbm.content.reg.BlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Holds the one stack a delivery missile brought and the name of the player who sent it. Has no item handler on
 * purpose, so hoppers, funnels and pipes can't take the mail, only the player who opens it.
 */
public class TileMail extends BlockEntity
{
    /** Mail nobody has opened starts to ding after 3 minutes */
    private static final int DING_AFTER = 3 * 60 * 20;
    /** Then dings every 15 to 25 seconds until someone opens it */
    private static final int DING_INTERVAL = 15 * 20;
    private static final int DING_JITTER = 10 * 20;

    private final ItemStackHandler contents = new ItemStackHandler(1)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }
    };

    private String senderName = "";
    @Nullable
    private UUID senderId = null;
    /** Set when a missile fills it, mail that shows up any other way (commands) removes itself */
    private boolean delivered = false;
    /** Game time it was delivered at */
    private long deliveredAt = 0;
    /** Set the first time a player opens it, which stops the dinging */
    private boolean opened = false;
    private int dingDelay = 0;

    public TileMail(BlockPos pos, BlockState state)
    {
        super(BlockReg.TILE_MAIL.get(), pos, state);
    }

    /**
     * Fills freshly placed mail
     *
     * @param stack  delivered items
     * @param sender player who sent them, null if unknown
     */
    public void fill(ItemStack stack, @Nullable LauncherOwner sender)
    {
        contents.setStackInSlot(0, stack.copy());
        senderName = sender != null ? sender.name() : "";
        senderId = sender != null ? sender.id() : null;
        delivered = true;
        deliveredAt = level != null ? level.getGameTime() : 0;
        setChanged();
        if (level != null)
        {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            UnopenedMail.add(level, worldPosition, senderName);
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        // Brings the list of unopened mail in line, mail delivered before it existed is added this way
        if (level instanceof ServerLevel server)
        {
            server.getServer().execute(this::syncUnopened);
        }
    }

    private void syncUnopened()
    {
        if (level == null || isRemoved())
        {
            return;
        }
        if (delivered && !opened)
        {
            UnopenedMail.add(level, worldPosition, senderName);
        }
        else
        {
            UnopenedMail.remove(level, worldPosition);
        }
    }

    public ItemStack getContents()
    {
        return contents.getStackInSlot(0);
    }

    /** Name of the sender, empty if unknown */
    public String getSenderName()
    {
        return senderName;
    }

    public boolean isDelivered()
    {
        return delivered;
    }

    /**
     * Moves the contents into the player's inventory
     *
     * @return true once the mail is empty
     */
    public boolean collect(Player player)
    {
        opened = true;
        setChanged();
        UnopenedMail.remove(player.level(), worldPosition);
        final ItemStack stack = contents.getStackInSlot(0).copy();
        if (!stack.isEmpty())
        {
            // Not Inventory.add(), in creative it deletes whatever doesn't fit
            final ItemStack rest = ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(player.getInventory()), stack, false);
            contents.setStackInSlot(0, rest.isEmpty() ? ItemStack.EMPTY : rest);
        }
        return contents.getStackInSlot(0).isEmpty();
    }

    /** Takes out whatever is left, used when the block is removed some other way */
    public ItemStack takeContents()
    {
        final ItemStack stack = contents.getStackInSlot(0);
        contents.setStackInSlot(0, ItemStack.EMPTY);
        return stack;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("contents", contents.serializeNBT(registries));
        tag.putString("sender", senderName);
        if (senderId != null)
        {
            tag.putUUID("sender_id", senderId);
        }
        tag.putBoolean("delivered", delivered);
        tag.putLong("delivered_at", deliveredAt);
        tag.putBoolean("opened", opened);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        if (tag.contains("contents"))
        {
            contents.deserializeNBT(registries, tag.getCompound("contents"));
        }
        senderName = tag.getString("sender");
        senderId = tag.hasUUID("sender_id") ? tag.getUUID("sender_id") : null;
        delivered = tag.getBoolean("delivered");
        deliveredAt = tag.getLong("delivered_at");
        opened = tag.getBoolean("opened");
    }

    /**
     * Rings now and then while the parcel waits unopened, so the recipient can find it
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileMail mail)
    {
        if (!mail.delivered || mail.opened)
        {
            return;
        }
        final long now = level.getGameTime();
        if (mail.deliveredAt <= 0 || mail.deliveredAt > now)
        {
            // Mail from before the dinging existed, start counting now
            mail.deliveredAt = now;
            mail.setChanged();
        }
        if (now - mail.deliveredAt < DING_AFTER || --mail.dingDelay > 0)
        {
            return;
        }
        mail.dingDelay = DING_INTERVAL + level.random.nextInt(DING_JITTER);
        level.playSound(null, pos, ICBMSounds.MAIL_DING.getSound(), SoundSource.BLOCKS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
        if (level instanceof ServerLevel server)
        {
            // Count 0 makes the x speed pick the note colour
            server.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 0.85, pos.getZ() + 0.5, 0, level.random.nextInt(25) / 24.0, 0, 0, 1);
        }
    }

    /** Clients only get the sender's name, for the label */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        final CompoundTag tag = new CompoundTag();
        tag.putString("sender", senderName);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
