package com.createicbm.client;

import com.createicbm.jlib.data.vector.IPos3D;
import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.lib.network.packet.PacketTrackedSound;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Enum of sounds used by ICBM
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/6/2018.
 * <p>
 * Credit to https://github.com/kitsushadow for sharing info on how to do sounds in MC 1.12
 */
public enum ICBMSounds
{
    ANTIMATTER("antimatter"),
    BEAM_CHARGING("beamcharging"),
    COLLAPSE("collapse"),
    DEBILITATION("debilitation"),
    EXPLOSION("explosion"),
    EXPLOSION_FIRE("explosionfire"),
    GAS_LEAK("gasleak"),
    MACHINE_HUM("machinehum"),
    POWER_DOWN("powerdown"),
    REDMATTER("redmatter"),
    SONICWAVE("sonicwave"),
    MISSILE_LAUNCH("missilelaunch"),
    MISSILE_ENGINE("missileinair"),
    MEEP("meep", SoundSource.NEUTRAL),
    RADIOACTIVE_TICK("radioactive_tick", SoundSource.BLOCKS),
    FLASHBANG("flashbang"),
    RECON_PULSE("recon_pulse"),
    PHANTOM_SUMMON("phantom_summon", SoundSource.HOSTILE),
    PHANTOM_POOF("phantom_poof", SoundSource.HOSTILE),
    DARKNESS("darkness"),
    MAIL_DING("mail_ding", SoundSource.BLOCKS),
    ENDER_SCREAM("ender_scream", SoundSource.HOSTILE),
    ENDER_WARP("ender_warp", SoundSource.HOSTILE),
    EXPLOSION_DISTANT("explosion_distant"),
    ANTIMATTER_BLAST("antimatter_blast");

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ICBMConstants.DOMAIN);

    private static final AtomicLong TRACKED_KEYS = new AtomicLong();
    /** 24 is heard out to 384 blocks */
    private static final float MAX_VIEW_VOLUME = 24F;

    private final ResourceLocation location;
    private final SoundSource category;
    private DeferredHolder<SoundEvent, SoundEvent> holder;

    ICBMSounds(String path)
    {
       this(path, SoundSource.BLOCKS);
    }

    ICBMSounds(String path, SoundSource category)
    {
        this.category = category;
        location = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, path);
    }

    /**
     * Gets the sound event for use with MC code
     *
     * @return sound event
     */
    public SoundEvent getSound()
    {
        return holder.get();
    }

    /**
     * Players the given sound at the entity's location with the arguments
     *
     * @param entity        - source
     * @param volume        - sound level
     * @param pitch         - sound pitch
     * @param distanceDelay - should the sound be delayed by distance
     */
    public void play(Entity entity, float volume, float pitch, boolean distanceDelay)
    {
        //TODO move audio settings to constants attached to configs
        play(entity.level(), entity.getX(), entity.getY(), entity.getZ(), volume, pitch, distanceDelay);
    }

    /**
     * Players the given sound at the entity's location with the arguments
     *
     * @param world         - location
     * @param x-            location
     * @param y-            location
     * @param z-            location
     * @param volume        - sound level
     * @param pitch         - sound pitch
     * @param distanceDelay - should the sound be delayed by distance
     */
    public void play(Level world, double x, double y, double z, float volume, float pitch, boolean distanceDelay)
    {
        if(world.isClientSide()) {
            world.playLocalSound(x, y, z, getSound(), category, volume, pitch, distanceDelay);
        }
        else {
            world.playSound(null, x, y, z, getSound(), category, volume, pitch);
        }
    }

    public void play(Level world, IPos3D pos, float volume, float pitch, boolean distanceDelay)
    {
        world.playSound(null, pos.x(), pos.y(), pos.z(), getSound(), category, volume, pitch);
    }

    /**
     * Plays the sound attached to the entity, so it moves with it and stops when the entity is removed
     */
    public void playOn(Entity entity, float volume, float pitch)
    {
        entity.level().playSound(null, entity, getSound(), category, volume, pitch);
    }

    /**
     * Plays the sound so it can be faded out with {@link #stopTracked(Level, long)} once the thing that made it is over
     *
     * @param key from {@link #nextTrackedKey()}, the same key can start several sounds
     */
    public void playTracked(Level world, long key, double x, double y, double z, float volume, float pitch)
    {
        if (world.isClientSide())
        {
            play(world, x, y, z, volume, pitch, false);
        }
        else
        {
            PacketTrackedSound.sendPlay(world, key, getSound(), category, x, y, z, volume, pitch);
        }
    }

    /**
     * Fades out every sound started with the key
     */
    public static void stopTracked(Level world, long key)
    {
        if (world != null && !world.isClientSide())
        {
            PacketTrackedSound.sendStop(world, key);
        }
    }

    /**
     * Volume that makes a sound carry as far as players can see: a sound is heard 16 blocks out per volume above 1
     * (the loudness itself stays the same, it just fades over a longer way)
     *
     * @param level server level or client level
     */
    public static float viewVolume(Level level)
    {
        return (float) Math.max(1.0, Math.min(MAX_VIEW_VOLUME, CreateICBM.proxy.getViewDistanceBlocks(level) / 16.0));
    }

    public static long nextTrackedKey()
    {
        return TRACKED_KEYS.incrementAndGet();
    }

    public static void register(IEventBus modBus)
    {
        for (ICBMSounds icbmSounds : values())
        {
            icbmSounds.holder = SOUNDS.register(icbmSounds.location.getPath(), () -> SoundEvent.createVariableRangeEvent(icbmSounds.location));
        }
        SOUNDS.register(modBus);
    }
}
