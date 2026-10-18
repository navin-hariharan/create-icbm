# TODO

Features the game text promises but the code doesn't do yet. README.md and marketplace.md describe the current
behaviour; update both once each item is done.

- [ ] **Launch Pad groups.** The Launch Pad's *Group* and *Index* fields do nothing: the Launch Controller always fires
  every pad on its network.
  - `LauncherCapability` doesn't override `getLauncherGroup()` / `getLaunchIndex()`, so every pad reports `-1`.
  - `TileLauncherScreen.getFiringGroup()` always returns `-1`, and the controller screen has no group field.
  - `LauncherNetwork.streamLaunchers(group)` already filters by group, so the rest is wiring.

- [ ] **Radar trigger range.** The Radar Station's *trigger range* is only drawn on its screen. Any missile heading
  towards the radar inside the detection range counts as incoming, so the alarm, redstone output and radio alerts
  ignore the trigger range.
  - `TileRadarStation.isMissileGoingToHit()` only checks that the missile is getting closer.

- [ ] **Remote Detonator and explosive crates.** The Remote Detonator doesn't set off explosive crates, even though the
  crate tooltip says it does (`explosive.tooltip.behaviour1` in `en_us.json`).
  - Crates have no radio receiver. Only Launch Controllers, Cruise Turrets and EMP Towers listen to the radio.

- [ ] **Ender destination.** You can't pick where the Ender explosive teleports things with the Laser Range Finder.
  The destination is always a random spot up to 150 blocks away.
  - `ExplosiveInit.enderBlockCoordSet` / `enderMissileCoordSet` exist but are never called:
    `ExBlockContentReg` stores the listener without running it, `ExMissileContentReg.setInteractionListener` is
    empty, and stored customisations aren't applied in `CapabilityExplosiveEntity.doExplosion`.
  - The `ender.scale` config option is also ignored; the vortex radius is fixed at 20 in `BlastEnder`.
