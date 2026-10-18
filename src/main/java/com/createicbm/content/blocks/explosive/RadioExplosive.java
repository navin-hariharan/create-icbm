package com.createicbm.content.blocks.explosive;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.api.radio.messages.ITargetMessage;
import com.createicbm.api.radio.messages.ITriggerActionMessage;
import com.createicbm.lib.radio.imp.RadioTile;
import com.createicbm.lib.radio.messages.TextMessage;
import com.createicbm.prefab.FakeRadioSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Radio of an explosive crate. Only listens once a Remote Detonator has wired the crate to its channel.
 */
public class RadioExplosive extends RadioTile<TileEntityExplosive> implements IRadioReceiver, INBTSerializable<CompoundTag> {

    public static final String SUCCESS = "tile.explosive.radio.success";

    public RadioExplosive(TileEntityExplosive host) {
        super(host);
    }

    @Override
    public boolean canReceive(IRadioSender sender, IRadioMessage packet) {
        // Checked first so an unwired crate never picks a random channel
        return hasChannel() && super.canReceive(sender, packet);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        // Fire signal only, radar alerts and designator strikes carry a target and are meant for launchers
        if (canReceive(sender, packet)
            && packet instanceof ITriggerActionMessage && ((ITriggerActionMessage) packet).shouldTrigger()
            && !(packet instanceof ITargetMessage)) {

            Entity igniter = null;
            if(sender instanceof FakeRadioSender) {
                igniter = ((FakeRadioSender) sender).player;
            }

            if(host.triggerByRadio(igniter)) {
                sender.onMessageCallback(this, new TextMessage(getChannel(), SUCCESS));
            }
        }
    }
}
