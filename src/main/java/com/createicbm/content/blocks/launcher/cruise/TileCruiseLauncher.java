package com.createicbm.content.blocks.launcher.cruise;

import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.events.LauncherSetTargetEvent;
import com.createicbm.config.ConfigMain;
import com.createicbm.config.machines.ConfigLauncher;
import com.createicbm.content.blocks.launcher.FiringPackage;
import com.createicbm.content.blocks.launcher.PendingLauncherChunkLoader;
import com.createicbm.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import com.createicbm.content.blocks.launcher.network.ILauncherComponent;
import com.createicbm.content.blocks.launcher.network.LauncherNode;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.content.missile.logic.source.cause.RedstoneCause;
import com.createicbm.content.missile.logic.targeting.BasicTargetData;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.capability.launcher.CapabilityMissileHolder;
import com.createicbm.lib.data.IMachineInfo;
import com.createicbm.lib.energy.storage.EnergyBuffer;
import com.createicbm.lib.network.lambda.PacketCodexReg;
import com.createicbm.lib.network.lambda.tile.PacketCodexTile;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.tile.TickAction;
import com.createicbm.lib.tile.TickDoOnce;
import com.createicbm.lib.transform.rotation.EulerAngle;
import com.createicbm.lib.transform.vector.Pos;
import com.createicbm.prefab.gui.IPlayerUsing;
import com.createicbm.prefab.inventory.InventorySlot;
import com.createicbm.prefab.inventory.InventoryWithSlots;
import com.createicbm.prefab.tile.IGuiTile;
import com.createicbm.prefab.tile.TileKineticMachine;
import lombok.Getter;
import lombok.Setter;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class TileCruiseLauncher extends TileKineticMachine implements IGuiTile, ILauncherComponent, IMachineInfo, IPlayerUsing, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "cruiselauncher");

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    public static final double MISSILE__HOLDER_Y = 2.0;

    /** Target position of the launcher */
    private Vec3 _targetPos = Vec3.ZERO;

    /** Desired aim angle, updated every tick if target != null */
    @Getter
    protected final EulerAngle aim = new EulerAngle(0, 0, 0); //TODO change UI to only have yaw and pitch, drop xyz but still allow tools to auto fill from xyz

    /** Current aim angle, updated each tick */
    @Getter
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();

    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    /** Launch charge, wound up by the shaft. A launch needs it full. */
    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_COST)
        .withOnChange((p,c,s) -> this.onChargeChanged(s));
    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> setChanged())
        .withSlot(new InventorySlot(0, CreateICBMHelpers::isMissile).withChangeCallback((stack) -> this.setChanged()));

    @Getter
    protected final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);

    @Getter
    protected final CLauncherCapability launcher = new CLauncherCapability(this);

    @Getter
    protected FiringPackage firingPackage;

    private final LauncherNode launcherNode = new LauncherNode(this, true);
    public final RadioCruise radio = new RadioCruise(this);

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileCruiseLauncher(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_CRUISE_LAUNCHER.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerCruiseLauncher));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if(isServer()) {
            descriptionPacketSender.doNext();
        }
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept("NEEDS_POWER", ConfigMain.REQUIRES_POWER);
        consumer.accept("ENERGY_COST_ACTION", getFiringCost());
    }

    public int getFiringCost() {
        return ConfigMain.REQUIRES_POWER ? ConfigLauncher.POWER_COST : 0;
    }

    /** True when the launch charge is full enough to fire */
    public boolean isCharged()
    {
        return energyStorage.consumePower(getFiringCost(), true);
    }

    /** Launch charge as 0-1 */
    public float getChargePercent()
    {
        if (getFiringCost() <= 0)
        {
            return 1;
        }
        return Math.min(1, energyStorage.getEnergyStored() / (float) getFiringCost());
    }

    /** Aim speed multiplier from the shaft speed, 0 when the turret has no rotation */
    public double getAimSpeedFactor()
    {
        if (!ConfigMain.REQUIRES_POWER)
        {
            return 1;
        }
        if (!isRunning())
        {
            return 0;
        }
        return Math.min(4, Math.abs(getSpeed()) / ConfigLauncher.CRUISE_AIM_RPM);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking)
    {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        addGoggleHeader(tooltip, "cruise_turret");
        addGoggleLine(tooltip, "launch_charge", percent(Math.round(getChargePercent() * 100), 100),
            getChargePercent() >= 1 ? ChatFormatting.GREEN : ChatFormatting.GOLD);
        addGoggleLine(tooltip, "missile", cachedMissileStack.isEmpty() ? Component.translatable("goggles." + ICBMConstants.PREFIX + "none").getString() : cachedMissileStack.getHoverName().getString(),
            cachedMissileStack.isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA);
        addGoggleLine(tooltip, "aim", Component.translatable(isAimed() ? "goggles." + ICBMConstants.PREFIX + "aimed" : "goggles." + ICBMConstants.PREFIX + "turning").getString(),
            isAimed() ? ChatFormatting.GREEN : ChatFormatting.GOLD);
        return true;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        // Restore/reconcile the persisted ticket only after the chunk and block entity are fully loaded.
        PendingLauncherChunkLoader.sync(this, this.firingPackage != null);
        launcherNode.connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radio);
        }
    }

    /** Sets the queued radio/aiming launch and keeps this block entity ticking until it fires. */
    public void setFiringPackage(FiringPackage firingPackage)
    {
        final boolean wasPending = this.firingPackage != null;
        this.firingPackage = firingPackage;
        // Reconcile every time a pending package is assigned: it is cheap when the ticket already exists, and retries
        // the operation if a prior ticket attempt failed while this launcher was remote from all players.
        if (firingPackage != null || wasPending != (firingPackage != null))
        {
            PendingLauncherChunkLoader.sync(this, firingPackage != null);
        }
        if (firingPackage != null)
        {
            ICBMDiagnostics.info("LAUNCHER_PACKAGE_QUEUED type=cruise pos={} dim={} countdown={} aimed={} target={}",
                getBlockPos(), level == null ? "unknown" : level.dimension().location(), firingPackage.getCountDown(), isAimed(),
                firingPackage.getTargetData() == null ? "null" : firingPackage.getTargetData().getPosition());
        }
        setChanged();
    }

    @Override
    public void invalidate()
    {
        if (isServer()) {
            RadioRegistry.remove(radio);
        }
        getNetworkNode().onTileRemoved();
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        super.update();

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        if(isServer()) {

            // Wind up the launch charge from the shaft
            if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                energyStorage.receiveEnergyInternal(takeRotationCharge(rateFor(getFiringCost(), ConfigLauncher.WIND_UP_SECONDS)), false);
            }

            final boolean wasAimed = isAimed();
            final double aimSpeed = ROTATION_SPEED * getAimSpeedFactor();
            if (aimSpeed > 0) {
                currentAim.moveTowards(aim, aimSpeed, deltaTime).clampTo360();
            }
            // PERF: only while it actually turns, an unpowered unaimed turret resent its description every tick
            if (!wasAimed && aimSpeed > 0) {
                setChanged();
            }

            // Check redstone
            if (this.ticks % REDSTONE_CHECK_RATE == 0) {
                for (Direction side : Direction.values()) {
                    final int power = level.getSignal(getBlockPos().relative(side), side);
                    if (power > 1 && firingPackage == null) {
                        setFiringPackage(new FiringPackage(new BasicTargetData(getTarget()), new RedstoneCause(level, getBlockPos(), getBlockState(), side), 0));
                        break;
                    }
                }
            }

            // Wait for the launch charge too: a turret that just fired recharges while it turns, firing before that
            // dropped the order. Without rotation it never charges, then the launch fails and the order is dropped.
            final FiringPackage pending = firingPackage;
            if (pending != null && isAimed() && (isCharged() || getAimSpeedFactor() <= 0)) {
                pending.setCountDown(pending.getCountDown() - 1);
                if (pending.getCountDown() <= 0) {
                    ICBMDiagnostics.info("LAUNCHER_PACKAGE_FIRE type=cruise pos={} dim={} target={}", getBlockPos(),
                        level.dimension().location(), pending.getTargetData() == null ? "null" : pending.getTargetData().getPosition());
                    try {
                        pending.launch(launcher);
                    } catch (RuntimeException exception) {
                        ICBMDiagnostics.error("LAUNCHER_PACKAGE_FIRE_FAILED type=cruise pos=" + getBlockPos()
                            + " dim=" + level.dimension().location(), exception);
                    } finally {
                        setFiringPackage(null);
                    }
                }
            }
        }
    }

    public void setTarget(Vec3 target)
    {
        if(!Objects.equals(target, this._targetPos)) {

            // Only fire packet server side to avoid description packet triggering events
            if(isServer()) {
                if (target != null && !(Double.isFinite(target.x) && Double.isFinite(target.y) && Double.isFinite(target.z))) {
                    return;
                }
                final LauncherSetTargetEvent event = new LauncherSetTargetEvent(level, getBlockPos(), target);

                if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    this._targetPos = event.target == null ? Vec3.ZERO : event.target;
                    this.setChanged();
                }
            }
            else {
                this._targetPos = target;
            }
            updateAimAngle();
        }
    }

    public boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle()
    {
        if (hasTarget())
        {
            final Vec3 aimPoint = getTarget();
            final Pos center = new Pos(this).add(0.5, MISSILE__HOLDER_Y, 0.5);
            aim.set(center.toEulerAngle(aimPoint).clampTo360());
            aim.setYaw(EulerAngle.clampPos360(aim.yaw()));
        }
        else
        {
            aim.set(0, 0, 0);
        }
    }

    protected void initFromLoad()
    {
        cachedMissileStack = inventory.getStackInSlot(0);
        updateAimAngle();
        currentAim.set(aim);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {
        super.onDataPacket(net, pkt, registries);
        initFromLoad();
    }

    protected boolean hasTarget()
    {
        return getTarget() != null && getTarget() != Vec3.ZERO;
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        // TODO use raytrace to detect collision so we can fire out of holes
         for (int y = 1; y <= 2; y++) {
             for (int x = -1; x < 2; x++) {
                 for (int z = -1; z < 2; z++) {
                     final BlockPos pos = getBlockPos().offset(x, y, z);
                     final BlockState state = level.getBlockState(pos);
                     if (!state.isAir()) {
                         final VoxelShape box = state.getCollisionShape(level, pos);
                         return box.isEmpty() || box.max(Direction.Axis.Y) < 0.5;
                     }
                 }
             }
         }
        return true;
    }

    // Is the target too close?
    public boolean isTooClose(Vec3 target)
    {
        return new Pos(getBlockPos()).add(0.5).distance(target) < 20; //TODO remove pos usage
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.launcher.cruise.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerCruiseLauncher(windowId, player, this);
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        if (capability == Capabilities.ItemHandler.BLOCK)
        {
            return (T) inventory;
        } else if (capability == CreateICBMAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return (T) missileHolder;
        }
        else if(capability == CreateICBMAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) launcher;
        }
        else if(capability == CreateICBMAPI.RADIO_CAPABILITY)
        {
            return (T) radio;
        }
        return null;
    }

    public Vec3 getTarget()
    {
        if (this._targetPos == null)
        {
            this._targetPos = Vec3.ZERO;
        }
        return this._targetPos;
    }

    @Override
    protected void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket)
    {
        super.read(nbt, registries, clientPacket);
        SAVE_LOGIC.load(this, nbt);
        if(nbt.contains(NBTConstants.FREQUENCY)) {
            this.radio.setChannel(Integer.toString(nbt.getInt(NBTConstants.FREQUENCY)));
        }
        initFromLoad();
    }

    @Override
    protected void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket)
    {
        SAVE_LOGIC.save(this, nbt);
        super.write(nbt, registries, clientPacket);
    }

    private static final NbtSaveHandler<TileCruiseLauncher> SAVE_LOGIC = new NbtSaveHandler<TileCruiseLauncher>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBTConstants.INVENTORY, launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radio)
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        /* */.nodeEulerAngle(NBTConstants.CURRENT_AIM, launcher -> launcher.currentAim, (launcher, pos) -> launcher.currentAim.set(pos))
        /* */.nodeINBTSerializable("firing_package", TileCruiseLauncher::getFiringPackage, (launcher, firingPackage) -> {
            // Avoid force-loading chunks from inside NBT deserialization; onLoad() applies the ticket after the chunk
            // is fully available.
            if (launcher.getLevel() == null || !launcher.getLevel().isClientSide()) {
                launcher.firingPackage = firingPackage;
            }
        }, () -> new FiringPackage(null, null))
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.launcher)
        .base();

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_DESCRIPTION = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack((t) -> t.missileHolder.getMissileStack(), (t, f) -> t.cachedMissileStack = f)
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .nodeDouble(t -> t.currentAim.yaw(), (t, f) -> t.currentAim.setYaw(f))
        .nodeDouble(t -> t.currentAim.pitch(), (t, f) -> t.currentAim.setPitch(f))
        // Create addon: launch charge for goggles
        .nodeInt(t -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i));

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_HZ = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.frequency", (t) -> t.radio)
        .fromClient()
        .nodeString(RadioCruise::getChannel, RadioCruise::setChannel)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_DISABLE = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.disable", (t) -> t.radio)
        .fromClient()
        .toggleBoolean(RadioCruise::isDisabled, RadioCruise::setDisabled)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_TARGET = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_GUI = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget);

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_LAUNCH = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((tile, target, player) -> {
            final EntityCause cause = new EntityCause(player); // TODO note was UI interaction
            tile.getLauncher().launch(new BasicTargetData(tile.getTarget()), cause, false); // TODO send feedback to UI as part of a event list or history
            tile.setChanged();
        });
}
