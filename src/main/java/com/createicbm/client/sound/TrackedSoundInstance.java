package com.createicbm.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Sound at a fixed position that can be faded out early, see {@link TrackedSounds}
 */
public class TrackedSoundInstance extends AbstractTickableSoundInstance
{
    /** Ticks a sound plays at least, so an event that ends right away still gets heard */
    private static final int MIN_TICKS = 10;
    /** Ticks the fade out takes */
    private static final int FADE_TICKS = 15;

    /** Volume above 1 only sets how far the sound carries, the audible level is capped at 1 */
    private final float audibleVolume;
    private int age = 0;
    private int fade = -1;
    private boolean stopRequested = false;

    public TrackedSoundInstance(SoundEvent sound, SoundSource source, double x, double y, double z, float volume, float pitch, RandomSource random)
    {
        super(sound, source, random);
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
        this.audibleVolume = Math.min(1F, volume);
        this.looping = false;
        this.delay = 0;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
    }

    /** Starts the fade out, once the sound has played for a moment */
    public void requestStop()
    {
        stopRequested = true;
    }

    @Override
    public void tick()
    {
        age++;
        if (stopRequested && fade < 0 && age >= MIN_TICKS)
        {
            fade = FADE_TICKS;
        }
        if (fade >= 0)
        {
            if (fade == 0)
            {
                stop();
                return;
            }
            volume = audibleVolume * fade / (float) FADE_TICKS;
            fade--;
        }
    }
}
