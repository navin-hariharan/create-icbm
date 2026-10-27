package com.createicbm.content.missile.tracker;

/**
 * Flight logic that hands its missile over to the missile tracker before it reaches chunks where entities don't tick.
 * When the hand over is refused (another mod cancels it, or the missile has no target) the missile keeps flying as an
 * entity, and {@link MissileChunkLoader} keeps the chunks on its path ticking until the hand over works.
 */
public interface ISimulatedFlight
{
    /**
     * @return true while the last hand over to the tracker was refused and the missile flies on as an entity
     */
    boolean isSimulationRefused();
}
