package com.createicbm.content.blocks.alarm;

import com.createicbm.ICBMConstants;
import com.createicbm.client.sound.AlarmSound;
import com.createicbm.config.machines.ConfigRadar;
import com.createicbm.content.blocks.alarm.gui.ContainerAlarm;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.network.lambda.PacketCodexReg;
import com.createicbm.lib.network.lambda.tile.PacketCodexTile;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.prefab.tile.IGuiTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Holds the radio channel an {@link BlockAlarm} listens to and how long the last radar alert keeps it sounding. The
 * channel is typed into the alarm's own screen.
 */
public class TileAlarm extends BlockEntity implements IGuiTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "alarm");

    /** Channel typed into the alarm's screen, sent to the server when the box loses focus or Enter is pressed */
    public static final PacketCodexTile<TileAlarm, TileAlarm> PACKET_CHANNEL = (PacketCodexTile<TileAlarm, TileAlarm>) new PacketCodexTile<TileAlarm, TileAlarm>(REGISTRY_NAME, "radio.channel")
        .fromClient()
        .nodeString(TileAlarm::getChannelText, TileAlarm::setRadioChannel);

    /** Longest channel the screen takes, the same as the radar's channel box */
    public static final int MAX_CHANNEL_LENGTH = 32;

    private static final String NBT_RADIO = "radio";
    private static final String NBT_ALERT_UNTIL = "alert_until";

    public final RadioAlarm radio = new RadioAlarm(this);

    /** Game time the last radar alert runs out */
    private long alertUntil;

    /** Client only: the siren playing for this alarm. Kept as Object so a server never loads the client sound class. */
    private Object siren;

    public TileAlarm(BlockPos pos, BlockState state)
    {
        super(BlockReg.TILE_ALARM.get(), pos, state);
    }

    /**
     * Ticks an alert keeps the alarm sounding. A radar reports each missile it sees again every
     * {@link ConfigRadar#SAM_TICKS}, so this outlasts two reports and the siren runs without gaps until the sky is
     * clear.
     */
    private static int alertTicks()
    {
        return Math.max(100, 2 * Math.max(1, ConfigRadar.SAM_TICKS) + 20);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        // Next tick: an alarm moved by a contraption or a ship is added to the level before its saved channel is
        //  loaded into it
        if (level instanceof ServerLevel server)
        {
            server.getServer().tell(new TickTask(server.getServer().getTickCount(), this::syncRadio));
        }
    }

    private void syncRadio()
    {
        if (level == null || isRemoved())
        {
            return;
        }
        if (radio.hasChannel())
        {
            RadioRegistry.add(radio);
        }
        scheduleCheck(1);
    }

    /**
     * Has the block check redstone and the alert after a delay. Block ticks only run where the chunks around are
     * loaded, reading the neighbours right away could load a chunk at the edge of the loaded area.
     */
    private void scheduleCheck(int delay)
    {
        if (level != null && getBlockState().getBlock() instanceof BlockAlarm block)
        {
            // Ignored while a check is already waiting, that one plans the next
            level.scheduleTick(getBlockPos(), block, delay);
        }
    }

    @Override
    public void setRemoved()
    {
        if (level != null && !level.isClientSide())
        {
            RadioRegistry.remove(radio);
        }
        super.setRemoved();
    }

    /** @return channel of the radars this alarm listens to, null when it isn't linked */
    @Nullable
    public String getRadioChannel()
    {
        return radio.hasChannel() ? radio.getChannel() : null;
    }

    /** @return channel for the screen's text box, empty when the alarm isn't linked */
    public String getChannelText()
    {
        final String channel = getRadioChannel();
        return channel == null ? "" : channel;
    }

    /**
     * Links the alarm to the radars on a channel, or unlinks it. On the server every player nearby is told the new
     * channel, so a screen open on another client shows it.
     *
     * @param channel to listen to, null or empty to stop listening
     */
    public void setRadioChannel(@Nullable String channel)
    {
        if (channel != null && channel.length() > MAX_CHANNEL_LENGTH)
        {
            channel = channel.substring(0, MAX_CHANNEL_LENGTH);
        }
        final String newChannel = channel == null || channel.trim().isEmpty() ? null : channel;
        if (Objects.equals(newChannel, getRadioChannel()))
        {
            return;
        }
        final boolean server = level != null && !level.isClientSide();
        if (server)
        {
            RadioRegistry.remove(radio);
        }
        radio.setChannel(newChannel);
        // Alerts heard on the old channel don't count any more
        alertUntil = 0;
        if (server)
        {
            if (radio.hasChannel())
            {
                RadioRegistry.add(radio);
            }
            // Set by a player standing next to it, so the chunks around are loaded
            BlockAlarm.refresh(level, getBlockPos());
            final BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            setChanged();
        }
    }

    /** @return ticks left of the last radar alert, 0 when there is none */
    public long alertTicksLeft()
    {
        return level == null ? 0 : Math.max(0, alertUntil - level.getGameTime());
    }

    /** A radar on the alarm's channel reported a missile. Server only. */
    void onRadarAlert()
    {
        if (level == null || isRemoved())
        {
            return;
        }
        final int ticks = alertTicks();
        alertUntil = level.getGameTime() + ticks;
        setChanged();
        // An alert is enough to sound, redstone doesn't need checking for that
        final BlockState state = level.getBlockState(getBlockPos());
        if (state.getBlock() instanceof BlockAlarm && !state.getValue(BlockAlarm.ACTIVE))
        {
            level.setBlock(getBlockPos(), state.setValue(BlockAlarm.ACTIVE, true), Block.UPDATE_CLIENTS);
        }
        scheduleCheck(ticks + 1);
    }

    /** Client tick while the alarm sounds, keeps its siren playing */
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileAlarm alarm)
    {
        alarm.siren = AlarmSound.keepPlaying(alarm, alarm.siren);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.create_icbm:alarm.name");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerAlarm(windowId, player, this);
    }

    /** Packets for the alarm's screen, called once at start up in a fixed order with the other machines */
    public static void register()
    {
        PacketCodexReg.register(PACKET_CHANNEL);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        if (nbt.contains(NBT_RADIO))
        {
            radio.deserializeNBT(registries, nbt.getCompound(NBT_RADIO));
        }
        else
        {
            // An unlinked alarm saves no radio, also how a client hears the alarm was unlinked
            radio.setChannel(null);
        }
        alertUntil = nbt.getLong(NBT_ALERT_UNTIL);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        if (radio.hasChannel())
        {
            nbt.put(NBT_RADIO, radio.serializeNBT(registries));
        }
        if (alertUntil > 0)
        {
            nbt.putLong(NBT_ALERT_UNTIL, alertUntil);
        }
    }

    /**
     * Sent with the chunk and on {@code sendBlockUpdated}: clients only need the channel, for the screen. The radio is
     * always written, an empty tag would be ignored and a client would miss the alarm being unlinked.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.put(NBT_RADIO, radio.serializeNBT(registries));
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
