package com.createicbm.lib.radio.messages;

import net.minecraft.world.phys.Vec3;

/**
 * Advanced Radar order to strike the launch site of an incoming missile. Only Launch Controllers carry it out, Cruise
 * Turrets keep their missiles for air defence.
 */
public class CounterstrikeMessage extends TriggerActionTargetMessage
{
    public CounterstrikeMessage(String channel, Vec3 target)
    {
        super(channel, target);
    }
}
