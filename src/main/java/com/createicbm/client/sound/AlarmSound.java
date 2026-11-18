package com.createicbm.client.sound;

import com.createicbm.client.ICBMSounds;
import com.createicbm.content.blocks.alarm.BlockAlarm;
import com.createicbm.content.blocks.alarm.TileAlarm;
import com.createicbm.mods.sable.SableProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The looping siren of one alarm. Stops the moment the alarm falls silent, is broken or unloaded, and follows an alarm
 * on a Create Aeronautics ship. How far it is heard is set in sounds.json (attenuation_distance).
 */
public class AlarmSound extends AbstractTickableSoundInstance
{
    /** Heard out to 64 blocks, see sounds.json. Started a little closer than it stops, so it doesn't flicker at the edge. */
    private static final double START_RANGE = 64;
    private static final double STOP_RANGE = 72;

    /** Ticks the sound engine gets to start the siren before it counts as dropped and is started again */
    private static final int START_TICKS = 40;

    private final TileAlarm alarm;
    private final long startTime;

    private AlarmSound(TileAlarm alarm, Vec3 pos)
    {
        super(ICBMSounds.ALARM_SIREN.getSound(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.alarm = alarm;
        this.startTime = alarm.getLevel().getGameTime();
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = false;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    /**
     * Called every client tick of a sounding alarm.
     *
     * @param current the alarm's siren from the last call, null if none
     * @return the siren now playing, null when the player is out of earshot
     */
    @Nullable
    public static Object keepPlaying(TileAlarm alarm, @Nullable Object current)
    {
        final Minecraft mc = Minecraft.getInstance();
        final Level level = alarm.getLevel();
        if (current instanceof AlarmSound sound && !sound.isStopped()
            && (level == null || level.getGameTime() - sound.startTime < START_TICKS || mc.getSoundManager().isActive(sound)))
        {
            return current;
        }
        final Player player = mc.player;
        if (level == null || player == null || level != mc.level)
        {
            return null;
        }
        final Vec3 pos = SableProxy.INSTANCE.worldCenter(level, alarm.getBlockPos());
        if (player.distanceToSqr(pos) > START_RANGE * START_RANGE)
        {
            return null;
        }
        final AlarmSound sound = new AlarmSound(alarm, pos);
        mc.getSoundManager().play(sound);
        return sound;
    }

    @Override
    public void tick()
    {
        final Minecraft mc = Minecraft.getInstance();
        final Level level = alarm.getLevel();
        if (alarm.isRemoved() || level == null || level != mc.level || mc.player == null || !isSounding(level))
        {
            stop();
            return;
        }
        // Follows an alarm on a ship, a fixed alarm stays where it is
        final Vec3 pos = SableProxy.INSTANCE.worldCenter(level, alarm.getBlockPos());
        if (mc.player.distanceToSqr(pos) > STOP_RANGE * STOP_RANGE)
        {
            stop();
            return;
        }
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    private boolean isSounding(Level level)
    {
        final BlockState state = level.getBlockState(alarm.getBlockPos());
        return state.getBlock() instanceof BlockAlarm && state.getValue(BlockAlarm.ACTIVE);
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }
}
