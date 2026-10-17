package com.createicbm.content.blocks.launcher.cruise;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.status.ActionStatusTypes;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.launcher.ILauncherSolution;
import com.createicbm.api.missiles.ICapabilityMissileStack;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.blocks.launcher.FiringPackage;
import com.createicbm.content.blocks.launcher.LauncherBaseCapability;
import com.createicbm.content.blocks.launcher.status.LaunchedWithMissile;
import com.createicbm.content.missile.logic.flight.DirectFlightLogic;
import com.createicbm.content.missile.logic.source.ActionSource;
import com.createicbm.content.missile.logic.source.cause.CausedByBlock;
import com.createicbm.content.blocks.launcher.status.FiringWithDelay;
import com.createicbm.content.blocks.launcher.status.LauncherStatus;
import com.createicbm.content.reg.ItemReg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CLauncherCapability extends LauncherBaseCapability {

    private final TileCruiseLauncher host;

    @Override
    public IActionStatus getStatus() {
        // Min power check
        if (!host.energyStorage.consumePower(host.getFiringCost(), true)) {
            return LauncherStatus.ERROR_POWER;
        }
        // Aiming the body
        else if (host.getFiringPackage() != null && !getHost().isAimed()) {
            return LauncherStatus.FIRING_AIMING;
        }
        // No missile stack
        else if (host.missileHolder.getMissileStack().isEmpty()) {
            return LauncherStatus.ERROR_EMPTY_STACK;
        }
        // Delayed fire
        else if (host.getFiringPackage() != null && host.getFiringPackage().getCountDown() > 0) {
            return new FiringWithDelay(host.getFiringPackage().getCountDown());
        } else if (!host.canSpawnMissileWithNoCollision()) {
            return LauncherStatus.ERROR_SPAWN_COLLIDER;
        }

        return LauncherStatus.READY;
    }

    @Override
    public IActionStatus preCheckLaunch(IMissileTarget targetData, @Nullable IActionCause cause) {
        // Validate target data
        if (targetData == null || targetData.getPosition() == null) {
            return LauncherStatus.ERROR_TARGET_NULL;
        }
        // User safety, yes they will shoot themselves
        else if (host.isTooClose(targetData.getPosition())) {
            return LauncherStatus.ERROR_MIN_RANGE;
        }
        return getStatus();
    }

    @Override
    public IActionStatus launch(ILauncherSolution solution, @Nullable IActionCause cause, boolean simulate) {

        final IMissileTarget target = solution.getTarget(this);

        // Do pre-checks
        final IActionStatus preCheck = preCheckLaunch(target, cause);
        if (preCheck.isType(ActionStatusTypes.BLOCKING)) {
            return preCheck;
        }
        else if (simulate) {
            return LauncherStatus.LAUNCHED;
        }

        // Set target so we can aim
        host.setTarget(target.getPosition()); // TODO store IMissileTarget

        // If not aimed, wait for aim and fire
        if (!host.isAimed()) {
            host.setFiringPackage(new FiringPackage(target, cause, 0));
            return LauncherStatus.FIRING_AIMING; // TODO return aiming status, with callback to check if did fire
        }

        final CausedByBlock selfCause = new CausedByBlock(host.getLevel(), host.getBlockPos(), host.getBlockState());
        selfCause.setPreviousCause(cause);
        final IActionSource missileSource = new ActionSource(getHost().getLevel(), new Vec3(host.getBlockPos().getX() + 0.5, host.getBlockPos().getY() + TileCruiseLauncher.MISSILE__HOLDER_Y, host.getBlockPos().getZ() + 0.5), selfCause);

        final ItemStack inventoryStack = host.missileHolder.getMissileStack();

        {
            final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(CreateICBMAPI.MISSILE_STACK_CAPABILITY);
            if (capabilityMissileStack != null) {

                if (host.isServer()) {
                    final IMissile missile = capabilityMissileStack.newMissile(host.getLevel());
                    final Entity entity = missile.getMissileEntity();
                    entity.setPos(missileSource.getPosition().x, missileSource.getPosition().y, missileSource.getPosition().z);

                    // Should always work but in rare cases capability might have failed
                    if (!host.missileHolder.consumeMissile()) {
                        return LauncherStatus.ERROR_INVALID_STACK;
                    }

                    // Check power again, with firing delay things could change
                    if (!host.energyStorage.consumePower(host.getFiringCost(), true)) {
                        return LauncherStatus.ERROR_POWER;
                    }
                    host.energyStorage.consumePower(host.getFiringCost(), false);

                    //Setup missile
                    missile.setMissileSource(missileSource);
                    missile.setTargetData(target);
                    missile.setFlightLogic(new DirectFlightLogic(ConfigMissile.CRUISE_FUEL));
                    missile.launch();

                    if (!host.getLevel().addFreshEntity(entity)) {
                        return LauncherStatus.ERROR_SPAWN;
                    }
                    return new LaunchedWithMissile().setMissile(missile);
                }
                return LauncherStatus.LAUNCHED;
            }
        }
        return LauncherStatus.ERROR_GENERIC;
    }

    @Override
    public float getPayloadVelocity() {
        // TODO find a way to get this from the missile stack
        return host.getMissileHolder().getMissileStack().getItem() == ItemReg.itemSAM.get()
            ? ConfigMissile.SAM_MISSILE.FLIGHT_SPEED : ConfigMissile.DIRECT_FLIGHT_SPEED;
    }
}
