package com.createicbm.lib.radio.messages;

import com.createicbm.api.radio.IRadioMessage;

/**
 * Radar report meant for air defence only. Cruise Turrets act on it, Launch Controllers ignore it: a launch pad fired
 * at a missile in the sky only wasted its missile.
 */
public interface IAirDefenceMessage extends IRadioMessage
{
}
