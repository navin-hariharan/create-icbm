package com.createicbm.api.radio.messages;

import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.radio.IRadioMessage;
import net.minecraft.world.entity.Entity;

/**
 * Packet containing incoming missile information from radar detection
 */
public interface IIncomingMissileMessage extends IRadioMessage, ITargetMessage, ITriggerActionMessage {

    IMissile getMissile();

    @Override
    default Entity getTargetEntity() {
        return getMissile().getMissileEntity();
    }
}
