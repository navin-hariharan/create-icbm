package com.createicbm.content.missile.entity;

import com.createicbm.CreateICBM;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.ConfigDebug;
import com.createicbm.content.blocks.mail.LauncherOwner;
import com.createicbm.content.missile.logic.source.MissileSender;
import com.createicbm.lib.CalculationHelpers;
import com.createicbm.lib.radar.RadarRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import com.createicbm.mods.sable.SableProxy;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityMissile implements IMissile, INBTSerializable<CompoundTag>
{
    private final EntityMissile missile;
    private IMissileTarget targetData;

    private IActionSource firingSource;

    private IMissileFlightLogic flightLogic;
    private boolean doFlight = false;

    /** Id of this flight, kept while the missile is simulated in unloaded chunks and spawned again at the target */
    private UUID flightId;

    /** Player who sent this missile, worked out once from the launch history and saved with the missile */
    private LauncherOwner sender;
    private boolean senderResolved = false;

    /** Launch sound of this missile and the game time it started, to cut it short if the missile blows up first */
    private long launchSoundKey = 0;
    private long launchSoundTime = 0;

    public CapabilityMissile(EntityMissile missile)
    {
        this.missile = missile;
    }

    @Override
    public int getTicksInAir()
    {
        return missile.ticksInAir;
    }

    @Override
    public Entity getMissileEntity()
    {
        return missile;
    }

    @Override
    public void setTargetData(IMissileTarget data) {
        this.targetData = data;
    }

    @Override
    public IMissileTarget getTargetData()
    {
        return this.targetData;
    }

    @Override
    public void setFlightLogic(IMissileFlightLogic logic)
    {
        this.missile.syncClient = true;
        this.flightLogic = logic;
    }

    @Override
    public void switchFlightLogic(IMissileFlightLogic logic) {
        if(ConfigDebug.DEBUG_MISSILE_LOGIC) {
            // TODO move to event system
            CreateICBM.logger().info(this + ": Switching flight logic from '" + getFlightLogic() + "' to '" + logic + "'");
        }
        setFlightLogic(logic);
        triggerFlightLogic();
    }

    protected void triggerFlightLogic() {
        if(flightLogic != null) {
            flightLogic.calculateFlightPath(world(), x(), y(), z(), getTargetData()); //TODO show in launcher screen with predicted path and time
            flightLogic.start(missile, this);
        }
    }

    @Override
    public IMissileFlightLogic getFlightLogic()
    {
        return this.flightLogic;
    }

    @Override
    public void setMissileSource(IActionSource source)
    {
        this.firingSource = source;
    }

    @Override
    public IActionSource getMissileSource()
    {
        return firingSource;
    }

    /**
     * @return id of this flight, created on first use
     */
    public UUID getFlightId()
    {
        if (flightId == null)
        {
            flightId = UUID.randomUUID();
        }
        return flightId;
    }

    /**
     * @return player who sent this missile, or null when nobody can be found
     */
    @Nullable
    public LauncherOwner getSender()
    {
        if (!senderResolved && firingSource != null)
        {
            senderResolved = true;
            sender = MissileSender.find(firingSource);
        }
        return sender;
    }

    @Override
    public void launch()
    {
        // A launcher on a Sable ship spawns its missile in the ship's plot, the flight path has to start from the world
        if (!world().isClientSide())
        {
            SableProxy.INSTANCE.moveToWorld(missile);
        }

        //Tell missile to start moving
        this.doFlight = true;
        triggerFlightLogic();

        if (!world().isClientSide())
        {
            getFlightId();
            getSender();
        }

        //Trigger events
        //TODO add generic event
        CreateICBMAPI.EX_MISSILE_REGISTRY.triggerLaunch(this);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this.missile); //TODO replace with capability and have radar system listen for entity spawn event

        //Play audio
        if (world().isClientSide())
        {
            ICBMSounds.MISSILE_LAUNCH.play(world(), x(), y(), z(),
                1F, (1.0F + CalculationHelpers.randFloatRange(world().random, 0.2F)) * 0.7F, true);
        }
        else
        {
            launchSoundKey = ICBMSounds.nextTrackedKey();
            launchSoundTime = world().getGameTime();
            ICBMSounds.MISSILE_LAUNCH.playTracked(world(), launchSoundKey, x(), y(), z(),
                1F, (1.0F + CalculationHelpers.randFloatRange(world().random, 0.2F)) * 0.7F);
        }

        if (ConfigDebug.DEBUG_MISSILE_LAUNCHES)
        {
            CreateICBM.logger().info(
                String.format("Missile(%s): Launch triggered of type[%s] starting from '%s' and aimed at `%s`",
                    this.missile.getId(),
                    this.missile.getName().getString(),
                    Optional.ofNullable(this.getMissileSource()).map(Objects::toString).orElse("'null'"),
                    Optional.ofNullable(this.getTargetData()).map(Objects::toString).orElse("'null'")
                )
            );
        }
    }

    /**
     * Fades out the launch sound if it is still playing, called when the missile is removed
     */
    public void stopLaunchSound()
    {
        // The longest launch sound is about 5 seconds, 9 at the lowest pitch
        if (launchSoundKey != 0 && world().getGameTime() - launchSoundTime < 200)
        {
            ICBMSounds.stopTracked(world(), launchSoundKey);
        }
        launchSoundKey = 0;
    }

    @Override
    public Level world()
    {
        return missile != null ? missile.level() : null;
    }

    @Override
    public double z()
    {
        return missile != null ? missile.getZ() : 0;
    }

    @Override
    public double x()
    {
        return missile != null ? missile.getX() : 0;
    }

    @Override
    public double y()
    {
        return missile != null ? missile.getY() : 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag saveData = new CompoundTag();
        SAVE_LOGIC.save(this, saveData);
        return saveData;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
       SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityMissile> SAVE_LOGIC = new NbtSaveHandler<CapabilityMissile>()
        .addRoot("flags")
        /* */.nodeBoolean("doFlight", CapabilityMissile::canRunFlightLogic, (cap, i) -> cap.doFlight = i)
        .base()
        .mainRoot()
        /* */.nodeBuildableObject("target", () -> CreateICBMAPI.MISSILE_TARGET_DATA_REGISTRY, CapabilityMissile::getTargetData, CapabilityMissile::setTargetData)
        /* */.nodeBuildableObject("flight", () -> CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY, CapabilityMissile::getFlightLogic, CapabilityMissile::setFlightLogic)
        /* */.nodeINBTSerializable("source", CapabilityMissile::getMissileSource)
        /* */.nodeUUID("flightId", (cap) -> cap.flightId, (cap, id) -> cap.flightId = id)
        /* */.nodeCompoundTag("sender", CapabilityMissile::saveSender, CapabilityMissile::loadSender)
        .base();

    @Nullable
    private CompoundTag saveSender()
    {
        if (sender == null)
        {
            return null;
        }
        final CompoundTag tag = new CompoundTag();
        tag.putUUID("id", sender.id());
        tag.putString("name", sender.name());
        return tag;
    }

    private void loadSender(CompoundTag tag)
    {
        senderResolved = true;
        sender = new LauncherOwner(tag.hasUUID("id") ? tag.getUUID("id") : new UUID(0, 0), tag.getString("name"));
    }

    public boolean canRunFlightLogic()
    {
        return doFlight;
    }

    @Override
    public int hashCode() {
        return missile != null ? missile.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof CapabilityMissile) {
            return Objects.equals(((CapabilityMissile) other).flightLogic, flightLogic)
                && Objects.equals(((CapabilityMissile) other).targetData, targetData)
                && ((CapabilityMissile) other).doFlight == doFlight;
        }
        return false;
    }
}
