package com.createicbm.content.missile.tracker;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Hands a missile over to the missile tracker for flight logics, see {@link ISimulatedFlight}. Create one with
 * {@link MissileTrackerHandler#newHandOver()}.
 * <p>
 * A refused hand over used to be saved as a flag that stopped every later try, so a missile that was refused once (an
 * event cancelled by another mod, a missing target) froze for good at the first chunk where entities don't tick. Now
 * it is tried again every few ticks and nothing is saved, and the missile's path is kept loaded meanwhile.
 * <p>
 * Plain Java on purpose so it can be unit tested without the game.
 *
 * @param <E> the missile entity
 */
public final class SimulationHandOver<E>
{
    /** Ticks between tries after a refused hand over */
    public static final int RETRY_TICKS = 40;

    private final Predicate<E> handOver;
    private final Consumer<E> onFirstRefusal;
    private int retryTicks = 0;
    private boolean refused = false;

    /**
     * @param handOver       gives the missile to the tracker, true if it was taken
     * @param onFirstRefusal called the first time the tracker refuses the missile, to log it
     */
    public SimulationHandOver(Predicate<E> handOver, Consumer<E> onFirstRefusal)
    {
        this.handOver = handOver;
        this.onFirstRefusal = onFirstRefusal;
    }

    /**
     * Called each server tick of the flight
     *
     * @param shouldSimulate true when the missile is about to fly where it would freeze
     * @return true if the missile was handed over this tick
     */
    public boolean tick(E entity, Predicate<E> shouldSimulate)
    {
        if (retryTicks > 0)
        {
            retryTicks--;
            return false;
        }
        if (!shouldSimulate.test(entity))
        {
            return false;
        }
        if (handOver.test(entity))
        {
            refused = false;
            return true;
        }
        if (!refused)
        {
            onFirstRefusal.accept(entity);
        }
        refused = true;
        retryTicks = RETRY_TICKS;
        return false;
    }

    /**
     * @see ISimulatedFlight#isSimulationRefused()
     */
    public boolean isRefused()
    {
        return refused;
    }
}
