package com.createicbm.content.blocks.launcher.cruise;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.api.radio.messages.ITargetMessage;
import com.createicbm.api.radio.messages.ITriggerActionMessage;
import com.createicbm.content.blocks.launcher.FiringPackage;
import com.createicbm.content.blocks.launcher.LauncherLangs;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.content.missile.logic.targeting.BasicTargetData;
import com.createicbm.lib.radio.imp.RadioTile;
import com.createicbm.lib.radio.messages.RadioTranslations;
import com.createicbm.lib.radio.messages.TextMessage;
import com.createicbm.prefab.FakeRadioSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public class RadioCruise extends RadioTile<TileCruiseLauncher> implements IRadioReceiver, INBTSerializable<CompoundTag> {

    public RadioCruise(TileCruiseLauncher host) {
        super(host);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        if (canReceive(sender, packet)) {

            // Set target packet, run first as laser-det triggers both (set & fire) from the same packet
            if(packet instanceof ITargetMessage) {
                final double vel = host.launcher.getPayloadVelocity();
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
                if(host.getFiringPackage() != null) {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), LauncherLangs.ERROR_MISSILE_QUEUED));
                    return;
                }
                host.setFiringPackage(getFiringPackage(sender, (ITriggerActionMessage) packet));

                // TODO if we are aiming give status feedback
                // TODO if we are in error state, give feedback
                sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_TRIGGERED));
            }
        }
    }

    private @Nonnull FiringPackage getFiringPackage(IRadioSender sender, ITriggerActionMessage packet) {
        FiringPackage firingPackage;
        if(sender instanceof FakeRadioSender) {
            //TODO add radio cause before player, pass in item used
            firingPackage = new FiringPackage(new BasicTargetData(host.getTarget()), new EntityCause(((FakeRadioSender) sender).player), 0);
        }
        else {
            // TODO set cause to radio
            firingPackage = new FiringPackage(new BasicTargetData(host.getTarget()), null, 0);
        }

        firingPackage.setOnTriggerCallback(packet::onTriggerCallback);
        return firingPackage;
    }
}
