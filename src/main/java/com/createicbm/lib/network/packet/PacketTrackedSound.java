package com.createicbm.lib.network.packet;

import com.createicbm.CreateICBM;
import com.createicbm.client.sound.TrackedSounds;
import com.createicbm.lib.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Starts or stops a sound that belongs to something with a lifetime (a blast, a missile). The key ties the stop to
 * the sounds it started, so the client can fade exactly those out once the event is over.
 */
public class PacketTrackedSound implements IPacket<PacketTrackedSound>
{
    private boolean play;
    private long key;

    private ResourceLocation sound;
    private SoundSource source = SoundSource.BLOCKS;
    private double x, y, z;
    private float volume, pitch;

    public PacketTrackedSound()
    {
        //Needed for forge to construct the packet
    }

    private PacketTrackedSound(boolean play, long key)
    {
        this.play = play;
        this.key = key;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeBoolean(play);
        buffer.writeLong(key);
        if (play)
        {
            buffer.writeResourceLocation(sound);
            buffer.writeEnum(source);
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
            buffer.writeFloat(volume);
            buffer.writeFloat(pitch);
        }
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        play = buffer.readBoolean();
        key = buffer.readLong();
        if (play)
        {
            sound = buffer.readResourceLocation();
            source = buffer.readEnum(SoundSource.class);
            x = buffer.readDouble();
            y = buffer.readDouble();
            z = buffer.readDouble();
            volume = buffer.readFloat();
            pitch = buffer.readFloat();
        }
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        if (play)
        {
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(sound);
            if (event == null)
            {
                event = SoundEvent.createVariableRangeEvent(sound);
            }
            TrackedSounds.play(key, event, source, x, y, z, volume, pitch);
        }
        else
        {
            TrackedSounds.stop(key);
        }
    }

    /**
     * Plays the sound for everyone who can hear it
     */
    public static void sendPlay(Level world, long key, SoundEvent sound, SoundSource source, double x, double y, double z, float volume, float pitch)
    {
        final PacketTrackedSound packet = new PacketTrackedSound(true, key);
        packet.sound = sound.getLocation();
        packet.source = source;
        packet.x = x;
        packet.y = y;
        packet.z = z;
        packet.volume = volume;
        packet.pitch = pitch;
        // Same reach as a vanilla sound of this volume, a bit more so players walking in still hear it start
        final double range = sound.getRange(volume) + 16;
        CreateICBM.packetHandler.sendToAllAround(packet, world, x, y, z, range);
    }

    /**
     * Fades out every sound started with the key. Sent to the whole dimension as players may have moved since.
     */
    public static void sendStop(Level world, long key)
    {
        CreateICBM.packetHandler.sendToAllInDimension(new PacketTrackedSound(false, key), world);
    }
}
