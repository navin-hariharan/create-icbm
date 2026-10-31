# Changelog

All notable changes to Create: ICBM are listed here, newest first.

When `mod_version` in `gradle.properties` changes on `main`, the Release workflow builds the mod and publishes a
GitHub release named after that version, with the matching section below as its notes. Add a section for the new
version (heading `## <version>`) in the same commit as the version bump, or the release fails.

## 1.0.5

### New

- **Friend or foe:** missiles remember the radio channel of the Launch Controller or Cruise Turret that launched
  them. Radars and Surface to Air Missiles on that channel leave them alone.
- **Early warning:** radars warn their Cruise Turrets about long-range missiles still out of sight that will come down
  inside the detection range, so the turrets are already aimed when the missile appears.

- **Cruise Turret firing delay:** set in the turret's screen, like the Launch Pad's.
- **Ponder:** the launch, turret and radar scenes now cover lock height, firing delays, which tools and radars fire
  which machine, friend or foe and the Advanced Radar's counter-strike.

### Changed

- Only the Advanced Radar sends Launch Controllers out (to strike the launch site). A plain Radar Station only calls
  Cruise Turrets, and Cruise Turrets ignore counter-strike orders and keep their missile for air defence.
- The Advanced Radar now also calls Cruise Turrets to fire Surface to Air Missiles at incoming missiles.
- Radars can be read by the Remote Detonator and Laser Designator to copy their channel.
- Cruise Turrets no longer fire on orders from the Remote Detonator or Laser Designator, only radars call them.
- Cruise Turrets wait `cruise_turret_launch_cooldown` (20 ticks) after every launch, and a turret that took an incoming
  missile reserves it, so a radar alert is spread over the turrets instead of every turret firing at the same missile.
- Cruise Turrets only answer air defence alerts while they hold a Surface to Air Missile. Without a firing delay they
  don't fire another SAM at a missile that SAMs already in the air will bring down; turrets with a firing delay always
  fire, for timed salvos.
- Radars name each missile of a wave in turn instead of only the closest one, and a turret queued for one missile
  ignores reports about the others until it has fired.
- Lock height is limited to 0 to 256 blocks, and a negative firing delay counts as none.
- Removed leftover debug messages when right-clicking launch blocks with a stone axe, and unused old code.

### Fixed

- Cruise Turrets kept firing Surface to Air Missiles into the sky. The radar repeats its alert every 2 seconds, so a
  turret fed by a hopper fired a new SAM at a missile that was already being chased, and every SAM left without a
  target turned towards the world origin until its fuel ran out. Turrets now hold fire while the missile is covered
  and drop a queued SAM when its target is gone, and a SAM that loses its target looks for another one nearby or
  blows up harmlessly in the air.
- A lock height (or any other value) typed into a machine screen was only set when you clicked somewhere else in the
  screen. Pressing Enter did nothing, and closing the screen threw the value away, so missiles kept the old lock
  height. Enter and closing the screen now set it too.
- The radar Ponder scene said right-clicking with Redstone switches the redstone output on. It is on from the start,
  and Redstone toggles it.
- Radars told turrets about a newly seen missile only after a leftover delay of up to 2 seconds from the last
  missile, and missiles that fell from the sky landed before the turrets fired. A new missile is now reported at
  once.
- Machines placed next to each other could hide each other's sides and leave see-through gaps to the sky, worst
  with culling or connected-texture mods. No Create: ICBM block hides a neighbour's face any more, and none of their
  faces is hidden by a neighbour.
- Explosive crates and Launch Controllers drew the parts that stick out of the block (straps, plates, antenna)
  black, and the straps of two crates side by side flickered into each other. Both blocks now let light in like the
  other machines, and the crate strap no longer overlaps its neighbour.
- Launch sites, Cruise Turrets and radars far from every player did not hear the Laser Designator, Remote Detonator,
  radars or counter-strikes, because radio only reaches machines in loaded chunks. Launch Controllers and Cruise
  Turrets on a radio channel that hold a missile, radars on a radio channel and wired crates now keep their chunks
  loaded and ticking, also after a restart.
