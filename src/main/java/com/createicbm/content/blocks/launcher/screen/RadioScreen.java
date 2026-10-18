package com.createicbm.content.blocks.launcher.screen;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.api.radio.messages.ITargetMessage;
import com.createicbm.api.radio.messages.ITriggerActionMessage;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.radio.imp.RadioTile;
import com.createicbm.prefab.FakeRadioSender;
import com.createicbm.lib.radio.messages.RadioTranslations;
import com.createicbm.lib.radio.messages.TextMessage;
import net.minecraft.world.phys.Vec3;

public class RadioScreen extends RadioTile<TileLauncherScreen> implements IRadioReceiver {

    public RadioScreen(TileLauncherScreen host) {
        super(host);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        if (canReceive(sender, packet)) {

            // Set target packet, run first as laser-det triggers both (set & fire) from the same packet
            if(packet instanceof ITargetMessage) {
                final double vel = host.getLaunchersInGroup()
                    .stream().mapToDouble(launcherEntry -> launcherEntry.getLauncher().getPayloadVelocity()).max()
                    .orElse(ConfigMissile.DIRECT_FLIGHT_SPEED);
                final Vec3 target = ((ITargetMessage) packet).getIntercept(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5, host.getBlockPos().getZ() + 0.5, vel);
                if(target != null) {
                    host.setTarget(target);

                    // Don't show set message if we are going to fire right away
                    if(!(packet instanceof ITriggerActionMessage)) {
                        sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_TARGET_SET, target.x, target.y, target.z));
                    }
                }
                else {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_TARGET_NULL));
                }
            }

            // Fire missile packet
            if(packet instanceof ITriggerActionMessage && ((ITriggerActionMessage) packet).shouldTrigger()) {
                if (host.fireAllLaunchers(false, sender instanceof FakeRadioSender fake ? fake.player : null)) { // TODO collect all screens and provide a single feedback message
                    final Vec3 target = host.getTarget();
                    final double distance = Math.sqrt(target.distanceToSqr(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5, host.getBlockPos().getZ() + 0.5)); // TODO base from launcher
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_SUCCESS, target.x, target.y, target.z, distance));

                    //((ILaunchMessage) packet).onLaunchCallback(); TODO implement
                }
                else {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_FAILED));
                }
            }
        }
    }
}
