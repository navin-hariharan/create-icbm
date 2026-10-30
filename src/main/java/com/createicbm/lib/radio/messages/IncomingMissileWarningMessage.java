package com.createicbm.lib.radio.messages;

import com.createicbm.api.radio.messages.ITargetMessage;
import lombok.Data;
import net.minecraft.world.phys.Vec3;

/**
 * Early warning from a radar about a missile that is still crossing the map out of sight and will come down near it.
 * Cruise Turrets turn towards the point where it will appear and charge, so they fire the moment it is seen.
 */
@Data
public class IncomingMissileWarningMessage implements ITargetMessage, IAirDefenceMessage
{
    private final String channel;
    private final Vec3 target;
}
