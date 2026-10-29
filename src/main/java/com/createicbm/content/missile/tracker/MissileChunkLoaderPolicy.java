package com.createicbm.content.missile.tracker;

/**
 * Dependency-free flight hand-off policy, kept separate from the Minecraft-facing chunk loader so it can be
 * exercised by ordinary unit tests without loading Minecraft or NeoForge classes.
 */
final class MissileChunkLoaderPolicy
{
    private MissileChunkLoaderPolicy()
    {
    }

    /**
     * Keep the physical missile's trajectory entity-ticking until the simulated-flight tracker can safely take over.
     * The path remains ticking if a player is watching it, if tracking is refused/unsupported, if no target is
     * available, or if the missile has not yet reached the configured simulation exit height.
     */
    static boolean shouldKeepArcFlightTickets(boolean simulationRefused, boolean watched, boolean canSimulate,
                                              boolean hasTarget, double y, double simulationExitHeight)
    {
        return simulationRefused || watched || !canSimulate || !hasTarget || y < simulationExitHeight;
    }
}
