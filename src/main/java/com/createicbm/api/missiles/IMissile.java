package com.createicbm.api.missiles;

import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.data.IWorldPosition;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import net.minecraft.world.entity.Entity;

/**
 * Capability added to entities to define them as missiles
 *
 * @author DarkGuardsman
 */
public interface IMissile extends IWorldPosition // TODO add registry and require each missile (ex combinations included) to have a key
{
    /**
     * The amount of ticks this missile has been flying for. Returns -1 if the missile is not
     * flying.
     */
    int getTicksInAir(); //TODO maybe change to a status? onGround, inAir, preFlight, impacted

    /**
     * Gets the entity that is host to this missile capability.
     *
     * @return entity
     */
    Entity getMissileEntity();

    /**
     * Sets the missile targeting data
     *
     * @param data defining the target and any specialized impact settings
     */
    void setTargetData(IMissileTarget data);

    IMissileTarget getTargetData();

    /**
     * Sets the flight logic to use for the missile
     * @param logic to use
     */
    void setFlightLogic(IMissileFlightLogic logic);

    /**
     * Called to switch the flight logic to a new logic or next step
     * in a flight path
     *
     * @param logic to use
     */
    default void switchFlightLogic(IMissileFlightLogic logic) {
        setFlightLogic(logic);
    }

    IMissileFlightLogic getFlightLogic();

    /**
     * Sets the missile source information
     * @param source
     */
    void setMissileSource(IActionSource source);

    /**
     * Gets the missile source
     *
     * Should not be exposed to the player
     *
     * @return source
     */
    IActionSource getMissileSource();

    /**
     * Tells the missile to start motion
     *
     * This will trigger flight logic to run calculation and lock in start conditions.
     * After which the entity will start moving and handle any updates as required.
     *
     * Once launched don't expect the flight logic or other systems to allow modifications.
     */
    void launch();

    /**
     * Undoes {@link #launch()} when the launched entity could not be added to the world (another mod refused the
     * spawn, or the chunk is not loaded). Stops the launch sound, takes the missile off the radar and discards the
     * entity, so a missile that never flew doesn't linger as a contact. Launch events that already fired can't be
     * taken back.
     * <p>
     * Does nothing by default, for missiles that have nothing to undo.
     */
    default void cancelLaunch()
    {
    }
}
