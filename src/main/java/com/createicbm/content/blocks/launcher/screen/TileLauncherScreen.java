package com.createicbm.content.blocks.launcher.screen;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.entity.Entity;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.events.LauncherSetTargetEvent;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.launcher.ILauncherSolution;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.config.ConfigMain;
import com.createicbm.content.blocks.launcher.LauncherLangs;
import com.createicbm.content.blocks.launcher.LauncherSolution;
import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.blocks.launcher.network.ILauncherComponent;
import com.createicbm.content.blocks.launcher.network.LauncherEntry;
import com.createicbm.content.blocks.launcher.network.LauncherNode;
import com.createicbm.content.blocks.launcher.screen.gui.ContainerLaunchScreen;
import com.createicbm.content.missile.logic.targeting.BasicTargetData;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.data.IMachineInfo;
import com.createicbm.lib.network.lambda.PacketCodexReg;
import com.createicbm.lib.network.lambda.tile.PacketCodexTile;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.tile.TickAction;
import com.createicbm.lib.world.MachineChunkLoader;
import com.createicbm.prefab.gui.IPlayerUsing;
import com.createicbm.prefab.tile.IGuiTile;
import com.createicbm.prefab.tile.TileMachine;
import lombok.Getter;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileMachine implements MachineChunkLoader.IRadioMachine, ILauncherComponent, IMachineInfo, IPlayerUsing, IGuiTile, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "launcherscreen");

    /** Ticks between checks that the screen is still on its launcher network and radio */
    private static final int RECONNECT_TICKS = 40;

    /** Target position of the launcher */
    private Vec3 _targetPos = Vec3.ZERO;

    private final LauncherNode launcherNode = new LauncherNode(this, false);
    public final RadioScreen radioCap = new RadioScreen(this);

    @Getter
    private float launcherInaccuracy = 0;

    private final List<LauncherPair> statusList = new ArrayList<>();
    private boolean refreshStatus = false;

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileLauncherScreen(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_LAUNCHER_SCREEN.get(), pos, state);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20,true,  (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerLaunchScreen));
        }));
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER);
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        // Before the tick actions, so the first GUI packet after opening already carries the status list
        if (isServer())
        {
            updateStatusList();
        }

        super.update();

        if (isServer())
        {
            // A launcher network or radio map that was not ready when this screen loaded (chunk borders, a
            // neighbour loading later, a dimension reload) used to leave the screen deaf until it was replaced
            if (Math.floorMod(ticks + getBlockPos().hashCode(), RECONNECT_TICKS) == 0)
            {
                if (getNetworkNode().getNetwork() == null && !getNetworkNode().isInvalid())
                {
                    getNetworkNode().connectToTiles();
                }
                RadioRegistry.add(radioCap);
                syncMachineChunks();
            }

            //Only launch if redstone
            if (ticks % 3 == 0 && level.getDirectSignalTo(getBlockPos()) > 0) //TODO check for pulse instead of high only & add delay
            {
                fireAllLaunchers(false);
            }
        }
    }

    private void updateStatusList()
    {
        // PERF: the status list only feeds the GUI packet, so do not pre-check every launcher while nobody has the GUI open
        if (playersUsing.isEmpty()) {
            refreshStatus = true; // refresh on the first tick someone has it open
        }
        else if(ticks % 5 == 0 || refreshStatus) {
            refreshStatus = false;
            statusList.clear();

            final List<LauncherEntry> launchers = getLaunchersInGroup();
            if(!launchers.isEmpty()) {
                final int launcherCount = launchers.size();
                launcherInaccuracy = launchers.stream().map(LauncherEntry::getLauncher).map(l -> {

                    // Collect status
                    final IActionStatus status = preCheck(l);
                    statusList.add(new LauncherPair(l.getLauncherGroup(), l.getLaunchIndex(), status));

                    // Get accuracy for compare
                    return l.getInaccuracy(getTarget(), launcherCount);

                }).max(Float::compareTo).orElse(0f);
            }
        }
    }

    @Nonnull
    public List<LauncherEntry> getLaunchersInGroup() {
        if(getNetworkNode().getNetwork() != null) {
            return getNetworkNode().getNetwork().getLaunchers(getFiringGroup());
        }
        return Collections.emptyList();
    }

    public int getFiringGroup() {
        return -1;
    }

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_GUI = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .nodeString(t -> t.radioCap.getChannel(), (t, f) -> t.radioCap.setChannel(f))
        .nodeBoolean(t -> t.radioCap.isDisabled(), (t, f) -> t.radioCap.setDisabled(f))
        .nodeFloat(t -> t.launcherInaccuracy, (t, f) -> t.launcherInaccuracy = f)
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .nodeNbtCompound(t -> {
            final CompoundTag tag = new CompoundTag(); //TODO find a way to send bytes
            final ListTag list = new ListTag();
            t.statusList.forEach((pair) -> {
                final CompoundTag save = new CompoundTag();
                save.putInt("g", pair.getGroup());
                save.putInt("i", pair.getIndex());
                save.put("p", CreateICBMAPI.ACTION_STATUS_REGISTRY.save(pair.getStatus()));
                list.add(save);
            });
            tag.put("p", list);
            return tag;
        }, (t, tag) -> {
            final ListTag list = tag.getList("p", 10);
            final List<LauncherPair> status = new ArrayList<>(list.size());
            for(int i = 0; i < list.size(); i++) {
                final CompoundTag save = (CompoundTag) list.get(i);
                final int group = save.getInt("g");
                final int index = save.getInt("i");
                final CompoundTag partSave = save.getCompound("p");
                final IActionStatus part = CreateICBMAPI.ACTION_STATUS_REGISTRY.load(partSave);
                if(part != null) {
                    status.add(new LauncherPair(group, index, part));
                }
            }

            t.statusList.clear();
            t.statusList.addAll(status);
        });

    public static final PacketCodexTile<TileLauncherScreen, RadioScreen> PACKET_RADIO_HZ = (PacketCodexTile<TileLauncherScreen, RadioScreen>) new PacketCodexTile<TileLauncherScreen, RadioScreen>(REGISTRY_NAME, "radio.frequency", (t) -> t.radioCap)
        .fromClient()
        .nodeString(RadioScreen::getChannel, RadioScreen::setChannel)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, RadioScreen> PACKET_RADIO_DISABLE = (PacketCodexTile<TileLauncherScreen, RadioScreen>) new PacketCodexTile<TileLauncherScreen, RadioScreen>(REGISTRY_NAME, "radio.disable", (t) -> t.radioCap)
        .fromClient()
        .toggleBoolean(RadioScreen::isDisabled, RadioScreen::setDisabled)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_TARGET = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_LAUNCH = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((r, t, p) -> {
            r.fireAllLaunchers(false, p);
            r.setChanged();
        });

    /**
     * Pre-check of the launch process
     *
     * @param launcher to use
     * @return status
     */
    public IActionStatus preCheck(IMissileLauncher launcher)
    {
        return launcher.preCheckLaunch(new BasicTargetData(this.getTarget()), createCause());
    }

    private IActionCause createCause() {
        return new BlockScreenCause(level, getBlockPos(), getBlockState()); //TODO cache?
    }

    public boolean fireAllLaunchers(boolean simulate) {
        return fireAllLaunchers(simulate, null);
    }

    /**
     * @param user who pressed launch or sent the radio signal, recorded as the sender of the missiles
     */
    public boolean fireAllLaunchers(boolean simulate, @Nullable Entity user) {
        refreshStatus = true;

        if(getNetworkNode().getNetwork() == null) {
            return false; //TODO return error status
        }

        final List<LauncherEntry> launchers = getLaunchersInGroup();

        final IActionCause cause = createCause();
        if (user != null) {
            cause.setPreviousCause(new EntityCause(user));
        }
        final IMissileTarget target = new BasicTargetData(getTarget());
        final ILauncherSolution solution = new LauncherSolution(target, getFiringGroup(), launchers.size());
        return getNetworkNode().getNetwork()
            .launch(solution, cause, simulate)
            // Fired, or queued behind its firing delay / aim. Refused and cancelled launches don't count.
            .filter(LauncherEntry::wasLastLaunchAccepted)
            // count is required, as anyMatch() or similar will short-circuit before running all
            .count() > 0;
    }

    /**
     * Client side check for canLaunch
     *
     * @return true if current status list contains no blocking
     */
    public boolean canLaunch() {
        return !statusList.isEmpty() && statusList.stream().map(LauncherPair::getStatus).noneMatch(IActionStatus::isBlocking);
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    public Component getStatusTranslation()
    {
        // Network isn't setup
        if (getNetworkNode().getNetwork() == null)
        {
            return LauncherLangs.TRANSLATION_ERROR_NO_NETWORK;
        }
        // No launcher is connected yet
        else if(getLaunchersInGroup().isEmpty()) {
            return LauncherLangs.TRANSLATION_ERROR_NO_LAUNCHER;
        }
        // Generally only fails client side when status list is missing
        else if(statusList.isEmpty()) {
            return LauncherLangs.TRANSLATION_ERROR_NO_NETWORK_STATUS;
        }
        final List<LauncherPair> errors = statusList.stream().filter(pair -> pair.getStatus().isBlocking()).collect(Collectors.toList());
        if(errors.isEmpty()) {
            return LauncherLangs.TRANSLATION_READY;
        }
        final MutableComponent status = Component.translatable(LauncherLangs.ERROR_MISSILE_MULTI, errors.size(), statusList.size());
        for(int i = 0; i < errors.size() && i < 5; i++) {
            status.append(" \n \t ");
            status.append(errors.get(i).getStatus().message());
        }
        return status;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.launcherscreen.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerLaunchScreen(windowId, player, this);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        getNetworkNode().connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    /**
     * Keeps this screen and its launch pads loaded while it listens on a radio channel and a pad holds a missile, so
     * the Laser Designator, Remote Detonator and radars reach it with no player nearby. Every machine gets a random
     * channel when placed, so an unarmed site would otherwise stay loaded for nothing.
     */
    private void syncMachineChunks()
    {
        final LongSet wanted = new LongOpenHashSet();
        if (!RadioRegistry.EMPTY_HZ.equals(radioCap.getChannel()) && getNetworkNode().getNetwork() != null
            && getNetworkNode().getNetwork().streamLaunchers(-1).anyMatch(entry -> entry.getHost() instanceof TileLauncherBase pad
                && !pad.isRemoved() && !pad.getMissileStack().isEmpty()))
        {
            MachineChunkLoader.addArea(wanted, getBlockPos());
            getNetworkNode().getNetwork().streamLaunchers(-1)
                .map(LauncherEntry::getHost)
                .filter(host -> host != null && !host.isRemoved())
                .forEach(host -> MachineChunkLoader.addArea(wanted, host.getBlockPos()));
        }
        MachineChunkLoader.sync(this, wanted);
    }

    @Override
    public void setRemoved()
    {
        if(isServer()) {
            RadioRegistry.remove(radioCap);
        }
        getNetworkNode().onTileRemoved();
        super.setRemoved();
    }

    public Vec3 getTarget()
    {
        return this._targetPos;
    }

    /**
     * Called to set the target
     *
     * @param target
     */
    public void setTarget(Vec3 target)
    {
        if(target != this._targetPos) {

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
        }
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        if(capability == CreateICBMAPI.RADIO_CAPABILITY) {
            return (T) radioCap;
        }
        else if (getNetworkNode().getNetwork() != null)
        {
            return getNetworkNode().getNetwork().getCapability(capability, facing);
        }
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        SAVE_LOGIC.load(this, nbt);

        if(nbt.contains(NBTConstants.FREQUENCY)) {
            this.radioCap.setChannel(Integer.toString(nbt.getInt(NBTConstants.FREQUENCY)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        SAVE_LOGIC.save(this, nbt);
        super.saveAdditional(nbt, registries);
    }

    private static final NbtSaveHandler<TileLauncherScreen> SAVE_LOGIC = new NbtSaveHandler<TileLauncherScreen>()
        .mainRoot()
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radioCap)
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        .base();
}
