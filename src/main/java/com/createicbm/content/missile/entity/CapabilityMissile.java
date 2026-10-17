package com.createicbm.content.missile.entity;

import com.createicbm.CreateICBM;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.ConfigDebug;
import com.createicbm.lib.CalculationHelpers;
import com.createicbm.lib.radar.RadarRegistry;
import com.createicbm.lib.saving.NbtSaveHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Objects;
import java.util.Optional;

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

    @Override
    public void launch()
    {
        //Tell missile to start moving
        this.doFlight = true;
        triggerFlightLogic();

        //Trigger events
        //TODO add generic event
        CreateICBMAPI.EX_MISSILE_REGISTRY.triggerLaunch(this);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this.missile); //TODO replace with capability and have radar system listen for entity spawn event

        //Play audio
        ICBMSounds.MISSILE_LAUNCH.play(world(), x(), y(), z(),
            1F, (1.0F + CalculationHelpers.randFloatRange(world().random, 0.2F)) * 0.7F, true);

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
        .base();

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
