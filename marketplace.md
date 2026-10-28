<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/banner.png" alt="Create: ICBM" width="100%">
</p>

<h3 align="center">Missiles, radar and launch networks, built the Create way.</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Version-1.0.35-8B5CF6?style=for-the-badge" alt="Version 1.0.35">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.219%2B-E2711D?style=for-the-badge" alt="NeoForge 21.1.219+">
  <img src="https://img.shields.io/badge/Create-6.0.10%2B-C9A45C?style=for-the-badge" alt="Create 6.0.10+">
  <img src="https://img.shields.io/badge/License-CC--BY--NC--4.0-3B82F6?style=for-the-badge" alt="CC BY-NC 4.0">
</p>

<p align="center">
  <a href="https://github.com/navin-hariharan/create-icbm"><b>Source on GitHub</b></a>
  &nbsp;&bull;&nbsp;
  <a href="https://github.com/navin-hariharan/create-icbm/issues"><b>Report a bug</b></a>
</p>

---

**Create: ICBM** is an addon for [Create](https://modrinth.com/mod/create) that brings missiles,
explosives, radar and launch networks into your factory. Launch pads are wound up by shafts, missiles are put together on
Mechanical Crafters and Deployers, explosives are mixed in basins over Blaze Burners, and every block looks like it came
out of Create itself.

It is a new addon, inspired by the classic **ICBM** mod (see Credits).

> **AI usage notice:** AI assistance was used in making Create: ICBM. Every feature is directed, reviewed
> and tested in game by the author.

---


## ✨ Features

- **Rotation-powered machines.** Launch Pads, Cruise Turrets and Radar Stations take a shaft and draw
  stress like any Create machine. No batteries, no energy cables.
- **26 explosives in 4 tiers**, from a beefed-up TNT to antimatter and a red matter black hole. Each one can be placed
  as a crate, fired as a missile or rolled in as a bomb cart.
- **Blasts you can see and hear from far away:** nuclear and antimatter raise a mushroom cloud, antimatter blinds
  everyone around it with a flash, and every big blast is heard with the delay of distance by everyone within the
  server's view distance.
- **The Ender missile abducts you.** Players pulled into its vortex are taken by a giant Enderman in a short cutscene
  and dragged through a purple warp tunnel to a spot far away. Onlookers see dense ender particles around the blast.
- **Tricks as well as bangs:** a blinding Flashbang, a Recon pulse that outlines enemies through walls, Phantom Enemies
  that only pretend to attack, and a cloud of Darkness.
- **Long-range missiles** that fly a ballistic arc up to 10,000 blocks and keep flying through unloaded chunks. Any
  player within view distance sees a missile fly smoothly all the way down to the ground before it goes off.
- **A real launch network:** chain pads, gantries and connectors to one Launch Controller and fire whole salvos.
- **Air defence:** a spinning Radar Station spots incoming missiles and tells Cruise Turrets loaded with
  Surface to Air Missiles to shoot them down.
- **Delivery Missiles** that drop a stack of items straight into a chest on the other side of the map, or leave it
  in a **Mail** parcel with the sender's name on it.
- **Made with Create:** every recipe uses only Create and vanilla items, on Create's machines or at a plain crafting
  table. Ponder scenes, goggle overlays, "Hold [Shift] for Summary" tooltips and JEI support included. Everything here
  is mechanical: there is no electricity.
- **Plays well with storage mods:** hoppers, funnels, chutes and Tom's Simple Storage can stock your launchers.
- **Live on BlueMap:** with [BlueMap](https://bluemap.bluecolored.de/) installed, flying missiles move across its web
  map. Click one to see who launched it, and every impact leaves a mushroom cloud on the map for a minute.
- **Airship ready:** with Sable (the physics behind Create Aeronautics) installed, missiles hit ships where they
  fly, launchers work from a moving deck and blasts tear blocks off nearby ships.
- **Big modpack ready:** missiles and blasts keep flying when tick optimizers such as Immersive Optimization or
  ServerCore's activation range slow down entities far from players.
- **Built for servers:** big blasts are spread over several ticks with a time budget that shrinks by itself while the
  server is lagging, blast maths runs on a worker thread, missile chunk loading is capped per dimension, missiles send
  half as many position updates (the client flies them smoothly in between), and clouds, flashes and cutscenes are
  drawn by each client from one small packet. See Server performance.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines.png" alt="The machines of Create: ICBM" width="100%">
</p>

## 📥 Installation

| Needs | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 or newer |
| [Create](https://modrinth.com/mod/create) | 6.0.10 or newer (brings Ponder and Flywheel with it) |
| [JEI](https://modrinth.com/mod/jei) | optional, 19.x, shows every recipe |
| [Tom's Simple Storage](https://modrinth.com/mod/toms-storage) | optional, for storage automation |
| [BlueMap](https://modrinth.com/plugin/bluemap) | optional, 5.x, shows missiles on its web map |
| Sable (Create Aeronautics) | optional, airships can be hit, damaged and fired from |
| Mekanism | optional, missiles fly through Teleporter portals |
| Immersive Optimization, ServerCore | optional, missiles keep flying far from players. If they still stall, add `create_icbm` to the optimizer's entity blacklist |

See Compatibility for what each optional mod adds.

Drop the `create-icbm` jar into your `mods` folder next to Create. The mod is needed on both the server and every
client. This is version **1.0.35**.

## 🚀 Your first launch

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/launch-site.png" alt="A launch site: Launch Pad with a missile, Launch Gantry, Launch Controller and a shaft" width="85%">
</p>

1. **Place a Launch Pad** on the ground and give it rotation through a shaft from below. The pad draws 8 SU per RPM.
   At 64 RPM it winds up a full launch charge in about 4 seconds, and it only fires when fully wound.
2. **Build the launch network.** Put a **Launch Gantry** next to the pad and a **Launch Controller** touching the
   gantry or the pad. Use **Launch Connectors** like cables to reach pads further away. Everything that touches forms
   one network, and the controller fires every pad in it.
3. **Make a missile.** Craft a Missile Casing, turn it into a Missile Module, then arm it by deploying an explosive
   crate onto it with a Deployer (or combine both at a crafting table).
4. **Load it.** Right-click the pad with the missile, or feed it in with a hopper, funnel or chute.
5. **Pick a target.** Aim a **Laser Range Finder** at a block and right-click, then right-click the controller with
   it. You can also type the coordinates into the controller.
6. **Fire!** Press *Launch* in the controller, give the controller a redstone signal, or use a **Remote Detonator** or
   **Laser Designator** tuned to the controller's radio channel.

> Feeling brave? Right-click a missile on a pad to sit on it. You go along for the ride when it launches.

## 🏭 Machines

| Machine | Power | In short |
|---|---|---|
| [Launch Pad](#launch-pad) | 8 SU/RPM, shaft from behind | Holds a missile and launches it in a ballistic arc. |
| [Launch Gantry](#launch-gantry) | none | Stands next to the missile and links the launch network. |
| [Launch Connector](#launch-connector) | none | A flat cable for the launch network. |
| [Launch Controller](#launch-controller) | none | Target, radio channel and the *Launch* button for a whole network. |
| [Cruise Turret](#cruise-turret) | 6 SU/RPM, shaft below | A turret that aims a missile and fires it straight at the target. |
| [Radar Station](#radar-station) | 4 SU/RPM, shaft below, 16 RPM or more | Tracks incoming missiles up to 500 blocks away. |

A machine runs while its shaft turns at or above its minimum speed and the network isn't overstressed. Engineer's
Goggles show each machine's charge and status, and every machine has a Ponder scene: hold **W** over it in your
inventory. Server owners can turn the rotation requirement off with `use_energy = false` (see
Configuration).

### Launch Pad

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/launch-pad.png" alt="Launch Pad" width="260">
</p>

The pad launches away from the face you place it on: on the ground it fires upwards. Its shaft goes into the
opposite side, so a pad on the ground takes its shaft from below.

- **Power:** 8 SU per RPM (512 SU at 64 RPM). Turning winds up the launch charge: about 4 seconds at 64 RPM, 16
  seconds at 16 RPM and 1 second at 256 RPM. A missile only leaves on a full charge, and a launch uses all of it.
- **Loading:** right-click the pad with a missile, or let a hopper, funnel or chute feed it from any side.
  **Shift + right-click** with an empty hand takes the missile back. A pad can't be loaded while its last missile is
  still above it. Every missile works: explosive missiles, Missile Modules, Surface to Air Missiles and Delivery
  Missiles.
- **Settings** (right-click the pad):
  - **Lock Height** (default 3): how far the missile climbs straight up before it turns towards the target.
  - **Firing Delay** (in ticks, default 0): a wait before this pad fires, to stagger a salvo.
  - **Group** and **Index**: labels shown on the goggles. The controller fires every pad on its network together.
  - **Missile Rotation** (top right): **Alt + click** turns the missile clockwise, **Shift + click** the other way.
    This only changes how it looks and where you sit.
- **Firing:** the pad has no button of its own. It fires when a Launch Controller on its network fires.
- **Riding:** right-click the loaded missile to sit on it and ride it to the target.

**Flight.** The missile warms up on the pad for 2 seconds, climbs to its lock height and flies a ballistic arc. The arc peaks as high as the target is far, up to 1,000 blocks. Targets 500 blocks or more away
make the missile climb straight up out of sight. It then crosses the map out of sight, through unloaded chunks too (simulated at 20 blocks a
second by default, so 1,000 blocks take about 50 seconds), and drops back in above the target. A pad reaches **10,000 blocks** in each direction and needs targets at least 10
blocks away.

**Accuracy.** A missile lands within about 2 blocks of the target, up to 12 at the full 10,000 blocks, plus 1 block
for every extra pad in the same salvo. The controller shows the expected spread.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-pad.png" alt="Launch Pad recipe" width="80%">
</p>

### Launch Gantry

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/launch-gantry.png" alt="Launch Gantry" width="260">
</p>

A girder that stands next to the missile on its pad. Gantries stack and link up with a controller above or below them.
They carry the launch network between pads and the controller, and pass items through: a funnel or hopper on a
gantry loads the pads behind it. A gantry isn't required for a launch, it just looks the part and links things up.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-gantry.png" alt="Launch Gantry recipe" width="80%">
</p>

### Launch Connector

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/launch-connector.png" alt="Launch Connector" width="260">
</p>

A flat cable for the launch network. Its arms reach out to pads, gantries, controllers, Cruise Turrets and any block
with an inventory, so one controller can fire pads spread across a whole silo field.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launcher-connector.png" alt="Launch Connector recipe" width="80%">
</p>

### Launch Controller

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/launch-controller.png" alt="Launch Controller" width="260">
</p>

The firing panel for every launcher on its network: pads, and Cruise Turrets too. It needs no rotation.

- **Target:** type it as `x, y, z` (decimals allowed), or right-click the controller with a
  [Laser Range Finder](#laser-range-finder) holding a target.
- **Radio channel:** any letters and numbers. Tools and radars on the same channel talk to it. **Alt + click** the
  radio icon to stop it listening.
- **Launch:** fires every ready launcher on the network. Hover over the button to see which are not ready and why
  (no missile, not wound up, target too close or too far, something in the way).
- **Acc:** the expected landing spread for this salvo.
- **Redstone:** a lever, button, repeater or redstone dust pointing into the controller fires it. While the signal
  stays on it fires again as soon as a pad is ready, so use a pulse for a single salvo.
- **Radio:** a Remote Detonator on its channel fires it at the current target. A Laser Designator sets a new target
  and fires. A Radar Station on its channel retargets it onto an incoming missile and fires.
- **Loading:** missiles put into a controller (by hand, hopper or funnel) go to the empty pads in its network.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-controller.png" alt="Launch Controller recipe" width="80%">
</p>

### Cruise Turret

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/cruise-turret.png" alt="Cruise Turret" width="260">
</p>

A rotating turret that swings round to face its target and fires a missile straight at it.

- **Power:** 6 SU per RPM from a shaft below. It turns at its normal speed at 32 RPM, slower below that, and up to 4
  times faster at 128 RPM. It winds up a launch charge the same way as the Launch Pad.
- **Screen** (right-click): missile slot, charge bar, target (`x, y, z`), radio channel and a *Launch* button.
- **Firing:** it fires once it is on target and fully wound. Press *Launch*, give it a redstone signal, fire it from a
  Launch Controller on the same network, or use a Remote Detonator or Laser Designator on its channel.
- **Air defence:** set it to a Radar Station's channel and load a [Surface to Air Missile](#surface-to-air-missile).
  When the radar spots an incoming missile, the turret aims at it and fires, and the SAM locks on.
- **Flight:** missiles fly in a straight line at 40 blocks a second, with fuel for about 400 blocks, then fall. The
  target has to be at least 20 blocks away, and the space just above the turret has to be clear.
- Takes any missile, by hand, hopper, funnel or chute. Right-click it with a Laser Range Finder to hand over a target.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/cruise-turret.png" alt="Cruise Turret recipe" width="80%">
</p>

### Radar Station

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines/radar-station.png" alt="Radar Station" width="260">
</p>

A spinning dish that watches the sky for missiles carrying a warhead. Surface to Air Missiles and Delivery Missiles
don't set it off.

- **Power:** 4 SU per RPM from a shaft below, at **16 RPM or more**.
- **Screen** (right-click): a live radar screen, the **detection range** (default 100, up to 500 blocks), the
  **trigger range** (default 50, drawn on the screen as an inner box), the radio channel and a redstone switch.
- **Incoming missiles:** a missile counts as incoming while it flies towards the radar. The radar scans four times a
  second over a square of its detection range.
- **Redstone:** while missiles are incoming it powers every side, one signal level per missile (up to 15). On by
  default. Use the switch on its screen, or right-click it with Redstone Dust, to turn it on or off.
- **Radio:** every 2 seconds while a missile is incoming, it sends the closest one on its channel. Cruise Turrets on
  that channel shoot at it and Launch Controllers fire at it.
- The radar can't be read by tools, so type its channel into the other machines.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/radar-station.png" alt="Radar Station recipe" width="80%">
</p>


## 🚀 Missiles

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles.png" alt="Every missile in Create: ICBM" width="100%">
</p>

| Missile | What it is |
|---|---|
| [Missile Casing](#missile-casing) | The empty rocket body every missile starts from. |
| [Missile Module](#missile-module) | A casing with a motor. It flies, and it is what you arm. |
| [Explosive Missiles](#explosive-missiles) | A Missile Module armed with one of the 26 explosives. |
| [Surface to Air Missile](#surface-to-air-missile) | Hunts down other missiles in the air. |
| [Delivery Missile](#delivery-missile) | Carries a stack of items to a chest or a [Mail](#mail) parcel. |

### Missile Casing

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles/missile-casing.png" alt="Missile Casing" width="260">
</p>

A pressed sheet-metal rocket body. It does nothing on its own: fit it with a motor to make a Missile Module, a
Surface to Air Missile or a Delivery Missile.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missile-casing.png" alt="Missile Casing recipe" width="80%">
</p>

### Missile Module

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles/missile-module.png" alt="Missile Module" width="260">
</p>

A Missile Casing fitted with an Electron Tube and a Blaze Powder motor. It launches and flies like any missile, but
carries only a tiny charge, so it is good for testing your aim. Arm it with an explosive crate to make an
[explosive missile](#explosive-missiles).

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missile-module.png" alt="Missile Module recipe" width="80%">
</p>

### Explosive Missiles

A Missile Module armed with an explosive crate. Put the module on a Depot or belt under a Deployer holding the crate,
or combine both at a crafting table. The warhead goes off when the missile hits the ground or comes within 2 blocks
of its target.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/explosive-missile.png" alt="How every explosive missile is made" width="80%">
</p>

Every explosive makes its own missile. Higher tiers are tougher to shoot down:

| Tier | Health | Missiles |
|---|---|---|
| 1 | 50 | [Conventional Missile](#condensed), [Shrapnel Missile](#shrapnel), [Incendiary Missile](#incendiary), [Debilitation Missile](#debilitation), [Chemical Missile](#chemical), [Anvil Missile](#anvil), [Repulsive Missile](#repulsive), [Attractive Missile](#attractive), [Colorful Missile](#colorful), [Smoke Marking Missile](#smoke-marking), [Flashbang Missile](#flashbang), [Recon Missile](#recon) |
| 2 | 80 | [Fragmentation Missile](#fragmentation), [Contagious Missile](#contagious), [Sonic Missile](#sonic), [Breaching Missile](#breaching), [Thermobaric Missile](#thermobaric), [Phantom Enemy Missile](#phantom-enemy), [Darkness Missile](#darkness) |
| 3 | 100 | [Nuclear Missile](#nuclear), [Exothermic Missile](#exothermic), [Endothermic Missile](#endothermic), [Anti-Gravitational Missile](#anti-gravitational), [Ender Missile](#ender) |
| 4 | 200 | [Antimatter Missile](#antimatter), [Red Matter Missile](#red-matter) |

Each explosive's own section below shows its missile and recipe.

### Surface to Air Missile

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles/surface-to-air-missile.png" alt="Surface to Air Missile" width="260">
</p>

A missile that hunts other missiles. Fire it from a [Cruise Turret](#cruise-turret), a handheld
[rocket launcher](#rocket-launcher-direct) or even a Launch Pad.

- In flight it looks for missiles within 30 blocks around it, twice a second, and chases the nearest one at 80 blocks
  a second. It goes after any missile except other SAMs, friendly ones included.
- Within 4 blocks it hits for 100 damage and is spent. One SAM brings down any tier 1 to 3 missile; a tier 4 missile
  takes two.
- A missile shot down by a SAM does **not** set off its warhead.
- Fired from a turret on a radar's channel, the SAM is locked onto the radar's target from the start.
- It has 10 seconds of fuel once it locks on, and gives up after 60 seconds in the air.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/surface-to-air-missile.png" alt="Surface to Air Missile recipe" width="80%">
</p>

### Delivery Missile

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles/delivery-missile.png" alt="Delivery Missile" width="260">
</p>

A missile that carries one stack of items instead of a warhead. It does no damage.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/delivery-menu.png" alt="The Delivery Missile cargo slot" width="45%">
</p>

- **Loading:** **shift + right-click** it in your hand to open its cargo slot. You can also load it like a bundle:
  right-click a stack onto it in your inventory, and right-click it with an empty cursor to take the cargo out.
  Anything a chest slot can hold fits, shulker boxes included, but not another loaded Delivery Missile.
- **On impact** it puts the stack into the container it hits: a chest, barrel, Create vault or any other block with
  an inventory. Whatever doesn't fit, or everything if it hit no container, is left in a [Mail](#mail) parcel.
- If it is shot down or runs out of time, the cargo still lands in a parcel below it.
- The sender is the player who fired it: whoever pressed *Launch*, used the radio tool or held the rocket launcher.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/delivery-missile.png" alt="Delivery Missile recipe" width="80%">
</p>

### Mail

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/mail.png" alt="A Mail parcel with the sender's name on its label" width="70%">
</p>

When a Delivery Missile has no container to fill, it leaves a **Mail** parcel where it lands, the size of a slab. Its
label shows the name of the player who launched the missile, and looking at it shows *Mail from &lt;name&gt;*.

- **Right-click** the parcel to take the items straight into your inventory. Once it is empty it disappears. If your
  inventory is full, whatever didn't fit stays inside for later.
- It can't be crafted, broken, pushed by pistons or taken from the Creative inventory, and explosions can't break it,
  so a delivery is never scattered on the ground. Hoppers can't empty it either.
- A parcel nobody has opened for 3 minutes starts to **ding** every 15 to 25 seconds, so it's easy to find, until
  someone opens it.
- With BlueMap installed, every unopened parcel shows on the web map until it is opened.

### Flight and interception

- **Health:** tier 1 missiles have 50 health, tier 2 80, tier 3 100 and tier 4 200. Surface to Air and Delivery
  Missiles have 80.
- **Shooting them down:** missiles can be hit with weapons, arrows and other projectiles and SAMs. A
  missile destroyed by an explosion or fire sets off its warhead where it is; anything else just destroys it.
- **Hitting things:** a missile that flies into a mob or player hurts it, more the faster it flies.
- Missiles stay in the air for up to 2 hours before they give up. Right-click one in flight to climb aboard.

## 💣 Explosives

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives.png" alt="All 26 explosive crates" width="100%">
</p>

Every explosive comes three ways: as a **crate** you place and light, as a **missile** warhead and as a **bomb cart**. Each one below shows all three, what it does, and its recipes.

**Setting off a crate:**

- a redstone signal, or placing it on a powered spot;
- right-click with Flint and Steel or a Fire Charge, or hit it with a burning arrow;
- another explosion or a Sonic shockwave reaching it;
- a [Remote Detonator](#remote-detonator) it is wired to (right-click the crate with the detonator to wire it);
- placing it next to fire or lava.

Once lit, the crate becomes a lit block that falls and can be pushed, and goes off when its fuse runs out: 5 seconds for most explosives. Mining a crate is safe and gives it back.

Tier 1 explosives mix in a plain Basin, tier 2 over a heated Blaze Burner, tiers 3 and 4 over a superheated one. Each explosive is made from a lower one, so you work your way up from Condensed Explosives (see Crafting recipes).

| Explosive | Tier | Reach |
|---|---|---|
| [Condensed](#condensed) | 1 | Blast radius 6 blocks |
| [Shrapnel](#shrapnel) | 1 | 900 shards |
| [Incendiary](#incendiary) | 1 | Fire within 14 blocks |
| [Debilitation](#debilitation) | 1 | Gas cloud 20 blocks, 30 s |
| [Chemical](#chemical) | 1 | Gas cloud 20 blocks, 30 s |
| [Anvil](#anvil) | 1 | 625 anvil chunks |
| [Repulsive](#repulsive) | 1 | Push within 12 blocks |
| [Attractive](#attractive) | 1 | Pull within 12 blocks |
| [Colorful](#colorful) | 1 | Rainbow cloud 10 blocks, 30 s |
| [Smoke Marking](#smoke-marking) | 1 | Smoke for 5 s |
| [Flashbang](#flashbang) | 1 | Flash within 16 blocks |
| [Recon](#recon) | 1 | Glow within 32 blocks, 3 min |
| [Fragmentation](#fragmentation) | 2 | 225 explosive fragments |
| [Contagious](#contagious) | 2 | Viral cloud 20 blocks, 30 s |
| [Sonic](#sonic) | 2 | Shockwave about 15 blocks |
| [Breaching](#breaching) | 2 | 3 x 3 tunnel, 7 blocks deep |
| [Thermobaric](#thermobaric) | 2 | Terrain up to 30 blocks, lethal within 50 |
| [Phantom Enemy](#phantom-enemy) | 2 | 4 phantoms per player within 24 blocks |
| [Darkness](#darkness) | 2 | Darkness within 24 blocks, 3 min |
| [Nuclear](#nuclear) | 3 | Terrain up to 35 blocks, lethal within 50, fallout to 80 |
| [Exothermic](#exothermic) | 3 | Nether land within 30 blocks |
| [Endothermic](#endothermic) | 3 | Frozen land within 30 blocks |
| [Anti-Gravitational](#anti-gravitational) | 3 | Lifts everything within 30 blocks for 2 min |
| [Ender](#ender) | 3 | Vortex 20 blocks, 8 s |
| [Antimatter](#antimatter) | 4 | Erases 55 blocks, lethal within 110 |
| [Red Matter](#red-matter) | 4 | Black hole, grows up to size 70 |

### Tier 1

Mixed in a plain Basin. Missile health 50.

#### Condensed

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/condensed.png" alt="Condensed" width="80%">
</p>

*Blast radius 6 blocks · Crate fuse: instant · Cart fuse: 5 s*

A stronger TNT. It breaks blocks out to 6 blocks, where TNT reaches 4, and hurts anything in that range. About one broken block in six drops as an item. The crate goes off the instant it is lit, and it is the base of every other explosive.

<details>
<summary><b>Recipes</b>: Condensed Explosives, Conventional Missile, Explosive Cart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/condensed-explosives.png" alt="Condensed Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/condensed.png" alt="Conventional Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/condensed.png" alt="Explosive Cart recipe" width="80%">
</p>
</details>

#### Shrapnel

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/shrapnel.png" alt="Shrapnel" width="80%">
</p>

*900 shards · Crate fuse: 5 s · Cart fuse: 5 s*

Sprays 900 burning metal shards in every direction. Each one hits for about 6 to 14 damage and sets its target on fire for 5 seconds. The blast itself breaks nothing, but shards smash glass, leaves, snow and glowstone in their way. Water slows them down and puts their flames out.

<details>
<summary><b>Recipes</b>: Shrapnel Explosives, Shrapnel Missile, Shrapnel Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/shrapnel-explosives.png" alt="Shrapnel Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/shrapnel.png" alt="Shrapnel Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/shrapnel.png" alt="Shrapnel Minecart recipe" width="80%">
</p>
</details>

#### Incendiary

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/incendiary.png" alt="Incendiary" width="80%">
</p>

*Fire within 14 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

Scatters fire over everything within 14 blocks, thinner towards the edge, and melts away ice. It breaks no blocks itself; the fire does the rest.

<details>
<summary><b>Recipes</b>: Incendiary Explosives, Incendiary Missile, Incendiary Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/incendiary-explosives.png" alt="Incendiary Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/incendiary.png" alt="Incendiary Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/incendiary.png" alt="Incendiary Minecart recipe" width="80%">
</p>
</details>

#### Debilitation

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/debilitation.png" alt="Debilitation" width="80%">
</p>

*Gas cloud 20 blocks, 30 s · Crate fuse: 5 s · Cart fuse: 5 s*

Releases a white gas cloud that spreads out to 20 blocks and lingers for 30 seconds. Anyone inside is poisoned, slowed down hard and gets Mining Fatigue. Solid walls keep the gas out. Breaks nothing.

<details>
<summary><b>Recipes</b>: Debilitation Explosives, Debilitation Missile, Debilitation Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/debilitation-explosives.png" alt="Debilitation Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/debilitation.png" alt="Debilitation Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/debilitation.png" alt="Debilitation Minecart recipe" width="80%">
</p>
</details>

#### Chemical

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/chemical.png" alt="Chemical" width="80%">
</p>

*Gas cloud 20 blocks, 30 s · Crate fuse: 5 s · Cart fuse: 5 s*

A yellow toxic cloud that spreads out to 20 blocks and lingers for 30 seconds. It brings Poison, Nausea, Hunger, Weakness and Mining Fatigue, and after about 5 seconds inside it starts dealing chemical damage that grows the longer you stay. Milk doesn't cure this poison, an [Antidote](#antidote) does. Solid walls keep the gas out.

<details>
<summary><b>Recipes</b>: Chemical Explosives, Chemical Missile, Chemical Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/chemical-explosives.png" alt="Chemical Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/chemical.png" alt="Chemical Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/chemical.png" alt="Chemical Minecart recipe" width="80%">
</p>
</details>

#### Anvil

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/anvil.png" alt="Anvil" width="80%">
</p>

*625 anvil chunks · Crate fuse: 5 s · Cart fuse: 5 s*

Hurls 625 heavy anvil chunks that hit for about 15 to 36 damage each, with a clang. Like shrapnel, the chunks break glass, leaves and snow but leave other blocks alone.

<details>
<summary><b>Recipes</b>: Anvil Explosives, Anvil Missile, Anvil Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/anvil-explosives.png" alt="Anvil Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/anvil.png" alt="Anvil Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/anvil.png" alt="Anvil Minecart recipe" width="80%">
</p>
</details>

#### Repulsive

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/repulsive.png" alt="Repulsive" width="80%">
</p>

*Push within 12 blocks · Crate fuse: 6 s · Cart fuse: 6 s*

A small blast that breaks blocks within 3 blocks and throws every mob, player and item within 12 blocks away from it, hardest up close. Players are thrown half as far. It deals no damage itself, but the landing might.

<details>
<summary><b>Recipes</b>: Repulsive Explosives, Repulsive Missile, Repulsive Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/repulsive-explosives.png" alt="Repulsive Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/repulsive.png" alt="Repulsive Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/repulsive.png" alt="Repulsive Minecart recipe" width="80%">
</p>
</details>

#### Attractive

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/attractive.png" alt="Attractive" width="80%">
</p>

*Pull within 12 blocks · Crate fuse: 6 s · Cart fuse: 6 s*

A small blast that breaks blocks within 3 blocks and pulls every mob, player and item within 12 blocks towards it, the furthest ones hardest. Players are pulled half as far. It deals no damage itself.

<details>
<summary><b>Recipes</b>: Attractive Explosives, Attractive Missile, Attractive Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/attractive-explosives.png" alt="Attractive Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/attractive.png" alt="Attractive Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/attractive.png" alt="Attractive Minecart recipe" width="80%">
</p>
</details>

#### Colorful

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/colorful.png" alt="Colorful" width="80%">
</p>

*Rainbow cloud 10 blocks, 30 s · Crate fuse: 5 s · Cart fuse: 5 s*

A harmless rainbow cloud that spreads out to 10 blocks and swirls for 30 seconds. Nothing is broken and nobody is hurt. Good for celebrations.

<details>
<summary><b>Recipes</b>: Colorful Explosives, Colorful Missile, Colorful Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/colorful-explosives.png" alt="Colorful Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/colorful.png" alt="Colorful Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/colorful.png" alt="Colorful Minecart recipe" width="80%">
</p>
</details>

#### Smoke Marking

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/smoke-marking.png" alt="Smoke Marking" width="80%">
</p>

*Smoke for 5 s · Crate fuse: 5 s · Cart fuse: 5 s*

A column of smoke for 5 seconds, handy for marking a target or testing your aim before you send something heavier. Harmless.

<details>
<summary><b>Recipes</b>: Smoke Marking Explosives, Smoke Marking Missile, Smoke Marking Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/smoke-marking-explosives.png" alt="Smoke Marking Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/smoke-marking.png" alt="Smoke Marking Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/smoke-marking.png" alt="Smoke Marking Minecart recipe" width="80%">
</p>
</details>

#### Flashbang

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/flashbang.png" alt="Flashbang" width="80%">
</p>

*Flash within 16 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

Every player within 16 blocks sees a white flash fill the screen for a second before it fades, whichever way they are looking. Mobs aren't affected, nothing breaks and nobody is hurt.

<details>
<summary><b>Recipes</b>: Flashbang Explosives, Flashbang Missile, Flashbang Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/flashbang-explosives.png" alt="Flashbang Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/flashbang.png" alt="Flashbang Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/flashbang.png" alt="Flashbang Minecart recipe" width="80%">
</p>
</details>

#### Recon

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/recon.png" alt="Recon" width="80%">
</p>

*Glow within 32 blocks, 3 min · Crate fuse: 5 s · Cart fuse: 5 s*

Sends out a radar pulse that makes every mob and player within 32 blocks glow through walls for 3 minutes. The player who launched or lit it, their team mates and their pets are left out. A crate set off by redstone with nobody to credit marks everyone. Breaks nothing.

<details>
<summary><b>Recipes</b>: Recon Explosives, Recon Missile, Recon Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/recon-explosives.png" alt="Recon Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/recon.png" alt="Recon Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/recon.png" alt="Recon Minecart recipe" width="80%">
</p>
</details>

### Tier 2

Mixed in a Basin over a heated Blaze Burner. Missile health 80.

#### Fragmentation

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/fragmentation.png" alt="Fragmentation" width="80%">
</p>

*225 explosive fragments · Crate fuse: 5 s · Cart fuse: 5 s*

Throws 225 fragments that each blow up when they hit the ground or a mob, with a blast a little under half the power of TNT. Together they crater a wide area and hurt anything caught in it.

<details>
<summary><b>Recipes</b>: Fragmentation Explosives, Fragmentation Missile, Fragmentation Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/fragmentation-explosives.png" alt="Fragmentation Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/fragmentation.png" alt="Fragmentation Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/fragmentation.png" alt="Fragmentation Minecart recipe" width="80%">
</p>
</details>

#### Contagious

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/contagious.png" alt="Contagious" width="80%">
</p>

*Viral cloud 20 blocks, 30 s · Crate fuse: 5 s · Cart fuse: 5 s*

A green viral cloud that spreads out to 20 blocks and lingers for 30 seconds. It brings strong Poison, Blindness, Hunger, Weakness and Mining Fatigue, and after about 2.5 seconds inside it deals damage that grows over time. Pigs turn into zombified piglins and villagers into zombie villagers. Only an [Antidote](#antidote) cures it.

<details>
<summary><b>Recipes</b>: Contagious Explosives, Contagious Missile, Contagious Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/contagious-explosives.png" alt="Contagious Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/contagious.png" alt="Contagious Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/contagious.png" alt="Contagious Minecart recipe" width="80%">
</p>
</details>

#### Sonic

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/sonic.png" alt="Sonic" width="80%">
</p>

*Shockwave about 15 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

A shockwave that rolls outward for about a second, tearing up blocks up to about 13 blocks away across open ground (only a block or two into solid stone) and flinging mobs and players into the air. Torn-up blocks are destroyed and some fly off as debris. Other explosive crates it reaches are set off. It deals no direct damage.

<details>
<summary><b>Recipes</b>: Sonic Explosives, Sonic Missile, Sonic Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/sonic-explosives.png" alt="Sonic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/sonic.png" alt="Sonic Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/sonic.png" alt="Sonic Minecart recipe" width="80%">
</p>
</details>

#### Breaching

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/breaching.png" alt="Breaching" width="80%">
</p>

*3 x 3 tunnel, 7 blocks deep · Crate fuse: instant · Cart fuse: instant*

Punches a 3 by 3 tunnel 7 blocks deep, even through obsidian. Only unbreakable blocks such as bedrock stop it. A crate fires the moment it is lit, straight into the block it was placed against, so place it on the wall you want to breach. As a missile or bomb cart it digs straight down. It also hurts anything within 3 blocks.

<details>
<summary><b>Recipes</b>: Breaching Explosives, Breaching Missile, Breaching Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/breaching-explosives.png" alt="Breaching Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/breaching.png" alt="Breaching Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/breaching.png" alt="Breaching Minecart recipe" width="80%">
</p>
</details>

#### Thermobaric

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/thermobaric.png" alt="Thermobaric" width="80%">
</p>

*Terrain up to 30 blocks, lethal within 50 · Crate fuse: 5 s · Cart fuse: 5 s*

A huge pressure blast. It levels terrain up to 30 blocks away in the open (much less into solid rock), leaves no drops, sets off other crates it reaches, and kills anything exposed within 50 blocks. Only solid cover saves you.

<details>
<summary><b>Recipes</b>: Thermobaric Explosives, Thermobaric Missile, Thermobaric Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/thermobaric-explosives.png" alt="Thermobaric Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/thermobaric.png" alt="Thermobaric Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/thermobaric.png" alt="Thermobaric Minecart recipe" width="80%">
</p>
</details>

#### Phantom Enemy

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/phantom-enemy.png" alt="Phantom Enemy" width="80%">
</p>

*4 phantoms per player within 24 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

Conjures 4 fake mobs (zombies, husks, spiders and creepers) around every player within 24 blocks. They appear 6 to 12 blocks away, mostly behind you, close in and swing, but never deal damage, and the creepers never explode. Hit one and it vanishes in a puff of smoke; the rest fade after 60 seconds, or when their victim dies or gets away.

<details>
<summary><b>Recipes</b>: Phantom Enemy Explosives, Phantom Enemy Missile, Phantom Enemy Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/phantom-enemy-explosives.png" alt="Phantom Enemy Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/phantom-enemy.png" alt="Phantom Enemy Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/phantom-enemy.png" alt="Phantom Enemy Minecart recipe" width="80%">
</p>
</details>

#### Darkness

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/darkness.png" alt="Darkness" width="80%">
</p>

*Darkness within 24 blocks, 3 min · Crate fuse: 5 s · Cart fuse: 5 s*

Gives every player and mob within 24 blocks the Darkness effect for 3 minutes, whoever set it off included. Breaks nothing.

<details>
<summary><b>Recipes</b>: Darkness Explosives, Darkness Missile, Darkness Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/darkness-explosives.png" alt="Darkness Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/darkness.png" alt="Darkness Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/darkness.png" alt="Darkness Minecart recipe" width="80%">
</p>
</details>

### Tier 3

Mixed in a Basin over a superheated Blaze Burner. Missile health 100.

#### Nuclear

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/nuclear.png" alt="Nuclear" width="80%">
</p>

*Terrain up to 35 blocks, lethal within 50, fallout to 80 · Crate fuse: 5 s · Cart fuse: 5 s*

The biggest blast short of antimatter. It levels terrain up to about 35 blocks away, leaves no drops and kills anything exposed within 50 blocks. Then fallout spreads up to 80 blocks, through walls: patches of stone and dirt turn into [Radioactive Stone and Dirt](#radioactive-dirt-and-stone), and leaves and plants wither away. Pigs and villagers it reaches turn into zombified piglins and zombie villagers.

A mushroom cloud rises over the crater, the same one as the antimatter blast but a bit smaller, and everyone within 50 blocks hears the detonation at full volume. Players further away, up to the server's view distance, hear it as a distant boom that arrives with the delay of distance. The cloud's size and range are in the `blast.nuclear` config (`mushroomCloud`, `mushroomCloudScale`, `mushroomCloudRange`).

<details>
<summary><b>Recipes</b>: Nuclear Explosives, Nuclear Missile, Nuclear Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/nuclear-explosives.png" alt="Nuclear Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/nuclear.png" alt="Nuclear Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/nuclear.png" alt="Nuclear Minecart recipe" width="80%">
</p>
</details>

#### Exothermic

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/exothermic.png" alt="Exothermic" width="80%">
</p>

*Nether land within 30 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

Opens with a TNT-sized blast and a beam of light that lifts the blocks nearby into the air. 5 seconds later the land within 30 blocks turns into the Nether: rock into netherrack, magma and lava, sand and gravel into soul sand, dirt into netherrack, water and ice boiled away, and fire everywhere. The sky turns to midnight.

<details>
<summary><b>Recipes</b>: Exothermic Explosives, Exothermic Missile, Exothermic Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/exothermic-explosives.png" alt="Exothermic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/exothermic.png" alt="Exothermic Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/exothermic.png" alt="Exothermic Minecart recipe" width="80%">
</p>
</details>

#### Endothermic

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/endothermic.png" alt="Endothermic" width="80%">
</p>

*Frozen land within 30 blocks · Crate fuse: 5 s · Cart fuse: 5 s*

Opens with a TNT-sized blast and a beam of light, then freezes the land within 30 blocks: water turns to ice, lava to obsidian, fire goes out, netherrack turns back into dirt and snow settles on top. Mobs caught in it (not players) are poisoned and slowed down for 2 minutes. The sky turns to morning.

<details>
<summary><b>Recipes</b>: Endothermic Explosives, Endothermic Missile, Endothermic Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/endothermic-explosives.png" alt="Endothermic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/endothermic.png" alt="Endothermic Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/endothermic.png" alt="Endothermic Minecart recipe" width="80%">
</p>
</details>

#### Anti-Gravitational

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/anti-gravitational.png" alt="Anti-Gravitational" width="80%">
</p>

*Lifts everything within 30 blocks for 2 min · Crate fuse: 5 s · Cart fuse: 5 s*

For 2 minutes it lifts the blocks within 30 blocks into the sky one by one, from the top down, and pushes every mob and player inside upwards. When it ends, gravity comes back and everything comes crashing down.

<details>
<summary><b>Recipes</b>: Anti-Gravitational Explosives, Anti-Gravitational Missile, Anti-Gravitational Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/anti-gravitational-explosives.png" alt="Anti-Gravitational Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/anti-gravitational.png" alt="Anti-Gravitational Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/anti-gravitational.png" alt="Anti-Gravitational Minecart recipe" width="80%">
</p>
</details>

#### Ender

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/ender.png" alt="Ender" width="80%">
</p>

*Vortex 20 blocks, 8 s · Crate fuse: 5 s · Cart fuse: 5 s*

Opens an ender vortex for 8 seconds that drags everything within 20 blocks towards its centre. Whatever reaches the middle is teleported to one random spot up to 150 blocks away. A player is not simply teleported: a giant Enderman looms over them and screams, the world tears open into a purple warp tunnel and the player is dragged to a far away spot, where the giant stands over them for a moment before it vanishes. The player cannot move during the cutscene (`blast.ender` in the config turns it off or changes the timing and distance). Eight Endermen turn up as it closes.

<details>
<summary><b>Recipes</b>: Ender Explosives, Ender Missile, Ender Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/ender-explosives.png" alt="Ender Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/ender.png" alt="Ender Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/ender.png" alt="Ender Minecart recipe" width="80%">
</p>
</details>

### Tier 4

Mixed in a Basin over a superheated Blaze Burner. Missile health 200.

#### Antimatter

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/antimatter.png" alt="Antimatter" width="80%">
</p>

*Erases 55 blocks, lethal within 110 · Crate fuse: 15 s · Cart fuse: 15 s*

Erases everything within 55 blocks, bedrock included, with no drops, and kills everything within 110 blocks. Other crates inside are wiped out rather than set off. It has a 15 second fuse, so run. If red matter is anywhere nearby, the antimatter is spent wiping it out instead and nothing else is harmed.

The blast is announced by a blinding white flash for every player within the blast and 100 blocks beyond it (shorter the further away), and a mushroom cloud rises over the crater, visible from far away. The crater opens up from the centre outwards over a few seconds instead of in one go. Flash range and length, the cloud and the pace are in the `blast.antimatter` config, the cloud's particle budget in the client config. The detonation sound is timed to the flash and the cloud: everyone within 100 blocks gets the full, loud blast, with the shockwave rolling in about a second after the flash, and players beyond that up to the view distance hear a distant boom.

<details>
<summary><b>Recipes</b>: Antimatter Explosives, Antimatter Missile, Antimatter Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/antimatter-explosives.png" alt="Antimatter Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/antimatter.png" alt="Antimatter Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/antimatter.png" alt="Antimatter Minecart recipe" width="80%">
</p>
</details>

#### Red Matter

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives/red-matter.png" alt="Red Matter" width="80%">
</p>

*Black hole, grows up to size 70 · Crate fuse: 5 s · Cart fuse: 5 s*

Spawns a black hole that eats every block around it, water and lava included, and grows each time it clears its surroundings, up to 70 times its starting size. It drags in mobs, players and items, and anything that reaches its core is destroyed. Missiles and lit explosives that fall in go off. With nothing left to eat it slowly shrinks and dies. An Antimatter blast nearby kills it at once.

<details>
<summary><b>Recipes</b>: Red Matter Explosives, Red Matter Missile, Red Matter Minecart</summary>

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/red-matter-explosives.png" alt="Red Matter Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missiles/red-matter.png" alt="Red Matter Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-carts/red-matter.png" alt="Red Matter Minecart recipe" width="80%">
</p>
</details>

## 🛒 Bomb carts

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/bomb-carts.png" alt="Bomb carts" width="85%">
</p>

A minecart with an explosive crate in it, one for every explosive. Put it on rails by hand or with a dispenser, and
run it over a powered **activator rail** to light the fuse. The fuse is 5 seconds for most carts (see each
explosive).

A cart goes off at once if it is broken while moving, set on fire, hit by a burning arrow, caught in another
explosion, slammed into a wall at speed or dropped 3 or more blocks. Broken while standing still, it drops as an item.
Bomb carts stack to 3.

Make one by deploying an explosive crate onto a Minecart, or combine both at a crafting table. A plain TNT block still
makes the vanilla TNT Minecart.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-cart.png" alt="How every bomb cart is made" width="80%">
</p>

## 🧰 Tools

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools.png" alt="Handheld tools" width="100%">
</p>

| Tool | In short |
|---|---|
| [Laser Range Finder](#laser-range-finder) | Reads the coordinates of a block and hands them to a launcher. |
| [Remote Detonator](#remote-detonator) | Fires launchers and wired explosive crates on its radio channel, at any distance. |
| [Laser Designator](#laser-designator) | Marks a block and calls a strike on it. |
| [Rocket Launcher: Direct](#rocket-launcher-direct) | Fires a missile in a straight line from your shoulder. |
| [Rocket Launcher: Ballistic](#rocket-launcher-ballistic) | Lobs a missile in a high arc onto where you aim. |

**Radio channels.** Launch Controllers and Cruise Turrets each have a radio channel. Right-click one of them
with a Remote Detonator or Laser Designator to tune the tool to the same channel. Explosive crates work the other way
round: right-click a crate with a Remote Detonator to give the crate the detonator's channel. Radio reaches the whole
dimension.

### Laser Range Finder

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools/laser-range-finder.png" alt="Laser Range Finder" width="260">
</p>

- **Right-click** a block, up to 200 blocks away, to store its exact coordinates. The tooltip shows what is stored.
- **Right-click a Launch Controller or Cruise Turret** with it to hand the target over.
- **Shift + right-click** clears it.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/laser-range-finder.png" alt="Laser Range Finder recipe" width="80%">
</p>

### Remote Detonator

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools/remote-detonator.png" alt="Remote Detonator" width="260">
</p>

- **Right-click** a Launch Controller or Cruise Turret to tune the detonator to its channel.
- **Right-click an explosive crate** to wire it to the detonator's channel. A detonator with no channel yet picks a
  random one. Wire as many crates as you like; a crate stays wired until it goes off or is mined.
- **Right-click** anywhere else to send the fire signal. Every controller, turret and tower on that channel fires at
  the target it already has, and every crate wired to it is lit, however far away it is. Answers show above your
  hotbar.
- Laser Designator strikes and radar alerts don't set off crates, only the detonator's fire signal does.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/remote-detonator.png" alt="Remote Detonator recipe" width="80%">
</p>

### Laser Designator

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools/laser-designator.png" alt="Laser Designator" width="260">
</p>

- **Right-click** a Launch Controller or Cruise Turret to tune the designator to its channel.
- **Right-click** a block up to 200 blocks away to paint it: every launcher on the channel takes it as the new target
  and fires. Hold the button to repeat about once a second.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/laser-designator.png" alt="Laser Designator recipe" width="80%">
</p>

### Rocket Launcher: Direct

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools/rocket-launcher.png" alt="Rocket Launcher: Direct" width="260">
</p>

A shoulder launcher. **Hold right-click** to raise it and let go to fire the first missile in your inventory in a
straight line where you aim, at 40 blocks a second. It needs no power, just a missile, and has a 1 second cooldown.
Fire a Surface to Air Missile to take down a missile yourself.

- Sneak as you let go to ride the missile.
- With a Lead in your other hand, a mob you have on a leash within 7 blocks rides it instead.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/rocket-launcher.png" alt="Rocket Launcher: Direct recipe" width="80%">
</p>

### Rocket Launcher: Ballistic

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools/ballistic-rocket-launcher.png" alt="Rocket Launcher: Ballistic" width="260">
</p>

Works like the direct launcher, but lobs the missile in a high arc onto the block you aim at. The target has to be a
block within 500 blocks and at least 20 blocks away.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/ballistic-rocket-launcher.png" alt="Rocket Launcher: Ballistic recipe" width="80%">
</p>

## 🧪 Materials and effects

### Poison Powder

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/materials/poison-powder.png" alt="Poison Powder" width="260">
</p>

A toxic powder that goes into [Chemical Explosives](#chemical). Mill a Spider Eye (2 or 3 powder) or a Poisonous
Potato (1 or 2) with a Millstone or Crushing Wheels, or craft a Spider Eye and a Poisonous Potato together for 3.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/poison-powder.png" alt="Poison Powder recipe" width="80%">
</p>

### Antidote

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/materials/antidote.png" alt="Antidote" width="260">
</p>

Drink it (hold right-click) to clear **every** effect, good and bad: gas, poison that milk can't cure and radiation
included. Mix 250 mB of milk with 3 seeds in a Basin, or craft a Milk Bucket with 3 seeds. Either way you get 6.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/antidote.png" alt="Antidote recipe" width="80%">
</p>

### Radioactive Dirt and Stone

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/materials/radioactive-dirt.png" alt="Radioactive Dirt" width="260">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/materials/radioactive-stone.png" alt="Radioactive Stone" width="260">
</p>

Fallout left behind by a [Nuclear](#nuclear) blast. They tick away like a Geiger counter and hurt anything within 5
blocks with a little damage and a short dose of Wither. Over time they decay back into plain dirt and stone. Mine them
with a pickaxe to clear the area faster. They can't be crafted.

### Protective gear

Gas and radiation check the armour you wear. Armour from other mods, such as gas masks and hazmat suits, can
protect you once it is listed in `item_ratings` in `create_icbm-main.toml` (for example
`"somemod:gas_mask=0.75"`). Ratings add up across the pieces you wear, and nothing counts without a helmet.
Debilitation gas needs a rating of 0.1, chemical gas 0.5, viral gas 0.8 and radiation 1.0. A rating at or above the
threshold, the rating is the chance that each dose is blocked: 0.75 blocks three doses in four.

## 🛡️ Air defence

1. Place a **Radar Station** and spin it at 16 RPM or more. Open it, set its detection range and give it a radio
   channel.
2. Place one or more **Cruise Turrets** with a shaft below, load each with a **Surface to Air Missile** and type the
   radar's channel into them. Feed them from a hopper or funnel so they reload by themselves.
3. When a missile carrying a warhead flies towards the radar, it sends an alert every 2 seconds. The turrets swing
   round and fire, and each SAM locks onto the missile.
4. The radar's redstone output can sound sirens or close blast doors at the same time.

A Launch Controller on the radar's channel fires its pads at the incoming missile's position, so keep your silos on a
different channel unless that's what you want.

## 🗄️ Storage and automation

- Launch Pads and Cruise Turrets take missiles from hoppers, Create funnels and chutes on any side. A Launch Controller,
  Launch Gantry or Launch Connector accepts missiles for every pad in its network.
- **Tom's Simple Storage autostock:** put a **Basic Inventory Hopper** with its back on your storage network (a cable
  or connector), a missile in its filter slot and its front against the **Launch Controller**. It refills every empty
  pad in that launch network from storage. Gantries and controllers are hidden from Tom's Inventory Connector so
  missiles aren't counted twice; connect the pads themselves to see their missiles in the terminal.
- A **redstone pulse** into the controller fires the whole network, so a clock or a Create sequencer can run timed
  salvos.

## 🗺️ BlueMap

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/bluemap.png" alt="BlueMap markers: a missile in flight, a mushroom cloud at an impact and a parcel from a Delivery Missile" width="100%">
</p>

Install [BlueMap](https://bluemap.bluecolored.de/) next to Create: ICBM and every missile in the air shows up on its
web map. Nothing to set up: the markers appear as soon as BlueMap has loaded.

- **Missiles in flight** move across the map, with the icon pointing the way they fly. Missiles crossing unloaded
  chunks are shown too, along their path to the target.
- **Click a missile** to see what it is, **who launched it** and where it is heading.
- **Impacts** leave a small **mushroom cloud** where the warhead went off. Click it to see the missile and who launched
  it. The cloud disappears from the map after **1 minute**.
- **Delivery Missiles** leave a **parcel** where their Mail is, with who sent it. It **stays on the map until someone
  opens the Mail**, even across restarts. A delivery that went straight into a chest shows its parcel for 1 minute.
- The markers sit on their own **Missiles** layer, which you can hide in BlueMap's menu.

Create: ICBM only adds its own marker layer. It never touches BlueMap's rendered tiles, textures or settings, and
BlueMap works exactly as before.

BlueMap publishes markers every 10 seconds, so a missile moves across the map in steps. If you serve the map from your
own webserver instead of BlueMap's built-in one, set `write-markers-interval` in BlueMap's `plugin.conf` (10 seconds
matches the built-in one), otherwise BlueMap only writes markers for the page every few minutes.

Blocks on the map showing the wrong textures come from BlueMap's browser cache, after BlueMap re-rendered a map or
rebuilt its texture list (a `/bluemap purge` does that). Click **Update Map** in BlueMap's menu or reload the page
with Ctrl+F5. If it stays wrong, check BlueMap's log for "Failed to load textures for map", run
`/bluemap purge <map>` and let it render again.

The settings live in `create_icbm-mods-bluemap.toml`:

| Option | Default | What it changes |
|---|---|---|
| `enabled` | `true` | Set to `false` to keep missiles off the map. |
| `update_ticks` | 100 | How often missile markers move, in ticks (20 ticks is 1 second). BlueMap publishes them every 10 seconds anyway. |
| `impact_marker_seconds` | 60 | How long the mushroom cloud stays on the map, and the parcel of a delivery that went into a chest. `0` turns them off. Parcels of unopened Mail stay until it is opened. |

## 🎬 Effects, sounds and visibility

Everything below is drawn and played by the **client** from small packets the server sends, so none of it costs the
server anything per frame.

### Mushroom clouds

[Nuclear](#nuclear) and [Antimatter](#antimatter) blasts raise a mushroom cloud: a fireball, a rising stem and a
billowing cap that opens up while it climbs and keeps leaking smoke for a few seconds afterwards. The antimatter cloud
is full size, the nuclear one is 70 % of it (`blast.nuclear.mushroomCloudScale`, 0.7 by default).

- **Who sees it:** every player in the same dimension within `mushroom_cloud_range` blocks of the blast (512 by default
  for both; antimatter never less than its blast radius plus its flash range). The server sends one small packet with the
  position, the size and the duration, and each client builds the cloud itself.
- **What it costs a player:** at most `client.mushroom_cloud_particles` particles per tick for one cloud (90 by
  default). The budget shrinks with the game's *Particles* setting, and with distance from 128 blocks out down to a
  quarter at 600 blocks, and at most three clouds are drawn at once. Set it to `0` to switch the cloud off on your client.
- **Switching it off for everyone:** `blast.nuclear.mushroomCloud = false` or `blast.antimatter.mushroom_cloud = false`.
  Nuclear then falls back to its old smoke ring.

### Antimatter flash

Everyone inside the blast and up to `blast.antimatter.flash_extra_range` blocks beyond it (100 by default) is blinded by
a white flash: fully white for up to `flash_ticks` ticks (60) at the centre, shorter the further away, then it fades
out. The crater opens from the centre outwards over `edit_ticks` ticks (120) instead of in one lump. `flash_extra_range =
0` turns the flash off. The [Flashbang](#flashbang) uses the same screen flash, only smaller.

### Ender abduction cutscene

When an [Ender](#ender) vortex pulls a **player** into its middle, they are not just teleported:

1. **Stare** (`stare_ticks`, 45): a giant Enderman appears in front of the player, screams and steps closer. The camera
   and controls are taken over. Meanwhile the server picks a destination 60 to 150 blocks away and loads its chunk in
   the background with a short chunk ticket, so there is no stall.
2. **Warp** (`min_ticks_in_warp`, 45, longer if the destination is still loading): the screen turns into a purple
   tunnel of streaks and the player is lifted off the ground.
3. **Arrival** (`arrival_ticks`, 60): the player lands, a second giant stands over them for a moment, then it vanishes in
   a puff and control comes back. The player cannot be taken again for `cooldown_ticks` (200).

Other players see dense ender particles around the blast, and a player who logs out, dies or changes dimension ends
their own cutscene on the spot. A watchdog on the client gives control back if the end of the cutscene never arrives.
Mobs and items are always teleported instantly. `blast.ender.abduct_players = false` skips the cutscene entirely.

### Explosion sounds

A vanilla sound can only be heard a few dozen blocks away, so blasts send each player **their own sound packet**,
placed a few blocks from them in the direction of the blast, with the delay that distance would give (sound is
treated as 20 blocks per tick, at most 2 seconds late). Only players within the server's view distance get one.

| Blast | Players within | Hear | Players further out, up to the view distance |
|---|---|---|---|
| Nuclear | 50 blocks | the full nuclear detonation | a distant explosion, quieter the further away |
| Antimatter | 100 blocks | the full antimatter blast, timed to the flash and the cloud | a distant explosion, quieter the further away |
| Any other explosion with a radius of 12 blocks or more | none | | a distant explosion, quieter the further away |

Gas clouds, vortexes and other effect blasts (Debilitation, Chemical, Contagious, Ender, Flashbang, Recon...) are not
explosions and send no distant sound. Smaller explosions only use their normal sound.

### Missiles you can see

- A missile is a **real entity** for every player within the server's view distance of it, tracked up to 500 blocks,
  and is drawn in flight right down to the ground. Missiles never pop out of existence just because the chunks
  ahead stopped ticking: while a player is in view, the missile keeps its chunks loaded (`keep_visible_in_flight`).
- Far from every player a missile is **simulated** instead, with no entity at all (see
  Server performance), and re-enters the world above its target.
- The client smooths the flight itself. The server sends a position every 2 ticks and the client dead-reckons the
  frames in between, and snaps back only if the two disagree a lot, so missiles glide instead of stepping.
- The missile's engine is heard from as far as the client's render distance, and the engine and launch smoke can be
  turned off in the client config.

## 🖥️ Server performance

Create: ICBM is meant for big modpack servers, where a salvo of missiles must not cost the server its tick rate.
Everything here works out of the box; the settings are there to tune it.

| Area | What it does | Setting (default) |
|---|---|---|
| **Paced block edits** | Big blasts (nuclear, antimatter, thermobaric...) are worked out first and then applied a slice per tick. All dimensions together share one time budget, so ten blasts do not take ten times as long per tick. | `block_edit_ms_per_tick` (20), `blast_ms_per_tick` (15), `max_blast_edit_blocks_per_tick` (20000) |
| **Adaptive budget** | The budgets shrink by themselves while the server is slow: when the smoothed tick time stays above 45 ms they drop 3 % a tick, down to 10 % of normal, and they grow back 2 % a tick once it is under 35 ms. A blast always finishes in the end. | `adaptive_budget` (`true`) |
| **Worker threads** | Blast shapes are calculated off the main thread from a snapshot of the chunks. Results are handed back to the server thread, which only applies them. Edits that fall in chunks which have meanwhile unloaded are skipped instead of force-loading them. | `thread_count` (1, up to 8) |
| **Missile chunk loading** | A missile only holds the chunks right ahead of it, with short tickets that run out on their own, like a thrown ender pearl. All missiles of one dimension together may hold at most 512 tickets, and a missile always keeps its own chunk and the next one. | `direct_fire_chunk_loading` (`true`), `max_missile_chunk_tickets` (512) |
| **Simulated flight** | A long-range missile that leaves the loaded area stops being an entity. A tracker counts it down once per second at `simulation_flight_speed` and spawns it again above the target. Costs a few numbers per missile. | `simulation_flight_speed` (20 blocks/s), `arc_distance_limit` (500) |
| **Network** | Missiles send a position every 2 ticks and the client smooths the rest. Mushroom clouds, flashes and cutscenes are one small packet each. Debris effects are capped. Sound packets only go to players within view distance. | `block_effects_per_blast` (64), `block_effects_per_tick` (1024) |
| **Radar** | A radar only scans four times a second, over a square, and drops contacts that have died. | `scan_rate` (5 ticks) |
| **Claims and protection** | Every blast asks claim mods (FTB Chunks, Open Parties and Claims...) before editing a block. | `protection_events` (`true`) |
| **Clean shutdown** | Queues, tickets, pending sounds and cutscenes are dropped when a level unloads or the server stops, so nothing leaks between worlds. | none |

Things worth knowing on a busy server:

- **A salvo does not freeze the server.** If a tick is already slow, the blast work waits for the next tick.
- **Nothing force-loads chunks for effects.** Fragments, gas clouds and blasts treat unloaded chunks as solid or skip
  them.
- **Bad input cannot hang the server.** Targets that are `NaN` or infinite are rejected by the launchers, and the
  `/icbm` commands refuse `NaN` and infinite numbers.
- **When something goes wrong**, `/icbm lag [radius]` clears every running blast and Create: ICBM entity around you, and
  `/icbm remove all` clears missiles and blasts in the whole dimension.
- **Smaller cost for clients too:** `client.mushroom_cloud_particles` and the vanilla *Particles* setting keep the
  clouds cheap.

## 🔌 Compatibility

Create: ICBM only needs **Create** (and the libraries Create brings). Everything else is optional and is only touched
when it is installed, so a missing or changed mod can never stop the game from starting.

| Mod | What works | How it is reached |
|---|---|---|
| **Create** 6.0.10+ (required) | Stress and rotation, Mechanical Crafter / Mixer / Deployer / Millstone recipes, Blaze Burners, goggles, Ponder, tooltips, Kinetic Appliances tag | Directly |
| **JEI** 19+ | Every explosive, missile, bomb cart and loaded Delivery Missile is its own entry, with info pages for the Missile Module, Delivery Missile and radioactive blocks | JEI's plugin system, off with `disable` in `create_icbm-mods-jei.toml` |
| **BlueMap** 5.x | Missiles in flight, impact mushroom clouds and Mail parcels on a **Missiles** marker layer | BlueMap's public API, off with `enabled` in `create_icbm-mods-bluemap.toml`. It never touches tiles, textures or settings |
| **Sable** (Create Aeronautics) | Missiles hit ships where they fly, launchers work on moving decks, blasts tear blocks off nearby ships | Sable's public API through reflection, every call does nothing if it fails |
| **Mekanism** | Missiles fly through a Teleporter portal and come out of the other one | Registry names and reflection, switched off with a warning if Mekanism's API changes |
| **ServerCore** | Missiles, blasts, bomb carts and flying blocks are added to its activation-range exclusions when the server starts, so they do not freeze far from players | Reflection, a failure only logs a warning |
| **Immersive Optimization** and other tick throttlers | Server-side missiles and blasts are flagged to keep ticking. If one still stalls, add the `create_icbm` namespace to the optimizer's blacklist | `noCulling` flag, plus the missile's own chunk tickets |
| **Tom's Simple Storage** | An Inventory Hopper in front of a Launch Controller restocks every empty pad in the network. Gantries and controllers are hidden from the Inventory Connector so missiles are not counted twice | Block tags in the `toms_storage` namespace, plain item handlers |
| **Claim and protection mods** (FTB Chunks, Open Parties and Claims...) | Claimed land is spared by blasts | The explosion event those mods already listen to, `protection_events` |
| **Armour mods** (gas masks, hazmat suits...) | Count as protection against gas and radiation once listed | `protective_armor.item_ratings` |
| **Hoppers, funnels, chutes, Create vaults** and other item handlers | Load Launch Pads, Cruise Turrets and Launch Controllers, and receive the cargo of a Delivery Missile | NeoForge item handler capability |

Both the server and every client need the mod. Optional mods only need to be installed on the side where they work
(BlueMap, ServerCore and Immersive Optimization on the server, JEI on the client).

## 🔨 Crafting recipes

Every recipe uses only Create and vanilla items. Almost everything can be made **two ways**, on Create's machines
(Mechanical Crafting, Mixing, Deploying, Milling) or at a plain crafting table, and both give the same item. JEI shows
them all, in Create's own recipe categories, and each item's recipe card also sits in its own section above.

A `·` in a grid is an empty slot. Every grid is read left to right, top to bottom, and a recipe that is mirrored
still works. The number after → is the **yield**: how many items one craft or one mix gives.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/explosives-overview.png" alt="How the explosives are made from each other" width="100%">
</p>

### How the Create machines are used

- **Mixing** happens in a **Basin** under a **Mechanical Mixer**. The recipe needs the listed items (and fluid)
  in the basin. *Plain* mixes need nothing else. **Heated** mixes need a lit **Blaze Burner** under the basin.
  **Superheated** mixes need a Blaze Burner that has been fed a **Blaze Cake** (its flame turns blue).
  Tier 1 explosives are plain, tier 2 are heated, tiers 3 and 4 are superheated.
- **Deploying**: put the *base item* on a Depot or a belt below a **Deployer** that holds the *added item*.
  A Deployer holding an explosive crate over a Missile Module makes an explosive missile. Each deploy uses up the
  crate it holds.
- **Mechanical crafting** is done in a grid of **Mechanical Crafters** laid out like the pattern (mirrors allowed).
- **Milling** is a **Millstone** or a pair of Crushing Wheels.

### Machines and launch network

| Item | Method | Ingredients | Output |
|---|---|---|---|
| **Launch Pad** | Shaped crafting | `BEB`<br>`SAS`<br>`ICI`<br>`B` Brass Sheet, `E` Electron Tube, `S` Iron Sheet, `A` Andesite Casing, `I` Industrial Iron Block, `C` Cogwheel | 1× |
| **Launch Pad** | Create: Mechanical Crafter | `BEB`<br>`IAI`<br>`SHS`<br>`B` Brass Sheet, `E` Electron Tube, `I` Industrial Iron Block, `A` Andesite Casing, `S` Iron Sheet, `H` Shaft | 1× |
| **Launch Controller** | Shaped crafting | `·N·`<br>`ECL`<br>`KKK`<br>`N` Nixie Tube, `E` Electron Tube, `C` Brass Casing, `L` Analog Lever, `K` Copper Sheet | 1× |
| **Launch Controller** | Create: Mechanical Crafter | `·G·`<br>`ECL`<br>`·S·`<br>`G` Glass, `E` Electron Tube, `C` Brass Casing, `L` Analog Lever, `S` Copper Sheet | 1× |
| **Launch Gantry** | Shaped crafting | `GBG`<br>`GBG`<br>`G` Metal Girder, `B` Brass Sheet | 1× |
| **Launch Connector** | Shaped crafting | `AWA`<br>`WHW`<br>`AWA`<br>`A` Andesite Alloy, `W` Copper Sheet, `H` Hopper | 4× |
| **Radar Station** | Shaped crafting | `GBG`<br>`ESE`<br>`KCK`<br>`G` Golden Sheet, `B` Brass Sheet, `E` Electron Tube, `S` Launch Controller, `K` Copper Casing, `C` Cogwheel | 1× |
| **Radar Station** | Create: Mechanical Crafter | `BGB`<br>`PSP`<br>`KHK`<br>`B` Brass Sheet, `G` Golden Sheet, `P` Precision Mechanism, `S` Launch Controller, `K` Copper Casing, `H` Shaft | 1× |
| **Cruise Turret** | Shaped crafting | `BTB`<br>`SLP`<br>`KCK`<br>`B` Brass Sheet, `T` Turntable, `S` Launch Controller, `L` Launch Pad, `P` Precision Mechanism, `K` Copper Sheet, `C` Brass Casing | 1× |
| **Cruise Turret** | Create: Mechanical Crafter | `CPC`<br>`SLB`<br>`ITI`<br>`C` Copper Sheet, `P` Precision Mechanism, `S` Launch Controller, `L` Launch Pad, `B` Brass Casing, `I` Industrial Iron Block, `T` Turntable | 1× |

### Missile parts

The Missile Casing is made at a plain crafting table. Missile Modules, Surface to Air Missiles and Delivery Missiles
can be made on Mechanical Crafters or at a crafting table. A Surface to Air Missile and a Delivery Missile need a
finished Missile Module.

| Item | Method | Ingredients | Output |
|---|---|---|---|
| **Missile Casing** | Shaped crafting | `·B·`<br>`C·C`<br>`C·C`<br>`B` Brass Sheet, `C` Copper Sheet | 1× |
| **Missile Module** | Shaped crafting | `·E·`<br>`ACA`<br>`·B·`<br>`E` Electron Tube, `A` Andesite Alloy, `C` Missile Casing, `B` Blaze Powder | 1× |
| **Missile Module** | Create: Mechanical Crafter | `E`<br>`C`<br>`A`<br>`B`<br>`E` Electron Tube, `A` Andesite Alloy, `C` Missile Casing, `B` Blaze Powder | 1× |
| **Surface to Air Missile** | Shaped crafting | `·T·`<br>`SES`<br>`·M·`<br>`T` Electron Tube, `S` Iron Sheet, `E` Condensed Explosives, `M` Missile Module | 1× |
| **Surface to Air Missile** | Create: Mechanical Crafter | `T`<br>`E`<br>`M`<br>`T` Electron Tube, `E` Condensed Explosives, `M` Missile Module | 1× |
| **Delivery Missile** | Shaped crafting | `·C·`<br>`ABA`<br>`·M·`<br>`C` Wooden Chest, `A` Andesite Alloy, `B` Brass Sheet, `M` Missile Module | 1× |
| **Delivery Missile** | Create: Mechanical Crafter | `C`<br>`A`<br>`M`<br>`C` Wooden Chest, `A` Andesite Alloy, `M` Missile Module | 1× |

### Explosives

All 26 explosives, tier by tier in the order below. Each one is made from a lower explosive, so you work your way up
from Condensed Explosives. The table shows both recipes for every explosive.

| Tier | Explosive | Create: Mechanical Mixer | Crafting table (shaped) |
|---|---|---|---|
| 1 | **Condensed Explosives** | Mixer<br>5× TNT, 4× Redstone Dust<br>→ **3× Condensed Explosives** | `TST`<br>`RAR`<br>`TST`<br>`T` TNT, `S` Iron Sheet, `R` Redstone Dust, `A` Andesite Alloy<br>→ **3×** |
| 1 | **Shrapnel Explosives** | Mixer<br>Condensed Explosives, 8× Arrow<br>→ **Shrapnel Explosives** | `AAA`<br>`SCS`<br>`AAA`<br>`A` Arrow, `S` Iron Sheet, `C` Condensed Explosives<br>→ **1×** |
| 1 | **Incendiary Explosives** | Mixer<br>Shrapnel Explosives, 4× Cinder Flour, Lava (250 mB)<br>→ **Incendiary Explosives** | `·K·`<br>`FSF`<br>`FLF`<br>`K` Fire Charge, `F` Cinder Flour, `S` Shrapnel Explosives, `L` Lava Bucket<br>→ **1×** |
| 1 | **Debilitation Explosives** | Mixer<br>Shrapnel Explosives, 3× Cinder Flour, Water (500 mB)<br>→ **3× Debilitation Explosives** | `FEF`<br>`·S·`<br>`FWF`<br>`F` Cinder Flour, `E` Fermented Spider Eye, `S` Shrapnel Explosives, `W` Water Bucket<br>→ **3×** |
| 1 | **Chemical Explosives** | Mixer<br>Debilitation Explosives, 8× Poison Powder<br>→ **Chemical Explosives** | `PPP`<br>`PDP`<br>`CCC`<br>`P` Poison Powder, `D` Debilitation Explosives, `C` Copper Sheet<br>→ **1×** |
| 1 | **Anvil Explosives** | Mixer<br>Repulsive Explosives, 8× Anvil<br>→ **10× Anvil Explosives** | `NNN`<br>`SRS`<br>`N` Anvil, `S` Iron Sheet, `R` Repulsive Explosives<br>→ **4×** |
| 1 | **Repulsive Explosives** | Mixer<br>2× Condensed Explosives, Piston<br>→ **Repulsive Explosives** | `·P·`<br>`CAC`<br>`P` Piston, `C` Condensed Explosives, `A` Andesite Alloy<br>→ **1×** |
| 1 | **Attractive Explosives** | Mixer<br>2× Condensed Explosives, Sticky Piston<br>→ **Attractive Explosives** | `·P·`<br>`CAC`<br>`P` Sticky Piston, `C` Condensed Explosives, `A` Andesite Alloy<br>→ **1×** |
| 1 | **Colorful Explosives** | Mixer<br>Repulsive Explosives, 2× Red Dye, 2× Yellow Dye, 2× Green Dye, 2× Blue Dye<br>→ **Colorful Explosives** | `RYG`<br>`BXO`<br>`PML`<br>`R` Red Dye, `Y` Yellow Dye, `G` Green Dye, `B` Blue Dye, `X` Repulsive Explosives, `O` Orange Dye, `P` Purple Dye, `M` Magenta Dye, `L` Lime Dye<br>→ **1×** |
| 1 | **Smoke Marking Explosives** | Mixer<br>Repulsive Explosives, 8× Charcoal<br>→ **16× Smoke Marking Explosives** | `CCC`<br>`FRF`<br>`CCC`<br>`C` Charcoal, `F` Cinder Flour, `R` Repulsive Explosives<br>→ **12×** |
| 1 | **Flashbang Explosives** | Mixer<br>Condensed Explosives, 4× Glowstone Dust, 2× Gunpowder<br>→ **8× Flashbang Explosives** | `GPG`<br>`PXP`<br>`GPG`<br>`G` Glowstone Dust, `P` Gunpowder, `X` Condensed Explosives<br>→ **6×** |
| 1 | **Recon Explosives** | Mixer<br>Smoke Marking Explosives, Spyglass, Electron Tube, 4× Redstone Dust<br>→ **6× Recon Explosives** | `·S·`<br>`RXR`<br>`·E·`<br>`S` Spyglass, `R` Redstone Dust, `E` Electron Tube, `X` Smoke Marking Explosives<br>→ **4×** |
| 2 | **Fragmentation Explosives** | Mixer + Blaze Burner (heated)<br>4× Shrapnel Explosives, Incendiary Explosives<br>→ **Fragmentation Explosives** | `SBS`<br>`GIG`<br>`S·S`<br>`S` Shrapnel Explosives, `B` Blaze Powder, `G` Gunpowder, `I` Incendiary Explosives<br>→ **1×** |
| 2 | **Contagious Explosives** | Mixer + Blaze Burner (heated)<br>4× Chemical Explosives, Rotten Flesh<br>→ **2× Contagious Explosives** | `CRC`<br>`RBR`<br>`CRC`<br>`C` Chemical Explosives, `R` Rotten Flesh, `B` Blaze Powder<br>→ **2×** |
| 2 | **Sonic Explosives** | Mixer + Blaze Burner (heated)<br>Repulsive Explosives, 4× Note Block, 4× Iron Ingot<br>→ **Sonic Explosives** | `NWN`<br>`SRS`<br>`NBN`<br>`N` Note Block, `W` Steam Whistle, `S` Iron Sheet, `R` Repulsive Explosives, `B` Blaze Powder<br>→ **1×** |
| 2 | **Breaching Explosives** | Mixer + Blaze Burner (heated)<br>Condensed Explosives, 6× Gunpowder<br>→ **2× Breaching Explosives** | `KKK`<br>`GCG`<br>`GBG`<br>`K` Copper Sheet, `G` Gunpowder, `C` Condensed Explosives, `B` Blaze Powder<br>→ **2×** |
| 2 | **Thermobaric Explosives** | Mixer + Blaze Burner (heated)<br>4× Chemical Explosives, 4× Incendiary Explosives, Repulsive Explosives<br>→ **Thermobaric Explosives** | `CIC`<br>`IRI`<br>`CBC`<br>`C` Chemical Explosives, `I` Incendiary Explosives, `R` Repulsive Explosives, `B` Blaze Powder<br>→ **1×** |
| 2 | **Phantom Enemy Explosives** | Mixer + Blaze Burner (heated)<br>Smoke Marking Explosives, 2× Phantom Membrane, Rotten Flesh, Spider Eye, Gunpowder<br>→ **2× Phantom Enemy Explosives** | `MRM`<br>`SXG`<br>`MBM`<br>`M` Phantom Membrane, `R` Rotten Flesh, `S` Spider Eye, `G` Gunpowder, `B` Blaze Powder, `X` Smoke Marking Explosives<br>→ **1×** |
| 2 | **Darkness Explosives** | Mixer + Blaze Burner (heated)<br>Debilitation Explosives, 4× Ink Sac, 2× Coal, 2× Soul Sand<br>→ **2× Darkness Explosives** | `ISI`<br>`CXC`<br>`IBI`<br>`I` Ink Sac, `S` Soul Sand, `C` Coal, `B` Blaze Powder, `X` Debilitation Explosives<br>→ **1×** |
| 3 | **Nuclear Explosives** | Mixer + Superheated Blaze Burner<br>4× Thermobaric Explosives, 5× Wither Skeleton Skull<br>→ **Nuclear Explosives** | `TWT`<br>`WCW`<br>`TWT`<br>`T` Thermobaric Explosives, `W` Wither Skeleton Skull, `C` Blaze Cake<br>→ **1×** |
| 3 | **Exothermic Explosives** | Mixer + Superheated Blaze Burner<br>8× Incendiary Explosives, Glass<br>→ **Exothermic Explosives** | `MCM`<br>`ITI`<br>`MLM`<br>`M` Magma Block, `C` Blaze Cake, `I` Incendiary Explosives, `T` Thermobaric Explosives, `L` Lava Bucket<br>→ **1×** |
| 3 | **Endothermic Explosives** | Mixer + Superheated Blaze Burner<br>Attractive Explosives, 4× Ice, 4× Snow Block<br>→ **Endothermic Explosives** | `ISI`<br>`STS`<br>`ISI`<br>`I` Packed Ice, `S` Snow Block, `T` Thermobaric Explosives<br>→ **1×** |
| 3 | **Anti-Gravitational Explosives** | Mixer + Superheated Blaze Burner<br>Shrapnel Explosives, 8× Eye of Ender<br>→ **Anti-Gravitational Explosives** | `FPF`<br>`ESE`<br>`FPF`<br>`F` Phantom Membrane, `P` Propeller, `E` Eye of Ender, `S` Sonic Explosives<br>→ **1×** |
| 3 | **Ender Explosives** | Mixer + Superheated Blaze Burner<br>Attractive Explosives, 4× Eye of Ender, 4× End Stone<br>→ **Ender Explosives** | `NPN`<br>`EBE`<br>`NPN`<br>`N` End Stone, `P` Ender Pearl, `E` Eye of Ender, `B` Breaching Explosives<br>→ **1×** |
| 4 | **Antimatter Explosives** | Mixer + Superheated Blaze Burner<br>Nuclear Explosives, 8× Nether Star<br>→ **Antimatter Explosives** | `SPS`<br>`SNS`<br>`SCS`<br>`S` Nether Star, `P` Precision Mechanism, `N` Nuclear Explosives, `C` Blaze Cake<br>→ **1×** |
| 4 | **Red Matter Explosives** | Mixer + Superheated Blaze Burner<br>Antimatter Explosives, 8× Nether Star<br>→ **Red Matter Explosives** | `SES`<br>`SAS`<br>`SCS`<br>`S` Nether Star, `E` Ender Explosives, `A` Antimatter Explosives, `C` Blaze Cake<br>→ **1×** |

### Explosive missiles and bomb carts

Every explosive crate makes its own **explosive missile** and its own **bomb cart**, and the recipe is the same for all
26. The only difference is the item you start from:

| | Missile | Bomb cart |
|---|---|---|
| **Start from** | a **Missile Module** | a vanilla **Minecart** |
| **On a Create Deployer** | Missile Module on the Depot, explosive crate in the Deployer | Minecart on the Depot, explosive crate in the Deployer |
| **At a crafting table** | Missile Module + explosive crate, in any arrangement (shapeless) | Minecart + explosive crate, in any arrangement (shapeless) |
| **You get** | 1 explosive missile of that kind | 1 bomb cart of that kind |

A plain TNT block still makes the vanilla TNT Minecart.

| Tier | Explosive crate | Explosive missile | Bomb cart |
|---|---|---|---|
| 1 | Condensed Explosives | **Conventional Missile** | **Explosive Cart** |
| 1 | Shrapnel Explosives | **Shrapnel Missile** | **Shrapnel Minecart** |
| 1 | Incendiary Explosives | **Incendiary Missile** | **Incendiary Minecart** |
| 1 | Debilitation Explosives | **Debilitation Missile** | **Debilitation Minecart** |
| 1 | Chemical Explosives | **Chemical Missile** | **Chemical Minecart** |
| 1 | Anvil Explosives | **Anvil Missile** | **Anvil Minecart** |
| 1 | Repulsive Explosives | **Repulsive Missile** | **Repulsive Minecart** |
| 1 | Attractive Explosives | **Attractive Missile** | **Attractive Minecart** |
| 1 | Colorful Explosives | **Colorful Missile** | **Colorful Minecart** |
| 1 | Smoke Marking Explosives | **Smoke Marking Missile** | **Smoke Marking Minecart** |
| 1 | Flashbang Explosives | **Flashbang Missile** | **Flashbang Minecart** |
| 1 | Recon Explosives | **Recon Missile** | **Recon Minecart** |
| 2 | Fragmentation Explosives | **Fragmentation Missile** | **Fragmentation Minecart** |
| 2 | Contagious Explosives | **Contagious Missile** | **Contagious Minecart** |
| 2 | Sonic Explosives | **Sonic Missile** | **Sonic Minecart** |
| 2 | Breaching Explosives | **Breaching Missile** | **Breaching Minecart** |
| 2 | Thermobaric Explosives | **Thermobaric Missile** | **Thermobaric Minecart** |
| 2 | Phantom Enemy Explosives | **Phantom Enemy Missile** | **Phantom Enemy Minecart** |
| 2 | Darkness Explosives | **Darkness Missile** | **Darkness Minecart** |
| 3 | Nuclear Explosives | **Nuclear Missile** | **Nuclear Minecart** |
| 3 | Exothermic Explosives | **Exothermic Missile** | **Exothermic Minecart** |
| 3 | Endothermic Explosives | **Endothermic Missile** | **Endothermic Minecart** |
| 3 | Anti-Gravitational Explosives | **Anti-Gravitational Missile** | **Anti-Gravitational Minecart** |
| 3 | Ender Explosives | **Ender Missile** | **Ender Minecart** |
| 4 | Antimatter Explosives | **Antimatter Missile** | **Antimatter Minecart** |
| 4 | Red Matter Explosives | **Red Matter Missile** | **Red Matter Minecart** |

### Tools

| Item | Method | Ingredients | Output |
|---|---|---|---|
| **Laser Range Finder** | Shaped crafting | `QEI`<br>`·BA`<br>`Q` Rose Quartz, `E` Electron Tube, `I` Iron Sheet, `B` Stone Button, `A` Andesite Alloy | 1× |
| **Laser Range Finder** | Create: Mechanical Crafter | `GES`<br>`·BA`<br>`G` Glass, `E` Electron Tube, `S` Iron Sheet, `B` Stone Button, `A` Andesite Alloy | 1× |
| **Remote Detonator** | Shaped crafting | `·L·`<br>`SES`<br>`SBS`<br>`L` Redstone Link, `S` Iron Sheet, `E` Electron Tube, `B` Wooden Button | 1× |
| **Remote Detonator** | Create: Mechanical Crafter | `STS`<br>`SPS`<br>`SBS`<br>`S` Iron Sheet, `T` Transmitter, `P` Precision Mechanism, `B` Wooden Button | 1× |
| **Laser Designator** | Shaped crafting | `RQ`<br>`EH`<br>`R` Laser Range Finder, `Q` Polished Rose Quartz, `E` Electron Tube, `H` Remote Detonator | 1× |
| **Laser Designator** | Create: Mechanical Crafter | `RQ·`<br>`·P·`<br>`··H`<br>`R` Laser Range Finder, `Q` Polished Rose Quartz, `P` Precision Mechanism, `H` Remote Detonator | 1× |
| **Rocket Launcher: Direct** | Shaped crafting | `BBB`<br>`CRE`<br>`·A·`<br>`B` Brass Sheet, `C` Cogwheel, `R` Laser Range Finder, `E` Electron Tube, `A` Andesite Alloy | 1× |
| **Rocket Launcher: Direct** | Create: Mechanical Crafter | `BBB`<br>`RET`<br>`A·A`<br>`B` Brass Sheet, `R` Laser Range Finder, `E` Electron Tube, `T` Cogwheel, `A` Andesite Alloy | 1× |
| **Rocket Launcher: Ballistic** | Shapeless crafting | Rocket Launcher: Direct, Launch Controller | 1× |
| **Rocket Launcher: Ballistic** | Create: Deployer | Rocket Launcher: Direct + Launch Controller | 1× |

### Materials

| Item | Method | Ingredients | Output |
|---|---|---|---|
| **Poison Powder** | Create: Millstone | Spider Eye | 2× Poison Powder, Poison Powder (50% chance) |
| **Poison Powder** | Create: Millstone | Poisonous Potato | Poison Powder, Poison Powder (25% chance) |
| **Poison Powder** | Shapeless crafting | Spider Eye, Poisonous Potato | 3× |
| **Antidote** | Create: Mixer | 3× Melon Seeds / Pumpkin Seeds / Wheat Seeds, Milk (250 mB) | 6× Antidote |
| **Antidote** | Shapeless crafting | Milk Bucket, 3× Melon Seeds / Pumpkin Seeds / Wheat Seeds | 6× |

## 📖 Ponder and JEI

- **Ponder scenes** (hold **W** over the item): Launch Pad, Launch Gantry, Launch Connector and Launch Controller share
  a launch scene, and the Cruise Turret and Radar Station each have their own. Explosive
  crates, missiles and bomb carts have an arming scene. The powered machines also appear under Create's *Kinetic
  Appliances* tag.
- **Engineer's Goggles** show each machine's charge and status: the pad's charge and missile, the turret's aim and the
  radar's status.
- **Tooltips:** hold **Shift** over any item for a Create-style summary. Machines also list their stress impact.
- **JEI** keeps every explosive, missile, bomb cart and loaded Delivery Missile as its own entry, and has info pages for
  the Missile Module, Delivery Missile and radioactive blocks. Turn the integration off with `disable` in
  `create_icbm-mods-jei.toml`.

## ⚙️ Configuration

Open **Mods → Create: ICBM → Config** in game, or edit the `create_icbm-*.toml` files in your `config` folder. They are
common configs, so on a server the server's files apply.

<details>
<summary><b>create_icbm-main.toml</b>: machines and protective gear</summary>

| Option | Default | What it changes |
|---|---|---|
| `use_energy` | `true` | Set to `false` to let the machines run without rotation. |
| `protective_armor.require_helmet` | `true` | Gas and radiation protection only counts with a helmet on. |
| `protective_armor.item_ratings` | `[]` | Protection ratings for armour from other mods, as `"mod:item=0.0-1.0"`. |
| `protective_armor.min_debilitation_gas_protection` | 0.1 | Rating needed against debilitation gas. |
| `protective_armor.min_chemical_gas_protection` | 0.5 | Rating needed against chemical gas. |
| `protective_armor.min_viral_gas_protection` | 0.8 | Rating needed against contagious gas. |
| `protective_armor.min_radiation_protection` | 1.0 | Rating needed against radiation. |

</details>

<details>
<summary><b>create_icbm-launcher.toml</b>: Launch Pad and Cruise Turret</summary>

| Option | Default | What it changes |
|---|---|---|
| `max_range` | 10000 | How far a Launch Pad can fire, in blocks along each axis. |
| `min_inaccuracy_range` | 2 | Landing spread in blocks at short range. |
| `scaled_inaccuracy_range` | 10 | Extra spread, reached at the maximum range. |
| `scaled_inaccuracy_per_launcher` | 1 | Extra spread for each extra pad in a salvo. |
| `launch_charge` | 1200 | Size of the launch charge. |
| `wind_up_seconds` | 4 | Seconds to wind up a launch at 64 RPM. |
| `launch_pad_stress` | 8 | Launch Pad stress impact in SU per RPM. |
| `cruise_turret_stress` | 6 | Cruise Turret stress impact in SU per RPM. |
| `cruise_turret_aim_rpm` | 32 | RPM at which the turret turns at its normal speed (up to 4 times faster above). |

</details>

<details>
<summary><b>create_icbm-radar.toml</b>: Radar Station</summary>

| Option | Default | What it changes |
|---|---|---|
| `max_range` | 500 | Largest detection range a radar can be set to. |
| `min_speed` | 16 | RPM the dish needs to work. |
| `stress_impact` | 4 | Stress impact in SU per RPM. |
| `scan_rate` | 5 | Ticks between scans. |
| `sam_delay` | 40 | Ticks between radio alerts. |

</details>

<details>
<summary><b>create_icbm-missile.toml</b>: missile flight</summary>

| Option | Default | What it changes |
|---|---|---|
| `speed_direct_fire` | 2 | Speed of turret and handheld missiles, in blocks per tick. |
| `cruise_fuel`, `handheld_fuel` | 200, 200 | Ticks of thrust for turret and handheld missiles. |
| `direct_fire_chunk_loading` | `true` | Turret and handheld missiles keep the chunks just ahead of them loaded (short tickets that time out on their own, like a thrown ender pearl), so a long shot doesn't freeze at the edge of the simulation distance. Tickets are renewed from the level tick, so a missile that already sits in a slow-loading chunk keeps its path loaded. |
| `arc_distance_limit` | 500 | Targets this far or further send the missile straight up and across the map out of sight. |
| `simulation_exit_height`, `simulation_enter_height` | 500, 450 | Heights where long-range missiles leave and re-enter the world. |
| `simulation_flight_speed` | 20 | Blocks per **second** a missile flies while it is simulated out of sight. 1,000 blocks take 50 seconds. |
| `simulation_enter_speed` | 1 | Blocks per tick it drops at when it re-enters the world above its target. |
| `keep_visible_in_flight` | `true` | A missile stays a real, visible entity while a player is within the server's view distance of it, and keeps its chunks ahead loaded. |
| `max_missile_chunk_tickets` | 512 | Most chunks all missiles of one dimension hold loaded ahead of them together. |
| `health_tier_1` to `health_tier_4` | 50, 80, 100, 200 | Missile health by explosive tier. |
| `impact_damage_scale`, `impact_damage_limit` | 20, -1 | Damage a missile does when it flies into a mob, and its cap (`-1` no cap, `0` off). |
| `SAM_MISSILE.target_range` | 30 | How far a SAM looks for targets. |
| `SAM_MISSILE.speed`, `SAM_MISSILE.fuel` | 4, 200 | SAM chase speed and ticks of fuel. |
| `SAM_MISSILE.attack_damage` | 100 | Damage a SAM does to its target. |
| `SAM_MISSILE.use_radar_only` | `false` | Only chase targets a radar has seen. |

</details>

<details>
<summary><b>create_icbm-blast.toml</b>: explosives</summary>

| Option | Default | What it changes |
|---|---|---|
| `blast_ms_per_tick`, `block_edit_ms_per_tick` | 15, 20 | Milliseconds per tick that big blasts may use. Lower is smoother for the server, higher finishes blasts sooner. |
| `adaptive_budget` | `true` | Shrinks both budgets by itself while the server is lagging and gives them back when it recovers. |
| `max_blast_edit_blocks_per_tick` | 20000 | Hard cap on blocks one paced blast (antimatter) removes per tick. |
| `protection_events` | `true` | Every blast asks claim and protection mods (FTB Chunks, Open Parties and Claims...) before it edits blocks, so claimed land is spared. |
| `block_effects_per_blast`, `block_effects_per_tick` | 64, 1024 | Limits on block particles and sounds. |
| `blast_do_block_updates` | `false` | Send block updates for every block a blast changes. |
| `allow_day_night_switch` | `true` | Exothermic and Endothermic change the time of day. |
| `FUSE_TIMES.EXPLOSIVES.*`, `FUSE_TIMES.BOMB_CARTS.*` | 100 (mostly) | Fuse of each crate and cart in ticks. |
| `condensed.energy_scale`, `condensed.damage` | 6, 10 | Condensed blast radius and damage. |
| `shrapnel.fragments`, `anvil.fragments`, `fragmentation.fragments` | 30, 25, 15 | Shards per side (the blast throws this number squared). |
| `debilitation.size`, `chemical.size`, `contagious.size` | 20 | Gas cloud radius. Each also has a `duration` (600 ticks). |
| `sonic.scale`, `thermobaric.scale`, `nuclear.scale` | 15, 30, 50 | Blast reach. |
| `nuclear.rotScale`, `nuclear.mutationScale` | 80, 80 | Fallout and mutation reach. |
| `exothermic.scale`, `endothermic.scale`, `antigravitational.scale` | 30 | Reach of these blasts. |
| `antimatter.size`, `antimatter.break_unbreakable` | 55, `true` | Antimatter radius, and whether it erases bedrock. |
| `antimatter.flash_extra_range`, `antimatter.flash_ticks` | 100, 60 | Blocks beyond the blast where the flash still reaches (0 = no flash), and ticks it stays fully white at the centre. |
| `antimatter.mushroom_cloud`, `antimatter.mushroom_cloud_range`, `antimatter.edit_ticks` | `true`, 512, 120 | The cloud, how far away it is seen, and the ticks the crater takes to open (0 = as fast as the budget allows). |
| `nuclear.mushroomCloud`, `nuclear.mushroomCloudScale`, `nuclear.mushroomCloudRange` | `true`, 0.7, 512 | The nuclear cloud, its size compared to antimatter (1.0) and how far away it is seen. |
| `ender.abduct_players` | `true` | Players are taken by the giant Enderman in a cutscene instead of being teleported instantly. |
| `ender.stare_ticks`, `ender.min_ticks_in_warp`, `ender.arrival_ticks` | 45, 45, 60 | Length of the three parts of the cutscene. |
| `ender.min_distance`, `ender.max_distance` | 60, 150 | How far the player can be taken, in blocks. |
| `ender.giant_scale`, `ender.cooldown_ticks` | 3.0, 200 | Size of the giant Enderman, and ticks before the same player can be taken again. |
| `redmatter.redmatter_max_size` | 70 | Largest size a red matter black hole grows to. |
| `flashbang.size`, `flashbang.duration` | 16, 20 | Flash reach and length in ticks. |
| `recon.size`, `recon.duration` | 32, 3600 | Recon reach and glow time in ticks. |
| `phantom.size`, `phantom.per_player`, `phantom.lifetime` | 24, 4, 1200 | Phantom reach, mobs per player and their lifetime in ticks. |
| `darkness.size`, `darkness.duration` | 24, 3600 | Darkness reach and length in ticks. |

</details>

<details>
<summary><b>Other files</b></summary>

| File | Option | Default | What it changes |
|---|---|---|---|
| `create_icbm-blocks.toml` | `radioactive.decay_block_chance` | 0.0001 | Chance per tick that a radioactive block decays. |
| | `radioactive.decay_effect_range`, `decay_effect_damage` | 5, 2 | Reach and damage of radioactive blocks. |
| `create_icbm-flying_blocks.toml` | `enable` | `true` | Blasts throw flying blocks. |
| | `block_ban_allow.list` | `["minecraft:fire"]` | Blocks that never fly. |
| `create_icbm-thread.toml` | `thread_count` | 1 (1 to 8) | Threads used to work out big blasts. |
| `create_icbm-mods-jei.toml` | `disable` | `false` | Turn the JEI integration off. |
| `create_icbm-mods-bluemap.toml` | see BlueMap | | |
| `create_icbm-client.toml` | `missile_engine_smoke`, `missile_launch_smoke` | `true` | Missile smoke effects. |
| | `mushroom_cloud_particles` | 90 | Most particles per tick one mushroom cloud may spawn on this client. 0 turns the cloud off. |

</details>

## ⌨️ Commands

All under `/icbm`, for operators (permission level 2).

| Command | Does |
|---|---|
| `/icbm help` | Lists the commands. |
| `/icbm remove <all\|missiles\|explosions> [radius]` | Removes missiles and/or running blasts around you, or in the whole dimension without a radius. |
| `/icbm lag [radius]` | Clears every running blast and Create: ICBM entity around you (default 1000 blocks). |
| `/icbm blast list` | Lists the explosive ids. |
| `/icbm blast trigger <explosive> <size>` | Sets off a blast where you stand, for testing. Add `<dimension> <x> <y> <z>` before the size to pick the spot. |
| `/icbm blast spread <count> <distance> <explosive> <dimension> <x> <y> <z> <size>` | Sets off a grid of blasts, for stress testing. |

## 📝 Changelog

### 1.0.35

**New**

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

**Removed**

- **Everything EMP:** the EMP explosive, missile and bomb cart, the EMP Tower and Coil Extender, their recipes, goggles,
  Ponder entries, textures and settings. Create is mechanical, so there is no electricity in this mod any more. Worlds
  that still contain those blocks or items lose them when loaded.
- Unused code: three dead classes, 68 unused methods, 81 unused imports, the unused Forge Energy plug-in and the
  unused energy data component, and two EMP goggle texts.

**Fixed**

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

## 🙏 Credits

**Create: ICBM is inspired by [ICBM-Classic](https://github.com/BuiltBrokenModding/ICBM-Classic)** by
[Built Broken Modding](https://github.com/BuiltBrokenModding), led by **Robin "DarkGuardsman" Seifert**
([@DarkGuardsman](https://github.com/DarkGuardsman)), and by the original **ICBM** mod by **Calclavia**
([@calclavia](https://github.com/calclavia)). Thank you for more than a decade of missiles. Much of the blast,
missile flight, radar and launcher code in this addon is built on ICBM-Classic's MIT-licensed source.

Thanks as well to everyone who worked on ICBM-Classic over the years: bl4ckscor3, GHXX, Dmod, kmecpp, Graugger, M1W3st,
Tgame14, LetterN, Vexatos and all its contributors.

Built for [Create](https://github.com/Creators-of-Create/Create) by simibubi and the Creators of Create. Create's
textures are not included in this mod; its models use them from your installed copy of Create.

Create: ICBM is an unofficial addon. It is not made by or affiliated with the ICBM-Classic or Create teams.

## 📜 Licence

Create: ICBM is © 2026 Navin Hariharan.

**Code and art:** [Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).
You may share and adapt it with credit, but not for commercial purposes. See [LICENSE](LICENSE).

**Sound effects:** Versions 1.0.1 and earlier contain sound effects sourced from ICBM-Classic; those sounds are not
covered by this project's licence and remain subject to their applicable original licensing terms. Beginning with
version 1.0.2, sound effects are covered by this project's CC BY-NC 4.0 licence.

The parts of the code based on ICBM-Classic remain under ICBM-Classic's MIT licence, whose notice is included in the
LICENSE file.
