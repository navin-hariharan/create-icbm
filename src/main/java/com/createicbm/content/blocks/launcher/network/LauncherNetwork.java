package com.createicbm.content.blocks.launcher.network;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.launcher.ILauncherSolution;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.content.blocks.launcher.FiringPackage;
import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.blocks.launcher.cruise.TileCruiseLauncher;
import net.minecraft.world.phys.Vec3;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LauncherNetwork {

    @Getter
    private final HashSet<LauncherNode> components = new HashSet<LauncherNode>();

    @Getter
    private final List<LauncherEntry> launchers = new LinkedList();

    public final NetworkInventory inventory = new NetworkInventory(this);

    public void invalidate(LauncherNode source) {
        final HashSet<LauncherNode> components = new HashSet(this.components);
        clearData();

        // Clear existing network state
        components.forEach(LauncherNode::onNetworkInvalidate);

        // Trigger new network state
        components.stream().filter(node -> node != source).forEach(LauncherNode::connectToTiles);
    }

    public List<LauncherEntry> getLaunchers(int group) {
        return streamLaunchers(group).collect(Collectors.toList());
    }

    public Stream<LauncherEntry> streamLaunchers(int group) {
        return launchers.stream().filter(l -> group < 0 || l.getLauncher().getLauncherGroup() == group);
    }

    public Stream<LauncherEntry> launch(ILauncherSolution solution, IActionCause cause, boolean simulate) {
        //TODO consider pre-checking launchers to calculate actual firing count rather than desired... for inaccuracy reasons
        return streamLaunchers(solution.getFiringGroup()).peek(entry -> {
            final IMissileTarget target = solution.getTarget(entry.getLauncher());
            entry.setLastFiringPackage(new FiringPackage(target, cause));
            entry.setLastFiringTime(System.currentTimeMillis());
            final FiringPackage queuedBefore = getQueuedPackage(entry.getHost());
            entry.setLastFiringStatus(entry.getLauncher().launch(solution, cause, simulate));
            // A delayed or aiming launch reports a blocking status. It only counts as accepted when this call queued a
            // new package for this target, an older package (launcher already busy) does not.
            final FiringPackage queuedAfter = getQueuedPackage(entry.getHost());
            entry.setLastLaunchQueued(!simulate && queuedAfter != null && queuedAfter != queuedBefore && isSameTarget(queuedAfter, target));
            //TODO consider wrapping solution with a static version to prevent mutability problems
        });
    }

    /**
     * @return the launch waiting on the launcher for its delay or aim, null if none or not one of ours
     */
    @Nullable
    public static FiringPackage getQueuedPackage(@Nullable BlockEntity host) {
        if (host instanceof TileLauncherBase pad) {
            return pad.getFiringPackage();
        }
        if (host instanceof TileCruiseLauncher cruise) {
            return cruise.getFiringPackage();
        }
        return null;
    }

    private static boolean isSameTarget(FiringPackage queued, @Nullable IMissileTarget target) {
        final IMissileTarget queuedTarget = queued.getTargetData();
        if (queuedTarget == null || target == null) {
            return queuedTarget == target;
        }
        final Vec3 a = queuedTarget.getPosition();
        final Vec3 b = target.getPosition();
        return a != null && b != null && a.distanceToSqr(b) < 1.0E-4;
    }

    protected void clearData() {
        launchers.clear();
        components.clear();
        onNetworkUpdated();
    }

    public void mergeNetwork(LauncherNetwork otherNetwork) {
        final LauncherNetwork newNetwork = new LauncherNetwork();
        this.components.forEach(newNetwork::addToNetwork);
        otherNetwork.components.forEach(newNetwork::addToNetwork);
        clearData();
    }

    public void addToNetwork(LauncherNode node) {

        if(!components.contains(node)) {
            components.add(node);

            // Setting node with this
            node.setNetwork(this);

            // Adding if launcher
            final IMissileLauncher launcher = getTileCapability(node.getSelf(), CreateICBMAPI.MISSILE_LAUNCHER_CAPABILITY, null);
            if(launcher != null) {
                launchers.add(new LauncherEntry(launcher, node.getSelf(), null));
                onNetworkUpdated();
            }
        }
    }

    public void onNetworkUpdated() {
        inventory.markDirty();
    }

    @Nullable
    public <T> T getCapability(@Nonnull BlockCapability<T, Direction> capability, @Nullable Direction facing) {
        if(capability == Capabilities.ItemHandler.BLOCK) {
            return (T) inventory;
        }
        return null;
    }

    /**
     * Gets a capability from a tile, uses the ICBM holder directly when possible to avoid the cap cache
     * and falls back to the level lookup for other mods' tiles.
     */
    @Nullable
    public static <T> T getTileCapability(@Nullable BlockEntity tile, BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if(tile == null) {
            return null;
        }
        if(tile instanceof ICapabilityHolderTile) {
            return ((ICapabilityHolderTile) tile).getCapability(capability, side);
        }
        if(tile.getLevel() != null) {
            return tile.getLevel().getCapability(capability, tile.getBlockPos(), tile.getBlockState(), tile, side);
        }
        return null;
    }
}
