package com.createicbm.lib.radio.messages;

import com.createicbm.api.radio.messages.ITargetMessage;
import com.createicbm.api.radio.messages.ITriggerActionMessage;
import lombok.Data;
import net.minecraft.world.phys.Vec3;

@Data
public class TriggerActionTargetMessage implements ITriggerActionMessage, ITargetMessage {

    private final String channel;
    private final Vec3 target;
}
