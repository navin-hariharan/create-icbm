package com.createicbm.content.blocks.launcher.cruise;

import com.createicbm.api.radio.IRadioMessage;
import com.createicbm.api.radio.IRadioReceiver;
import com.createicbm.api.radio.IRadioSender;
import com.createicbm.api.radio.messages.IIncomingMissileMessage;
import com.createicbm.api.radio.messages.ITargetMessage;
import com.createicbm.api.radio.messages.ITriggerActionMessage;
import com.createicbm.content.blocks.launcher.FiringPackage;
import com.createicbm.content.blocks.launcher.LauncherLangs;
import com.createicbm.content.blocks.launcher.status.LauncherStatus;
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

import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;

public class RadioCruise extends RadioTile<TileCruiseLauncher> implements IRadioReceiver, INBTSerializable<CompoundTag> {

    public RadioCruise(TileCruiseLauncher host) {
        super(host);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        if (canReceive(sender, packet)) {

            // Set target packet, run first as laser-det triggers both (set & fire) from the same packet
            boolean targetApplied = true;
            if(packet instanceof ITargetMessage) {
                targetApplied = false;
                final double vel = host.launcher.getPayloadVelocity();
                final Vec3 target = ((ITargetMessage) packet).getIntercept(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + 0.5, host.getBlockPos().getZ() + 0.5, vel);
                if(target != null) {
                    final Vec3 previous = host.getTarget();
                    host.setTarget(target);
                    // Another mod can cancel the new target (LauncherSetTargetEvent), firing then would hit the old one.
                    // A target the event moved somewhere else still counts as set.
                    final Vec3 applied = host.getTarget();
                    targetApplied = applied != null && (applied.distanceToSqr(target) < 1.0E-4 || !applied.equals(previous));

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
                if (!targetApplied) {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_FAILED));
                    return;
                }
                if(host.getFiringPackage() != null) {
                    // A radar keeps reporting the missile while the turret turns. Follow it, the queued order still
                    // pointed at where the missile was first seen, and the SAM fired there without a lock.
                    if (packet instanceof IIncomingMissileMessage) {
                        final FiringPackage pending = host.getFiringPackage();
                        pending.setTargetData(new BasicTargetData(host.getTarget()));
                        pending.setOnTriggerCallback(((ITriggerActionMessage) packet)::onTriggerCallback);
                        sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_TRIGGERED));
                        return;
                    }
                    sender.onMessageCallback(this, new TextMessage(getChannel(), LauncherLangs.ERROR_MISSILE_QUEUED));
                    return;
                }
                final FiringPackage firingPackage = getFiringPackage(sender, (ITriggerActionMessage) packet);

                // Only acknowledge a launch that can happen: a missile, charge and a target out of minimum range, and
                // a turret that can turn to aim. A queued package that can never fire used to be reported as triggered.
                final IActionStatus status = host.launcher.preCheckLaunch(firingPackage.getTargetData(), firingPackage.getCause());
                // Still winding up its charge counts as fine while the shaft turns, the turret fires once charged
                final boolean charging = status == LauncherStatus.ERROR_POWER && host.getAimSpeedFactor() > 0
                    && !host.missileHolder.getMissileStack().isEmpty();
                if ((status.isBlocking() && !charging) || (!host.isAimed() && host.getAimSpeedFactor() <= 0)) {
                    ICBMDiagnostics.info("CRUISE_RADIO_TRIGGER_REFUSED pos={} status={} aimed={}", host.getBlockPos(), status.getRegistryKey(), host.isAimed());
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_FAILED));
                    return;
                }
                host.setFiringPackage(firingPackage);
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