- Long-range missiles could hang in the air far from players when an optimization or chunk mod stopped them from
  ticking. A missile that stops ticking for 3 seconds is now flown on to its target by the missile tracker, or goes
  off right there when it stalled above its target.

## 1.0.4

### New

- **Advanced Radar:** an upgraded Radar Station that sends Launch Controllers and Cruise Turrets on its channel to
  the launch site of incoming missiles. See [Advanced Radar](#advanced-radar).
- Circular radar scope with range rings and a side console for both radars. Longer detection ranges cost more stress.

### Fixed

- Advanced Radar counter-strikes that never fired were treated as done and waited the full cooldown. Unanswered or
  refused tries now retry every second, up to 30 times per missile, and only a launch that really fired or was
  queued counts.
- A Launch Controller counted a launch behind a firing delay as a failure, and could count an old firing state as a
  success. It now checks that a firing package for the new target was actually queued.
- A Cruise Turret said yes to radio orders it could not carry out (no power to aim, launch blocked) and kept the order.
  It now checks the launch first and answers that it failed.
- When another mod cancelled a new target, Launch Controllers and Cruise Turrets fired at their old target.
- Launch Controllers that missed their launch network or radio registration on load (chunk loading order) now retry
  every 2 seconds instead of staying deaf until they were broken and placed again.
- Radios of removed or replaced blocks stayed registered, so one block could answer the same message twice.
- Cruise Turrets dropped a radar's order to fire a Surface to Air Missile when their launch charge was not full yet.
  They now finish charging while they turn and then fire.
- A Cruise Turret already turning towards a missile kept aiming at the spot where the radar first saw it, and the SAM
  flew there without a lock. It now follows the radar's latest report and the SAM locks onto the missile.
- Radar scope packets with many contacts were slow to encode.
- The Advanced Radar mined slowly with a pickaxe and had no Ponder entry.
- Fixed optimizer interoperability for every registered Create: ICBM entity, including surface-to-air missiles, blast controllers, seats, decoys, and flying blocks.
- Kept launch pads/radar stations as block entities and detonators/designators as server-authoritative item interactions; entity tick exclusions are applied only to actual entity types.
- Extended the Entity Culling whitelist to all ICBM block entities as well as all ICBM entities, covering launchers and radar-station visuals.
- Prevented Immersive Optimization's optional global block-entity scheduler from delaying ICBM launchers/radar stations when that scheduler is enabled; its entity scheduler remains enabled with the `create_icbm` namespace blacklisted.
- Retained opt-in diagnostics and chunk-ticket controls for testing long-range missile simulation.


## 1.0.35

### New

- **Ender missile abduction cutscene** with a giant Enderman, a purple warp tunnel and its own scream, and dense ender
  particles for everyone watching.
- **Antimatter** flash, mushroom cloud and a crater that opens up smoothly, with a louder detonation that is timed to
  both.
- **Nuclear mushroom cloud**, the same cloud at 70 % size.
- **Distant explosion sound** for everyone within the view distance, with the delay of distance. Full-volume nuclear
  sound within 50 blocks and antimatter within 100.
- **Missiles stay visible** in flight for every player within view distance and fly all the way to the ground before they
  go off. Smooth client-side flight with half the position updates.
- **Server work:** adaptive blast budget, paced antimatter crater, a cap on missile chunk tickets, worker thread count up
  to 8, debris effect limits, per-dimension clean-up.
- **Compatibility:** ServerCore and Immersive Optimization, Sable, Mekanism and BlueMap re-checked and hardened.

### Removed

- **Everything EMP:** the EMP explosive, missile and bomb cart, the EMP Tower and Coil Extender, their recipes, goggles,
  Ponder entries, textures and settings. Create is mechanical, so there is no electricity in this mod any more. Worlds
  that still contain those blocks or items lose them when loaded.
- Unused code: three dead classes, 68 unused methods, 81 unused imports, the unused Forge Energy plug-in and the
  unused energy data component, and two EMP goggle texts.

### Fixed

- Missiles that froze in mid-air or vanished when they got some distance from the player.
- Missiles in unloaded chunks were simulated at twice the configured speed. The default `simulation_flight_speed`
  is now 20 blocks a second and a tick counts once, which keeps the flight time you were used to.
- Radar: the nearest threat was not the one a Surface to Air Missile was sent after, and turrets kept firing at
  missiles that had already died.
- Cruise Turret: a missile was lost when power ran out at the moment of firing.
- Targets that were `NaN` or infinite could be saved in a launcher, and could be typed into commands.
- Several blasts threw entities or blocks with `NaN` velocity when they stood exactly in the centre (condensed,
  repulsive, attractive, sonic, anti-gravitational, red matter, ...).
- Fragments, gas clouds and threaded blasts could load or generate chunks they were not supposed to touch.
- A missile exactly on its target and a missile starting on a block face could end up with a `NaN` position and vanish.
- A delayed ballistic launch lost its airburst height and target type.
- A radio that was saved before it had a channel stayed deaf for ever.
- Launch inaccuracy for far targets overflowed a number above 46,340 blocks of range.
- Wrong vector maths: cross product, minimum and normalise of 3D positions, an angle's sine, the shortest way round an
  angle, cropping a region to the world and negative chunk coordinates.
- The anvil fragments flickered between shapes, the ender camera could stare at the spot a player had just left, and a
  long screen flash could survive a log-out.
- Config safety: ranges for the launcher and radar range, `NaN` input to the blast commands, and mistakes in the flying
  block list are now logged instead of silently skipped.

## 1.0.3

### Fixed
- Missiles from the handheld rocket launcher and the cruise launcher no longer stop partway or vanish at the edge of the simulation distance. They keep the chunks just ahead of them loaded while they fly, the same way thrown ender pearls do.
- Missiles no longer stall far from players in big modpacks. Missiles, explosions, gravity blocks, fragments and bomb carts keep ticking with mods that slow down far-away entities (Immersive Optimization, ServerCore).
- Missiles no longer hit things twice or vanish early on screen. Impacts are now only handled by the server.
- Fixed projectiles drifting up or down when steering toward a target.
- Launch pad missiles no longer freeze for good mid-flight. If the missile tracker can't take a missile (another mod cancels it, or it has no target), the missile is offered again a couple of seconds later, and its path stays loaded while it flies on. Before, a single refusal was saved with the missile and blocked it forever.
- Missiles now check correctly whether the chunks ahead are really ticking, so they hand over to the tracker in time.

### Claims and protection
- Every blast now respects claim and protection mods such as FTB Chunks and Open Parties and Claims, not just the TNT-style ones. This covers nuclear, antimatter, radiation, sonic, beam, fire, anti-gravity and Red Matter.
- Big blasts check claims while the crater is being carved, so land claimed during a long blast is still spared.
- Blocks a blast changes next to its area are checked too: ice, fire, magma, soul sand, snow, and flying blocks landing. A flying block that would land in a claim drops as an item instead.
- If another mod's claim check crashes, the blast leaves those blocks alone.
- New config option `protection_events` (on by default) turns this on or off.

### Sable and Create Aeronautics
- Missiles, rockets and fragments that hit a ship now hit the ship where it is in the world, instead of vanishing.
- Launchers, radar guns and laser designators work from a ship's deck. Rockets fired from a moving ship keep the ship's speed.
- TNT, nuclear, thermobaric and antimatter blasts damage nearby ships.

### Compatibility
- Recipes now use common `c:` tags, so other mods' iron, gold, copper and brass plates, and similar materials, work too.
- Added common entity tags so capture and teleport mods leave missiles and blasts alone, and a block tag so movers don't relocate Mail.

### Multiplayer
- The server now enforces the laser detonator and radar gun cooldowns, so modified clients can't spam designations.
- Malformed packets are dropped quietly, and packet errors are logged at most every 10 seconds.
- Clients and servers with different Create: ICBM versions get a clear "incompatible" message instead of desyncing.
- The explosive registry file is saved safely in UTF-8. A crash while saving no longer corrupts it.

### Performance
- Straight-flying missiles only load the chunks right ahead of them. The chunks are loaded in the background and released on their own.
- Claim checks run once per batch of blocks instead of once per block.

### Config
- New: `missile.direct_fire_chunk_loading` (default on).
- New: `blast.protection_events` (default on).

## 1.0.2 and earlier

First releases of Create: ICBM. See the GitHub releases page for their notes.
