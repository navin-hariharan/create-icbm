package com.createicbm.client.sound;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side list of sounds started by a blast or missile, keyed by the id the server gave them, so the server can
 * fade them out when the event that made them is over.
 */
public final class TrackedSounds
{
    private static final Long2ObjectMap<List<TrackedSoundInstance>> PLAYING = new Long2ObjectOpenHashMap<>();

    private TrackedSounds()
    {
    }

    public static void play(long key, SoundEvent sound, SoundSource source, double x, double y, double z, float volume, float pitch)
    {
        final SoundManager manager = Minecraft.getInstance().getSoundManager();
        prune(manager);

        final TrackedSoundInstance instance = new TrackedSoundInstance(sound, source, x, y, z, volume, pitch, SoundInstance.createUnseededRandom());
        PLAYING.computeIfAbsent(key, k -> new ArrayList<>()).add(instance);
        manager.play(instance);
    }

    public static void stop(long key)
    {
        final List<TrackedSoundInstance> sounds = PLAYING.remove(key);
        if (sounds != null)
        {
            sounds.forEach(TrackedSoundInstance::requestStop);
        }
    }

    /** Forgets every sound, called when leaving a world (the sound engine stops them itself) */
    public static void clear()
    {
        PLAYING.clear();
    }

    /** Drops sounds that already finished on their own, so the map doesn't grow */
    private static void prune(SoundManager manager)
    {
        if (PLAYING.size() > 32)
        {
            PLAYING.values().removeIf(list -> {
                list.removeIf(sound -> sound.isStopped() || !manager.isActive(sound));
                return list.isEmpty();
            });
        }
    }
}
