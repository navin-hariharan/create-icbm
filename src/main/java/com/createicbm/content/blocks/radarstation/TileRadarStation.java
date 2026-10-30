package com.createicbm.content.blocks.radarstation;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.radio.IRadioChannelAccess;
import com.createicbm.config.ConfigMain;
import com.createicbm.config.machines.ConfigRadar;
import com.createicbm.content.blocks.radarstation.data.RadarRenderData;
import com.createicbm.content.blocks.radarstation.data.RadarRenderDot;
import com.createicbm.content.blocks.radarstation.gui.ContainerRadarStation;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.CapabilityMissile;
import com.createicbm.content.missile.entity.anti.EntitySurfaceToAirMissile;
import com.createicbm.content.missile.tracker.MissileTrackerData;
import com.createicbm.content.missile.tracker.MissileTrackerHandler;
import com.createicbm.content.missile.tracker.MissileTrackerWorld;
import com.createicbm.content.reg.BlockReg;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.data.IMachineInfo;
import com.createicbm.lib.diagnostics.ICBMDiagnostics;
import com.createicbm.lib.network.lambda.GeneralCodexs;
import com.createicbm.lib.network.lambda.PacketCodex;
import com.createicbm.lib.network.lambda.PacketCodexReg;
import com.createicbm.lib.network.lambda.tile.PacketCodexTile;
import com.createicbm.lib.radar.RadarRegistry;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.lib.radio.imp.Radio;
import com.createicbm.lib.radio.messages.CounterstrikeMessage;
import com.createicbm.lib.radio.messages.IncomingMissileMessage;
import com.createicbm.lib.radio.messages.IncomingMissileWarningMessage;
import com.createicbm.lib.radio.messages.RadioTranslations;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.lib.tile.TickAction;
import com.createicbm.lib.tile.TickDoOnce;
import com.createicbm.lib.world.MachineChunkLoader;
import com.createicbm.prefab.gui.IPlayerUsing;
import com.createicbm.prefab.tile.IGuiTile;
import com.createicbm.prefab.tile.TileKineticMachine;
import com.simibubi.create.content.kinetics.KineticNetwork;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.Setter;
import com.createicbm.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class TileRadarStation extends TileKineticMachine implements MachineChunkLoader.IRadioMachine, IMachineInfo, IGuiTile, IPlayerUsing, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "radarstation");

    public static final Component TRANSLATION_GUI_NAME = Component.translatable("gui.create_icbm:radar.name");
    public static final Component TRANSLATION_TOOLTIP_RANGE = Component.translatable("gui.create_icbm:radar.range");
    public static final Component TRANSLATION_TOOLTIP_RANGE_SHIFT = Component.translatable("gui.create_icbm:radar.range.shift");
    public static final Component TRANSLATION_TOOLTIP_ADVANCED_RANGE_SHIFT = Component.translatable("gui.create_icbm:advanced_radar.range.shift");
    public static final Component TRANSLATION_TOOLTIP_REDSTONE_OFF = Component.translatable("gui.create_icbm:radar.redstone.off");
    public static final Component TRANSLATION_TOOLTIP_REDSTONE_ON = Component.translatable("gui.create_icbm:radar.redstone.on");

    public static final String NBT_DETECTION_RANGE = "detection_range"; //TODO fix name
    public static final String NBT_TRIGGER_RANGE = "safetyRadius"; //TODO fix name
    public static final String NBT_OUTPUT_REDSTONE = "emitAll"; //TODO fix name
    public static final String NBT_INVENTORY = "inventory";
    public static final String NBT_RADIO = "radio";

    /** Maximum horizontal detection radius, in blocks. */
    @Getter
    private int detectionRange = 100;

    /** Horizontal radius that counts as a protected target area. */
    @Getter
    private int triggerRange = 50;

    /** True if we should output redstone */
    @Getter @Setter
    private boolean outputRedstone = true;

    /** All detected threats in our radar range*/
    @Getter
    private final List<Entity> detectedThreats = new ArrayList<Entity>();
    /** Threats that will cause harm to our protection area */
    @Getter
    private final List<IMissile> incomingThreats = new ArrayList<>(); //TODO decouple from missile so we can track other entities

    @Getter
    private final RadarRenderData radarRenderData = new RadarRenderData(this);

    @Getter
    private final RadioRadar radio = new RadioRadar(this);

    private EnumRadarState radarVisualState = EnumRadarState.OFF;
    private EnumRadarState preRadarVisualState = EnumRadarState.OFF;

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    private int firingCooldown = 0;

    /** Ticks between early warnings about missiles still out of sight */
    private static final int WARNING_TICKS = 20;
    /** Seconds before arrival a missile still out of sight is announced to the Cruise Turrets */
    private static final int WARNING_SECONDS = 15;
    /** Game time each incoming missile was last reported to the Cruise Turrets */
    private final Map<UUID, Long> airDefenceAlerts = new HashMap<>();

    /** Missiles whose launch coordinates have already produced a successful counter-strike. */
    private final Set<UUID> counterstrikeFired = new LinkedHashSet<>();
    /** Radio callbacks are synchronous in the current network; this correlates a response to its missile. */
    private UUID pendingCounterstrikeId;

    /** Ticks to wait before trying again when nobody accepted a counter-strike */
    private static final int COUNTERSTRIKE_RETRY_TICKS = 20;
    /** Unanswered tries per missile before the radar gives up on it */
    private static final int COUNTERSTRIKE_MAX_ATTEMPTS = 30;
    /** Failed tries per missile, cleared when the missile leaves the threat list */
    private final Map<UUID, Integer> counterstrikeAttempts = new HashMap<>();
    /** Answers to the counter-strike being sent, only counted while it is being sent */
    private boolean collectingCounterstrike;
    private int counterstrikeAccepted;
    private int counterstrikeRefused;

    public TileRadarStation(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_RADAR_STATION.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerRadarStation));
        }));
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
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER);
        consumer.accept("MAX_RANGE", ConfigRadar.MAX_RANGE);
    }

    /**
     * Keeps this radar loaded while it reports on a radio channel, so it sees missiles and calls SAMs and
     * counter-strikes with no player nearby
     */
    private void syncMachineChunks()
    {
        final LongSet wanted = new LongOpenHashSet();
        if (!RadioRegistry.EMPTY_HZ.equals(radio.getChannel()))
        {
            MachineChunkLoader.addArea(wanted, getBlockPos());
        }
        MachineChunkLoader.sync(this, wanted);
    }

    @Override
    public void update()
    {
        super.update();

        if (isServer())
        {
            // PERF: offset by position so machines loaded together do not all check on the same tick
            if (Math.floorMod(ticks + getBlockPos().hashCode(), MachineChunkLoader.SYNC_TICKS) == 0)
            {
                syncMachineChunks();
            }

            // Scans only while the dish turns fast enough
            final boolean hasPower = isRunning();

            if (hasPower)
            {
                // Do a radar scan
                // PERF: offset by position so radars loaded on the same tick do not all scan on the same tick
                if (Math.floorMod(ticks + getBlockPos().hashCode(), Math.max(1, ConfigRadar.SCAN_TICKS)) == 0)
                {
                    this.doScan(); //TODO consider rewriting to not cache targets
                }

                // Drop missiles that died since the last scan so SAMs are not fired at them
                this.incomingThreats.removeIf(m -> m.getMissileEntity() == null || !m.getMissileEntity().isAlive());

                // Counter-strike cooldown runs down all the time, it used to stop while the sky was clear and a
                // new attack then waited out what was left of the last one
                if (firingCooldown > 0)
                {
                    firingCooldown--;
                }

                if (!radio.getChannel().equals(RadioRegistry.EMPTY_HZ))
                {
                    if (!this.incomingThreats.isEmpty())
                    {
                        // Both radars call the Cruise Turrets on their channel to fire SAMs
                        alertAirDefence();

                        // Only the Advanced Radar also sends the Launch Controllers on its channel to the launch site
                        if (isAdvancedRadar() && firingCooldown <= 0)
                        {
                            // Accepted waits the full cooldown, unanswered or refused tries again soon
                            final int cooldown = sendCounterstrike();
                            if (cooldown > 0)
                            {
                                firingCooldown = cooldown;
                            }
                        }
                    }
                    else if (Math.floorMod(ticks + getBlockPos().hashCode(), WARNING_TICKS) == 0)
                    {
                        warnAirDefence();
                    }
                }
            }
            // No power, reset state
            else
            {
                incomingThreats.clear();
                detectedThreats.clear();
                radarRenderData.clear();
            }

            //Update redstone state
            final boolean shouldBeOn = hasPower && hasIncomingMissiles();

            // Track our state server side
            this.radarVisualState = getRadarState(hasPower);

            final BlockState currentState = getBlockState();
            if (currentState.hasProperty(BlockRadarStation.REDSTONE_PROPERTY)
                && (currentState.getValue(BlockRadarStation.REDSTONE_PROPERTY) != shouldBeOn || currentState.getValue(BlockRadarStation.RADAR_STATE) != radarVisualState))
            {
                final boolean redstoneChanged = currentState.getValue(BlockRadarStation.REDSTONE_PROPERTY) != shouldBeOn;
                final BlockPos selfPos = getBlockPos();
                level.setBlock(selfPos, currentState
                    .setValue(BlockRadarStation.REDSTONE_PROPERTY, shouldBeOn)
                    .setValue(BlockRadarStation.RADAR_STATE, radarVisualState), 3);

                if (redstoneChanged)
                {
                    for (Direction facing : Direction.values())
                    {
                        final BlockPos targetPos = selfPos.relative(facing);
                        level.neighborChanged(targetPos, getBlockState().getBlock(), selfPos);
                        level.updateNeighborsAtExceptFromFacing(targetPos, getBlockState().getBlock(), facing.getOpposite());
                    }
                }
            }

            if (preRadarVisualState != radarVisualState) {
                this.setChanged();
                preRadarVisualState = radarVisualState;
            }
        }
    }

    private void doScan() //TODO document and thread
    {
        this.incomingThreats.clear();
        this.detectedThreats.clear();
        // PERF: no description packet per scan, update() calls setChanged (which sends it) when the visual state changes
        this.radarRenderData.clear();

        final List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, Math.min(detectionRange, ConfigRadar.MAX_RANGE));

        // Loop list of contacts to ID threats
        for (Entity entity : entities)
        {
            if (isThreat(entity))
            {
                final IMissile newMissile = CreateICBMHelpers.getMissile(entity);
                if (newMissile != null && newMissile.getTicksInAir() > 1)
                {
                    // Friend or foe: missiles launched on this radar's channel are shown but never reported
                    if (!CapabilityMissile.isFriendly(entity, radio.getChannel()) && this.isMissileGoingToHit(newMissile))
                    {
                        this.incomingThreats.add(newMissile);
                    }
                    else {
                        this.detectedThreats.add(entity);
                    }
                }
            }
        }

        if(!this.incomingThreats.isEmpty()) {
            this.incomingThreats.sort(this::compare);
        }

        // Forget retry counts of missiles that are no longer coming at us
        if (!counterstrikeAttempts.isEmpty())
        {
            final Set<UUID> current = new HashSet<>();
            for (IMissile missile : incomingThreats)
            {
                if (missile.getMissileEntity() != null)
                {
                    current.add(missile.getMissileEntity().getUUID());
                }
            }
            counterstrikeAttempts.keySet().retainAll(current);
        }
        if (!airDefenceAlerts.isEmpty())
        {
            final Set<UUID> current = new HashSet<>();
            for (IMissile missile : incomingThreats)
            {
                if (missile.getMissileEntity() != null)
                {
                    current.add(missile.getMissileEntity().getUUID());
                }
            }
            airDefenceAlerts.keySet().retainAll(current);
        }

        if(!this.detectedThreats.isEmpty()) {
            this.detectedThreats.sort(this::compare);
        }

        // Only update render data if we have players viewing the UI
        if(!this.getPlayersUsing().isEmpty()) {
            radarRenderData.update();
        }
    }

    private int compare(IMissile a, IMissile b) {
        return compare(a.getMissileEntity(), b.getMissileEntity());
    }

    private int compare(Entity a, Entity b) {
        return Integer.compare(distance(a), distance(b));
    }

    private int distance(Entity entity) {
        final BlockPos pos = getBlockPos();
        final int deltaX = (int)Math.floor(Math.abs(pos.getX() + 0.5 - entity.getX()));
        final int deltaY = (int)Math.floor(Math.abs(pos.getY() + 0.5 - entity.getY()));
        final int deltaZ = (int)Math.floor(Math.abs(pos.getZ() + 0.5 - entity.getZ()));

        return deltaX + deltaY + deltaZ;
    }

    public static boolean isThreat(Entity entity)
    {
        // TODO let users customize threat list
        return entity != null
            // Ignore SAM missiles
            && !(entity instanceof EntitySurfaceToAirMissile)
            // Track explosive missiles (using caps to allow other mods to interact more easily)
            && entity.getCapability(CreateICBMAPI.MISSILE_CAPABILITY) != null
            && entity.getCapability(CreateICBMAPI.EXPLOSIVE_ENTITY_CAPABILITY) != null; //TODO recode to use a radar classification system
    }

    /**
     * Checks to see if the missile will hit within the range of the radar station
     *
     * @param missile - missile being checked
     * @return true if it will
     */
    public boolean isMissileGoingToHit(IMissile missile)
    {
        if (missile == null || missile.getMissileEntity() == null || !missile.getMissileEntity().isAlive())
        {
            return false;
        }
        // Incoming means closing in on the radar. Asking for the landing spot to be inside the trigger range stopped
        // alerts for missiles aimed at anything near the radar but its own spot, and turrets never fired their SAMs.
        final Entity entity = missile.getMissileEntity();
        final Vec3 radarPos = new Vec3(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
        final Vec3 missilePos = entity.position();
        final Vec3 nextPos = missilePos.add(entity.getDeltaMovement());
        if (!isFinite(missilePos) || !isFinite(nextPos))
        {
            return false;
        }
        return nextPos.distanceToSqr(radarPos) < missilePos.distanceToSqr(radarPos);
    }

    /** Detection range drives both the scan radius and Create stress load. */
    public void setDetectionRange(int range)
    {
        final int clamped = Math.max(1, Math.min(ConfigRadar.MAX_RANGE, range));
        if (detectionRange == clamped)
        {
            return;
        }
        detectionRange = clamped;
        if (triggerRange > detectionRange)
        {
            triggerRange = detectionRange;
        }
        refreshKineticStress();
        if (level != null && !level.isClientSide())
        {
            setChanged();
        }
    }

    public void setTriggerRange(int range)
    {
        final int clamped = Math.max(1, Math.min(detectionRange, range));
        if (triggerRange == clamped)
        {
            return;
        }
        triggerRange = clamped;
        if (level != null && !level.isClientSide())
        {
            setChanged();
        }
    }

    /** Advanced mode is selected by the registered block, not a second block-entity implementation. */
    public boolean isAdvancedRadar()
    {
        return getBlockState().is(BlockReg.blockAdvancedRadar.get());
    }

    /** More detection distance costs proportionally more kinetic stress (100 m = configured base load). */
    @Override
    public float calculateStressApplied()
    {
        final float scaledStress = super.calculateStressApplied() * (detectionRange / 100f);
        // Create uses this cached value when the network is persisted and restored.
        this.lastStressApplied = scaledStress;
        return scaledStress;
    }

    private void refreshKineticStress()
    {
        if (isServer() && hasNetwork())
        {
            final KineticNetwork network = getOrCreateNetwork();
            if (network != null)
            {
                network.updateStressFor(this, calculateStressApplied());
            }
        }
    }

    /**
     * Calls the Cruise Turrets on this channel to fire at the closest incoming missile. A missile seen for the first
     * time is reported at once, the old fixed delay between reports let a falling missile land before any turret
     * heard of it. The same missile is reported again every {@link ConfigRadar#SAM_TICKS} so turrets follow it and
     * fire again if a SAM missed.
     */
    private void alertAirDefence()
    {
        final long now = level.getGameTime();
        for (IMissile missile : incomingThreats)
        {
            final Entity entity = missile.getMissileEntity();
            if (entity == null || !entity.isAlive())
            {
                continue;
            }
            final Long last = airDefenceAlerts.get(entity.getUUID());
            if (last != null && now - last < Math.max(1, ConfigRadar.SAM_TICKS))
            {
                // Reported a moment ago, name the next missile so a wave is spread over the turrets instead of every
                //  turret only ever hearing about the closest one
                continue;
            }
            airDefenceAlerts.put(entity.getUUID(), now);
            RadioRegistry.popMessage(level, radio, new IncomingMissileMessage(radio.getChannel(), missile, true));
            ICBMDiagnostics.info("RADAR_SAM_ALERT radar={} channel={} missile={} first={}",
                getBlockPos(), radio.getChannel(), entity.getUUID(), last == null);
            // One report per tick, closest first
            return;
        }
    }

    /**
     * Warns the Cruise Turrets on this channel about a missile still crossing the map out of sight that will come
     * down inside the detection range, so they turn towards the point where it will appear and are ready to fire.
     */
    private void warnAirDefence()
    {
        final MissileTrackerWorld tracker = MissileTrackerHandler.getOrCreateHandler(level, false);
        if (tracker == null)
        {
            return;
        }
        final double range = Math.min(detectionRange, ConfigRadar.MAX_RANGE);
        final double radarX = getBlockPos().getX() + 0.5;
        final double radarZ = getBlockPos().getZ() + 0.5;
        final String channel = radio.getChannel();
        final MissileTrackerData[] closest = new MissileTrackerData[1];
        tracker.forEachSimulated(data -> {
            if (data.targetPos == null || channel.equals(data.radioChannel)
                || data.ticksLeftToTarget > WARNING_SECONDS
                || Math.abs(data.targetPos.x() - radarX) > range || Math.abs(data.targetPos.z() - radarZ) > range)
            {
                return;
            }
            if (closest[0] == null || data.ticksLeftToTarget < closest[0].ticksLeftToTarget)
            {
                closest[0] = data;
            }
        });
        if (closest[0] != null)
        {
            // The tracker brings it back at the simulation enter height straight above its target
            final Vec3 entry = new Vec3(closest[0].targetPos.x(), ConfigMissile.SIMULATION_ENTER_HEIGHT, closest[0].targetPos.z());
            RadioRegistry.popMessage(level, radio, new IncomingMissileWarningMessage(channel, entry));
            ICBMDiagnostics.info("RADAR_SAM_WARNING radar={} channel={} entry={} seconds={}",
                getBlockPos(), channel, entry, closest[0].ticksLeftToTarget);
        }
    }

    /**
     * Sends one counter-strike at the launch site of the closest missile not yet answered.
     *
     * @return ticks to wait before the next try, 0 when there was nothing to strike
     */
    private int sendCounterstrike()
    {
        for (IMissile missile : incomingThreats)
        {
            final Entity entity = missile.getMissileEntity();
            if (entity == null || !entity.isAlive() || counterstrikeFired.contains(entity.getUUID()))
            {
                continue;
            }

            final IActionSource source = missile.getMissileSource();
            if (source == null || source.getWorld() == null || source.getPosition() == null
                || !source.getWorld().dimension().equals(level.dimension()) || !isFinite(source.getPosition()))
            {
                // A source in another dimension cannot be attacked by a local launchpad.
                continue;
            }

            final UUID id = entity.getUUID();
            final int attempts = counterstrikeAttempts.getOrDefault(id, 0);
            if (attempts >= COUNTERSTRIKE_MAX_ATTEMPTS)
            {
                // Gave up on this one, try the next missile
                continue;
            }

            // Launch screens on this channel update their target and fire. Only a screen or cruise launcher that
            // actually queued or fired a launch counts, everything else is retried soon instead of after the cooldown.
            pendingCounterstrikeId = id;
            counterstrikeAccepted = 0;
            counterstrikeRefused = 0;
            collectingCounterstrike = true;
            final int delivered;
            try
            {
                delivered = RadioRegistry.popMessage(level, radio, new CounterstrikeMessage(radio.getChannel(), source.getPosition()));
            }
            finally
            {
                collectingCounterstrike = false;
            }

            if (counterstrikeAccepted > 0)
            {
                counterstrikeAttempts.remove(id);
                markCounterstrikeFired(id);
                ICBMDiagnostics.info("RADAR_COUNTERSTRIKE_ACCEPTED radar={} channel={} target={} accepted={} receivers={}",
                    getBlockPos(), radio.getChannel(), source.getPosition(), counterstrikeAccepted, delivered);
                return Math.max(1, ConfigRadar.SAM_TICKS);
            }

            final int attempt = attempts + 1;
            counterstrikeAttempts.put(id, attempt);
            ICBMDiagnostics.warn("RADAR_COUNTERSTRIKE_UNANSWERED radar={} channel={} target={} receivers={} refused={} attempt={}/{}",
                getBlockPos(), radio.getChannel(), source.getPosition(), delivered, counterstrikeRefused, attempt, COUNTERSTRIKE_MAX_ATTEMPTS);
            if (attempt >= COUNTERSTRIKE_MAX_ATTEMPTS)
            {
                CreateICBM.logger().warn("Advanced radar at {} gave up on a counter-strike on channel '{}' after {} tries: {} receivers, none launched."
                    + " Check that a launch screen or cruise launcher on that channel is linked to a loaded, armed launcher.",
                    getBlockPos(), radio.getChannel(), attempt, delivered);
            }
            return COUNTERSTRIKE_RETRY_TICKS;
        }
        return 0;
    }

    void onCounterstrikeResponse(String message)
    {
        if (!collectingCounterstrike || pendingCounterstrikeId == null)
        {
            return;
        }
        // Launch screens answer SUCCESS, cruise launchers answer TRIGGERED once they queued the shot
        if (RadioTranslations.RADIO_LAUNCH_SUCCESS.equals(message) || RadioTranslations.RADIO_LAUNCH_TRIGGERED.equals(message))
        {
            counterstrikeAccepted++;
        }
        else if (!RadioTranslations.RADIO_TARGET_SET.equals(message))
        {
            // Failed launch, busy cruise launcher, missing target. Target set alone is only a step on the way.
            counterstrikeRefused++;
        }
    }

    private void markCounterstrikeFired(UUID id)
    {
        counterstrikeFired.add(id);
        // Keep this set bounded for long-running servers.
        while (counterstrikeFired.size() > 1024)
        {
            final Iterator<UUID> iterator = counterstrikeFired.iterator();
            if (iterator.hasNext())
            {
                iterator.next();
                iterator.remove();
            }
        }
    }

    private static boolean isFinite(@Nullable Vec3 position)
    {
        return position != null && Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }

    public EnumRadarState getRadarState() {

        if(isClient()) {
            return radarVisualState;
        }
        return getRadarState(isRunning());
    }

    /** Visual state for the given running state, OFF when the dish is not turning fast enough */
    protected EnumRadarState getRadarState(boolean hasPower) {

        if(!hasPower) {
            return EnumRadarState.OFF;
        }
        else if(this.incomingThreats.size() > 0) {
            return EnumRadarState.DANGER;
        }
        else  if(this.detectedThreats.size() > 0) {
            return EnumRadarState.WARNING;
        }
        return EnumRadarState.ON;
    }

    public int getStrongRedstonePower(Direction side)
    {
        if (this.outputRedstone && incomingThreats.size() > 0) //TODO add UI customization to pick side of redstone output and minimal number of missiles to trigger
        {
            return Math.min(15, incomingThreats.size());
        }
        return 0;
    }

    @Override
    public float getMinimumSpeed()
    {
        return (float) ConfigRadar.MIN_SPEED;
    }

    /** Client copy of the radar state, synced by the description packet */
    public EnumRadarState getVisualState()
    {
        // The block state carries the same value and is synced on chunk load, the description packet is only sent on change
        final BlockState state = getBlockState();
        if (isClient() && state.hasProperty(BlockRadarStation.RADAR_STATE))
        {
            return state.getValue(BlockRadarStation.RADAR_STATE);
        }
        return radarVisualState;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking)
    {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        addGoggleHeader(tooltip, isAdvancedRadar() ? "advanced_radar" : "radar_station");
        final EnumRadarState state = getVisualState();
        final ChatFormatting colour = switch (state) {
            case OFF -> ChatFormatting.DARK_GRAY;
            case ON -> ChatFormatting.GREEN;
            case WARNING -> ChatFormatting.GOLD;
            case DANGER -> ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        };
        addGoggleLine(tooltip, "radar_state", Component.translatable("goggles." + ICBMConstants.PREFIX + "radar_state." + state.name().toLowerCase(java.util.Locale.ROOT)).getString(), colour);
        if (state == EnumRadarState.OFF && ConfigMain.REQUIRES_POWER)
        {
            addGoggleLine(tooltip, "needs_speed", Math.round(getMinimumSpeed()) + " RPM", ChatFormatting.GOLD);
        }
        addGoggleLine(tooltip, "radar_range", detectionRange + " m", ChatFormatting.WHITE);
        return true;
    }

    public boolean hasIncomingMissiles() {
        return incomingThreats.size() > 0;
    }

    @Override
    public Component getDisplayName()
    {
        return isAdvancedRadar()
            ? Component.translatable("gui.create_icbm:advanced_radar.name")
            : TRANSLATION_GUI_NAME;
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        // Create addon: no Forge Energy any more, the radar runs straight off its shaft. Its radio lets tools copy
        // the channel.
        if (capability == CreateICBMAPI.RADIO_CAPABILITY)
        {
            return (T) radio;
        }
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerRadarStation(windowId, player, this);
    }

    @Override
    protected void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket)
    {
        super.read(nbt, registries, clientPacket);
        SAVE_LOGIC.load(this, nbt);
        if(nbt.contains(NBTConstants.FREQUENCY)) {
            this.radio.setChannel(Integer.toString(nbt.getInt(NBTConstants.FREQUENCY)));
        }
    }

    @Override
    protected void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket)
    {
        SAVE_LOGIC.save(this, nbt);
        super.write(nbt, registries, clientPacket);
    }

    private static final NbtSaveHandler<TileRadarStation> SAVE_LOGIC = new NbtSaveHandler<TileRadarStation>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBT_RADIO, TileRadarStation::getRadio)
        /* */.nodeBoolean(NBT_OUTPUT_REDSTONE, TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone)
        /* */.nodeInteger(NBT_DETECTION_RANGE, TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange)
        /* */.nodeInteger(NBT_TRIGGER_RANGE, TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange)
        /* */.nodeInteger("sam_delay", tile -> tile.firingCooldown, (tile, i) -> tile.firingCooldown = i)
        .base();

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_GUI, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_DETECTION_RANGE, PACKET_TRIGGER_RANGE, PACKET_REDSTONE_OUTPUT);
    }

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_DESCRIPTION = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeEnum(EnumRadarState.class, (t) -> t.radarVisualState, (t, e) -> t.radarVisualState = e);

    public static final PacketCodex<TileRadarStation, IRadioChannelAccess> PACKET_RADIO_HZ = GeneralCodexs.radioChannel(REGISTRY_NAME, (tile) -> tile.radio);
    public static final PacketCodex<TileRadarStation, Radio> PACKET_RADIO_DISABLE = GeneralCodexs.radioToggleDisable(REGISTRY_NAME, (tile) -> tile.radio);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_TRIGGER_RANGE = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "range.trigger")
        .fromClient()
        .nodeInt(TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_DETECTION_RANGE = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "range.detection")
        .fromClient()
        .nodeInt(TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_REDSTONE_OUTPUT = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "redstone.output")
        .fromClient()
        .toggleBoolean(TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_GUI = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "gui")
        .fromServer()
        // Detection first so client-side trigger-range validation uses the newly synced outer range.
        .nodeInt(TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange)
        .nodeInt(TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange)
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeBoolean(TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone)
        .<List<RadarRenderDot>>node((t) -> new ArrayList<>(t.radarRenderData.getDots()), (t, dots) -> t.radarRenderData.setDots(dots), RadarRenderData::encodeDots, RadarRenderData::decodeDots);
}
