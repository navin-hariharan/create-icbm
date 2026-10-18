package com.createicbm.content.entity.phantom;

/**
 * Fake mob summoned by the phantom enemy missile. It chases a player and goes through the motions of attacking, but
 * never deals damage and vanishes in a puff of smoke the moment anything hits it. It leaves the world alone too: no
 * trampling, griefing, pressure plates, tripwires or rides, and it is never saved.
 */
public interface PhantomMob
{
    PhantomLogic phantom();
}
