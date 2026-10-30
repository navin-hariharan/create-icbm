package com.createicbm.content.blocks.radarstation;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.api.radio.messages.ITextMessage;
import com.createicbm.lib.radio.imp.RadioTile;

public class RadioRadar extends RadioTile<TileRadarStation> implements IRadioSender {

    public RadioRadar(TileRadarStation host) {
        super(host);
    }

    @Override
    public void onMessageCallback(IRadioReceiver receiver, IRadioMessage response)
    {
        if (host.isAdvancedRadar() && response instanceof ITextMessage text)
        {
            host.onCounterstrikeResponse(text.getMessage());
        }
    }
}
