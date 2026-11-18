package com.createicbm.content.blocks.alarm;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.lib.radio.imp.RadioTile;
import com.createicbm.lib.radio.messages.IAirDefenceMessage;

/**
 * Radio of an alarm. Only listens once it is linked to a channel, and only to radars reporting a missile coming in:
 * both the report of a missile in sight and the early warning of one still crossing the map. Friendly missiles,
 * launched on the radar's own channel, are never reported.
 */
public class RadioAlarm extends RadioTile<TileAlarm> implements IRadioReceiver
{
    public RadioAlarm(TileAlarm host)
    {
        super(host);
    }

    @Override
    public boolean canReceive(IRadioSender sender, IRadioMessage packet)
    {
        // Checked first so an unlinked alarm never picks a random channel
        return hasChannel() && super.canReceive(sender, packet);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet)
    {
        if (packet instanceof IAirDefenceMessage && canReceive(sender, packet))
        {
            host.onRadarAlert();
        }
    }
}
