package com.createicbm.client.sound;

import com.createicbm.client.ICBMSounds;
import com.createicbm.content.missile.entity.EntityMissile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * One looping engine sound per missile that follows it and stops the moment the missile is gone or its engine
 * cuts out. It used to start a new copy of the engine sound every tick, and the last copies kept playing on after
 * the missile had exploded.
 */
public class MissileEngineSound extends AbstractTickableSoundInstance
{
    /** Volume above 1 makes the engine carry further, 2 is heard out to 32 blocks */
    private static final float VOLUME = 2.0F;
    /** Engine sounds closer than this start playing */
    private static final double START_RANGE = 40;
    /** Engine sounds stop again past this */
    private static final double STOP_RANGE = 56;
    /** Most engine sounds playing at once, so a big salvo doesn't use up all the sound channels */
    private static final int MAX_PLAYING = 16;

    /** Ticks the sound engine gets to start a sound before it counts as dropped */
    private static final int START_TICKS = 5;

    private static final List<MissileEngineSound> PLAYING = new ArrayList<>();

    /** Client ticks counted by {@link #onClientTick()}, the sound's own tick only runs once the engine plays it */
    private static long clientTicks = 0;

    private final EntityMissile<?> missile;
    private final long startTick;

    private MissileEngineSound(EntityMissile<?> missile)
    {
        super(ICBMSounds.MISSILE_ENGINE.getSound(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.missile = missile;
        this.looping = true;
        this.delay = 0;
        this.volume = VOLUME;
        this.pitch = 0.7F + random.nextFloat() * 0.15F;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.x = missile.getX();
        this.y = missile.getY();
        this.z = missile.getZ();
        this.startTick = clientTicks;
    }

    /**
     * Called every client tick the missile's engine runs. Starts the loop if the missile doesn't have one yet.
     */
    public static void onEngineTick(EntityMissile<?> missile)
    {
        missile.lastEngineTick = missile.tickCount;

        if (missile.engineSound != null || PLAYING.size() >= MAX_PLAYING)
        {
            return;
        }
        final Player player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(missile) > START_RANGE * START_RANGE)
        {
            return;
        }
        final MissileEngineSound sound = new MissileEngineSound(missile);
        missile.engineSound = sound;
        PLAYING.add(sound);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    /**
     * Called every client tick. The sound engine can refuse or drop a sound without telling it (volume at 0, no free
     * channel, sounds reloaded, world left), those would hold their missile and fill the list for good.
     */
    public static void onClientTick()
    {
        clientTicks++;
        if (PLAYING.isEmpty())
        {
            return;
        }
        final Minecraft mc = Minecraft.getInstance();
        final SoundManager manager = mc.getSoundManager();
        for (MissileEngineSound sound : List.copyOf(PLAYING))
        {
            if (sound.isDead(mc, manager))
            {
                sound.finish();
            }
        }
    }

    /** Forgets every engine sound, called when leaving a world */
    public static void stopAll()
    {
        for (MissileEngineSound sound : List.copyOf(PLAYING))
        {
            sound.finish();
        }
        PLAYING.clear();
    }

    private boolean isDead(Minecraft mc, SoundManager manager)
    {
        return isStopped() || missile.isRemoved() || missile.level() != mc.level
            || clientTicks - startTick > START_TICKS && !manager.isActive(this);
    }

    @Override
    public void tick()
    {
        final Player player = Minecraft.getInstance().player;
        final boolean engineOff = missile.tickCount - missile.lastEngineTick > 2;
        if (missile.isRemoved() || engineOff || player == null || player.distanceToSqr(missile) > STOP_RANGE * STOP_RANGE)
        {
            finish();
            return;
        }
        this.x = missile.getX();
        this.y = missile.getY();
        this.z = missile.getZ();
    }

    /** Stops the loop and frees the missile to start a new one */
    private void finish()
    {
        if (!isStopped())
        {
            stop();
        }
        PLAYING.remove(this);
        if (missile.engineSound == this)
        {
            missile.engineSound = null;
        }
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }
}
