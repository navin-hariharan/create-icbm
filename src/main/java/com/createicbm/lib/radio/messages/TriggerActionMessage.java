package com.createicbm.lib.radio.messages;

import com.createicbm.api.radio.messages.ITriggerActionMessage;
import lombok.Data;

@Data
public class TriggerActionMessage implements ITriggerActionMessage {

    private final String channel;
}
