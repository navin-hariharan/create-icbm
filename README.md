<p align="center">
  <img src="doc/images/banner.png" alt="Create: ICBM" width="100%">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.219%2B-E2711D?style=for-the-badge" alt="NeoForge 21.1.219+">
  <img src="https://img.shields.io/badge/Create-6.0.10%2B-C9A45C?style=for-the-badge" alt="Create 6.0.10+">
  <img src="https://img.shields.io/badge/Release-V1.0.0-22C55E?style=for-the-badge" alt="Release V1.0.0">
  <img src="https://img.shields.io/badge/License-CC_BY--NC_4.0-3B82F6?style=for-the-badge" alt="CC BY-NC 4.0 licence">
</p>

**Create: ICBM** is an addon for [Create](https://github.com/Creators-of-Create/Create) that brings missiles,
explosives, radar and EMP towers into your factory. Launch pads are wound up by shafts, missiles are put together on
Mechanical Crafters and Deployers, explosives are mixed in basins over Blaze Burners, and every block looks like it came
out of Create itself.

It is a new addon, inspired by the classic **ICBM** mod (see [Credits](#credits)).

---

## Contents

- [Features](#features)
- [Installation](#installation)
- [Your first launch](#your-first-launch)
- [Machines](#machines)
- [Missiles](#missiles)
- [Explosives](#explosives)
- [Bomb carts](#bomb-carts)
- [Tools](#tools)
- [Air defence](#air-defence)
- [Storage and automation](#storage-and-automation)
- [Crafting recipes](#crafting-recipes)
- [Configuration and commands](#configuration-and-commands)
- [Building from source](#building-from-source)
- [Credits](#credits)

---

## Features

- **Rotation-powered machines.** Launch Pads, Cruise Turrets, Radar Stations and EMP Towers take a shaft and draw
  stress like any Create machine. No batteries, no energy cables.
- **23 explosives in 4 tiers**, from a beefed-up TNT to antimatter and a red matter black hole. Each one can be placed
  as a crate, fired as a missile or rolled in as a bomb cart.
- **Long-range missiles** that fly a ballistic arc up to 10,000 blocks and keep flying through unloaded chunks.
- **A real launch network:** chain pads, gantries and connectors to one Launch Controller and fire whole salvos.
- **Air defence:** a spinning Radar Station spots incoming missiles and tells Cruise Turrets loaded with
  Surface to Air Missiles to shoot them down.
- **Delivery Missiles** that drop a stack of items straight into a chest on the other side of the map.
- **Made with Create:** every recipe uses only Create and vanilla items, on Create's machines or at a plain crafting
  table. Ponder scenes, goggle overlays, "Hold [Shift] for Summary" tooltips and JEI support included.
- **Plays well with storage mods:** hoppers, funnels, chutes and Tom's Simple Storage can stock your launchers.
- **Server friendly:** big blasts are spread over several ticks with a time budget, so a salvo doesn't freeze the
  server.

<p align="center">
  <img src="doc/images/machines.png" alt="The machines of Create: ICBM" width="100%">
</p>

## Installation

| Needs | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 or newer |
| [Create](https://modrinth.com/mod/create) | 6.0.10 or newer (brings Ponder and Flywheel with it) |
| [JEI](https://modrinth.com/mod/jei) | optional, 19.x, shows every recipe |
| [Tom's Simple Storage](https://modrinth.com/mod/toms-storage) | optional, for storage automation |

Drop the `create-icbm` jar into your `mods` folder next to Create.

## Your first launch

<p align="center">
  <img src="doc/images/launch-site.png" alt="A launch site: Launch Pad with a missile, Launch Gantry, Launch Controller and a shaft" width="85%">
</p>

1. **Place a Launch Pad** and give it rotation through a shaft on the side facing away from the missile. The pad
   draws 8 SU per RPM. At 64 RPM it winds up a full launch charge in about 4 seconds, and it only fires when fully
   wound.
2. **Build the launch network.** Put **Launch Gantries** next to the pad and a **Launch Controller** touching the
   gantries. Use **Launch Connectors** like cables to reach pads further away. Everything that touches forms one
   network, and the controller fires every pad in it.
3. **Make a missile.** Craft a Missile Casing, turn it into a Missile Module on a Mechanical Crafter, then arm it by
   deploying an explosive crate onto it with a Deployer (or combine both at a crafting table).
4. **Load it.** Right-click the pad with the missile, or feed it in with a hopper, funnel or chute.
5. **Pick a target.** Aim a **Laser Range Finder** at a block, then right-click the controller with it, or type the
   coordinates into the controller.
6. **Fire!** Press *Launch* in the controller, give the controller a redstone signal, or use a **Remote Detonator**
   or **Laser Designator** on the controller's radio channel.

Each pad has its own settings: the height the missile climbs before it turns, a group number (the controller fires
one group at a time) and a firing delay for staggered salvos. Missiles land within a few blocks of the target: about
2 blocks at short range, up to 12 at the longest range, plus a block for every extra pad in the same salvo.

> Feeling brave? Right-click a missile on a pad to sit on it. You go along for the ride when it launches.

## Machines

| Machine | Power | What it does |
|---|---|---|
| **Launch Pad** | 8 SU/RPM, shaft on the side away from the missile | Holds a missile upright and launches it in a ballistic arc. A launch needs a full charge (about 4 s at 64 RPM). |
| **Launch Gantry** | none | Stands next to the missile and carries the launch network between pads and the controller. |
| **Launch Connector** | none | A flat cable that carries the launch network along the floor. |
| **Launch Controller** | none | Target, group and radio channel for every pad in its network, plus the *Launch* button. Fires on redstone too. |
| **Cruise Turret** | 6 SU/RPM, shaft below | A rotating turret that aims a missile and fires it straight at the target. It turns at its normal speed at 32 RPM, slower below that, up to 4x faster above. |
| **Radar Station** | 4 SU/RPM, shaft below, at least 16 RPM | Spins its dish and tracks missiles up to 500 blocks away. Raises a redstone signal and a radio alert when one heads into its trigger range. |
| **EMP Tower** | 16 SU/RPM, shaft below | Charges a coil, then releases an electromagnetic pulse that drains energy from machines and can knock missiles out of the sky. 150 block maximum radius, plus 20 blocks for each **EMP Coil Extender** stacked on top. Fires from its screen, on redstone or by radio. |

Engineer's Goggles show each machine's speed, stress and charge. The Launch Pad, Cruise Turret, Radar Station and EMP
Tower have Ponder scenes: hold **W** over one in your inventory.

## Missiles

<p align="center">
  <img src="doc/images/missiles.png" alt="Every missile in Create: ICBM" width="100%">
</p>

- **Missile Casing**: a pressed sheet-metal rocket body.
- **Missile Module**: a casing fitted with an Electron Tube and a Blaze Powder motor. It flies, but carries only a
  small charge.
- **Explosive Missile**: a Missile Module armed with any of the 23 explosives. The warhead goes off on impact.
- **Surface to Air Missile**: hunts flying targets, other missiles included. Fire it from a Cruise Turret or a handheld
  launcher (see [Air defence](#air-defence)).
- **Delivery Missile**: carries one stack of anything a chest slot can hold, shulker boxes included. On impact it puts
  the stack into the chest, barrel or Create vault it hits, or drops it where it lands. It does no damage.

<p align="center">
  <img src="doc/images/delivery-menu.png" alt="The Delivery Missile cargo slot" width="45%">
</p>

To load a Delivery Missile, **shift + right-click** it in your hand to open its cargo slot, or right-click a stack onto
it in your inventory, like a bundle.

Missiles can be shot down by SAMs, EMP blasts or enough damage. A missile brought down by fire or an explosion sets off
its warhead where it falls.

## Explosives

<p align="center">
  <img src="doc/images/explosives.png" alt="All 23 explosive crates" width="100%">
</p>

Every explosive comes as a **crate** you can place and light (redstone, fire or a Remote Detonator set it off), as a
**missile** warhead and as a **bomb cart**. Higher tiers need hotter mixing: tier 1 mixes in a plain basin, tier 2
over a heated Blaze Burner, tiers 3 and 4 over a superheated one.

| Explosive | Tier | Effect |
|---|---|---|
| Condensed | 1 | A stronger TNT blast that reaches about half again as far as TNT. |
| Shrapnel | 1 | Sprays hundreds of burning metal shards. They set targets alight but leave blocks untouched. |
| Incendiary | 1 | Scatters fire over a wide area and clears away ice. Breaks no blocks. |
| Debilitation | 1 | A white gas cloud that lingers for 30 seconds. Walls keep it out. |
| Chemical | 1 | A yellow toxic cloud that lingers for 30 seconds. Walls keep it out. |
| Anvil | 1 | Hurls hundreds of heavy anvil chunks, each about three times harder than shrapnel. |
| Repulsive | 1 | Throws back every mob, player and item within 12 blocks. |
| Attractive | 1 | Pulls in every mob, player and item within 12 blocks. |
| Colorful | 1 | A harmless rainbow gas cloud. |
| Smoke Marking | 1 | A column of smoke for 5 seconds, handy for marking a target or testing your aim. |
| Fragmentation | 2 | Over 200 explosive fragments that blow up where they land. |
| Contagious | 2 | A green viral cloud that lingers for 30 seconds. |
| Sonic | 2 | A shockwave out to about 15 blocks that rips up blocks and flings mobs into the air. |
| Breaching | 2 | Punches a 3x3 tunnel up to 7 blocks deep, even through obsidian. |
| Thermobaric | 2 | Wipes out terrain within about 30 blocks and kills anything in the open within 50. |
| Nuclear | 3 | Wipes out terrain within about 50 blocks and leaves radioactive fallout behind. |
| EMP | 3 | Drains stored energy from machines and carried items within 50 blocks. Breaks nothing. |
| Exothermic | 3 | Turns the land within about 30 blocks into netherrack, magma and lava. |
| Endothermic | 3 | Freezes the land within about 30 blocks: ice and snow, lava to obsidian. |
| Anti-Gravitational | 3 | For 2 minutes, lifts blocks, mobs and players within about 30 blocks into the sky. |
| Ender | 3 | Opens an ender vortex for 8 seconds that drags things in and teleports them. Right-click the crate with a Laser Range Finder to pick where they land. |
| Antimatter | 4 | Erases everything within 55 blocks, even bedrock. |
| Red Matter | 4 | Spawns a black hole that devours blocks and grows as it feeds. |

Gas, radiation and contagion are cured by drinking an **Antidote**. **Radioactive Dirt** and **Radioactive Stone** are
left behind by nuclear blasts and irradiate whoever stands on them; over time they decay back to normal blocks.

## Bomb carts

<p align="center">
  <img src="doc/images/bomb-carts.png" alt="Bomb carts" width="85%">
</p>

A minecart with an explosive crate in it, one for every explosive. Run it over a powered **activator rail** to light
the fuse. A cart that is broken while moving, burning or caught in a blast goes off at once.

## Tools

<p align="center">
  <img src="doc/images/tools.png" alt="Handheld tools" width="100%">
</p>

| Tool | Use |
|---|---|
| **Laser Range Finder** | Right-click to measure the coordinates of the block you aim at. Right-click a Launch Controller or Cruise Turret to hand the target over. |
| **Remote Detonator** | Sends a radio signal on its channel: it sets off explosives and launches missiles on the same channel. |
| **Laser Designator** | Paints the block you aim at and radios it to every launcher on its channel, which fire at it. |
| **Rocket Launcher: Direct** | A shoulder launcher that fires a missile from your inventory in a straight line. |
| **Rocket Launcher: Ballistic** | Lobs a missile in a high arc onto where you aim. |

## Air defence

1. Place a **Radar Station** and spin it at 16 RPM or more. Open it to set its detection range, trigger range and a
   radio channel.
2. Place one or more **Cruise Turrets** with a shaft below and load each with a **Surface to Air Missile**. Set them to
   the radar's radio channel.
3. When a missile heads into the trigger range, the radar raises the alarm and the turrets swing round and fire their
   SAMs at it. Turn on the radar's redstone output to sound sirens or close doors at the same time.

An **EMP Tower** wired to the radar's redstone output is the last line of defence: its pulse can knock missiles in
range out of the sky.

## Storage and automation

- Launch Pads and Cruise Turrets take missiles from hoppers, Create funnels and chutes. A Launch Controller or Launch
  Gantry accepts missiles for every pad in its network.
- **Tom's Simple Storage autostock:** put a **Basic Inventory Hopper** with its back on your storage network (a cable or
  connector), a missile in its filter slot and its front against the **Launch Controller**. It refills every empty pad
  in that launch network from storage. Gantries and controllers are hidden from Tom's Inventory Connector so missiles
  aren't counted twice; connect the pads themselves to see their missiles in the terminal.

## Crafting recipes

Every recipe uses only Create and vanilla items. Almost everything can be made two ways: on Create's machines
(Mechanical Crafting, Mixing, Deploying, Milling) or at a plain crafting table. Both give the same item. JEI shows all
of them, including Create's own recipe categories.

### Machines

<p align="center">
  <img src="doc/images/recipes/launch-pad.png" alt="Launch Pad recipe" width="80%"><br>
  <img src="doc/images/recipes/launch-controller.png" alt="Launch Controller recipe" width="80%"><br>
  <img src="doc/images/recipes/launch-gantry.png" alt="Launch Gantry recipe" width="80%"><br>
  <img src="doc/images/recipes/launcher-connector.png" alt="Launch Connector recipe" width="80%"><br>
  <img src="doc/images/recipes/cruise-turret.png" alt="Cruise Turret recipe" width="80%"><br>
  <img src="doc/images/recipes/radar-station.png" alt="Radar Station recipe" width="80%"><br>
  <img src="doc/images/recipes/emp-tower.png" alt="EMP Tower recipe" width="80%"><br>
  <img src="doc/images/recipes/emp-tower-extender.png" alt="EMP Coil Extender recipe" width="80%">
</p>

### Missiles

<p align="center">
  <img src="doc/images/recipes/missile-casing.png" alt="Missile Casing recipe" width="80%"><br>
  <img src="doc/images/recipes/missile-module.png" alt="Missile Module recipe" width="80%"><br>
  <img src="doc/images/recipes/explosive-missile.png" alt="Explosive Missile recipe" width="80%"><br>
  <img src="doc/images/recipes/surface-to-air-missile.png" alt="Surface to Air Missile recipe" width="80%"><br>
  <img src="doc/images/recipes/delivery-missile.png" alt="Delivery Missile recipe" width="80%">
</p>

### Bomb carts

<p align="center">
  <img src="doc/images/recipes/bomb-cart.png" alt="Bomb Cart recipe" width="80%">
</p>

A plain TNT block still makes the vanilla TNT Minecart.

### Tools

<p align="center">
  <img src="doc/images/recipes/laser-range-finder.png" alt="Laser Range Finder recipe" width="80%"><br>
  <img src="doc/images/recipes/remote-detonator.png" alt="Remote Detonator recipe" width="80%"><br>
  <img src="doc/images/recipes/laser-designator.png" alt="Laser Designator recipe" width="80%"><br>
  <img src="doc/images/recipes/rocket-launcher.png" alt="Rocket Launcher: Direct recipe" width="80%"><br>
  <img src="doc/images/recipes/ballistic-rocket-launcher.png" alt="Rocket Launcher: Ballistic recipe" width="80%">
</p>

### Materials

<p align="center">
  <img src="doc/images/recipes/poison-powder.png" alt="Poison Powder recipe" width="80%"><br>
  <img src="doc/images/recipes/antidote.png" alt="Antidote recipe" width="80%">
</p>

### Explosives

Each explosive is mixed from a lower one, so you work your way up from Condensed Explosives.

<p align="center">
  <img src="doc/images/recipes/explosives-overview.png" alt="How the explosives are made from each other" width="100%">
</p>

<details>
<summary><b>Tier 1</b>: Condensed, Shrapnel, Incendiary, Debilitation, Chemical, Anvil, Repulsive, Attractive, Colorful, Smoke Marking</summary>
<p align="center">
  <img src="doc/images/recipes/condensed-explosives.png" alt="Condensed Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/shrapnel-explosives.png" alt="Shrapnel Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/incendiary-explosives.png" alt="Incendiary Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/debilitation-explosives.png" alt="Debilitation Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/chemical-explosives.png" alt="Chemical Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/anvil-explosives.png" alt="Anvil Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/repulsive-explosives.png" alt="Repulsive Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/attractive-explosives.png" alt="Attractive Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/colorful-explosives.png" alt="Colorful Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/smoke-marking-explosives.png" alt="Smoke Marking Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>Tier 2</b>: Fragmentation, Contagious, Sonic, Breaching, Thermobaric</summary>
<p align="center">
  <img src="doc/images/recipes/fragmentation-explosives.png" alt="Fragmentation Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/contagious-explosives.png" alt="Contagious Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/sonic-explosives.png" alt="Sonic Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/breaching-explosives.png" alt="Breaching Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/thermobaric-explosives.png" alt="Thermobaric Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>Tier 3</b>: Nuclear, EMP, Exothermic, Endothermic, Anti-Gravitational, Ender</summary>
<p align="center">
  <img src="doc/images/recipes/nuclear-explosives.png" alt="Nuclear Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/emp-explosives.png" alt="EMP Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/exothermic-explosives.png" alt="Exothermic Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/endothermic-explosives.png" alt="Endothermic Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/anti-gravitational-explosives.png" alt="Anti-Gravitational Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/ender-explosives.png" alt="Ender Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>Tier 4</b>: Antimatter, Red Matter</summary>
<p align="center">
  <img src="doc/images/recipes/antimatter-explosives.png" alt="Antimatter Explosives recipe" width="80%"><br>
  <img src="doc/images/recipes/red-matter-explosives.png" alt="Red Matter Explosives recipe" width="80%">
</p>
</details>

## Configuration and commands

Open **Mods → Create: ICBM → Config** in game, or edit the `create_icbm-*.toml` files. The most useful options:

| Option | Default | What it changes |
|---|---|---|
| `use_energy` | `true` | Set to `false` to let the machines run without rotation. |
| `launch_pad_stress`, `cruise_turret_stress` | 8, 6 | Stress impact in SU per RPM. |
| `wind_up_seconds` | 4 | Seconds to wind up a launch at 64 RPM. |
| `max_range` (launcher) | 10000 | How far a Launch Pad can fire, in blocks. |
| `max_range` (radar), `min_speed` | 500, 16 | Radar detection range and the RPM the dish needs. |
| `base_max_range`, `extender_range_bonus` | 150, 20 | EMP Tower radius and the bonus per extender. |
| `blast_ms_per_tick`, `block_edit_ms_per_tick` | 15, 20 | Milliseconds per tick that big blasts may use. Lower is smoother for the server, higher finishes blasts sooner. |

Operator commands, all under `/icbm`:

| Command | Does |
|---|---|
| `/icbm help` | Lists the commands. |
| `/icbm remove <all\|missile\|explosion> [range]` | Removes missiles and/or running blasts around you. |
| `/icbm lag [range]` | Clears every running blast and ICBM entity around you. |
| `/icbm blast list` | Lists the explosive ids. |
| `/icbm blast trigger <explosive> ...` | Sets off a blast at a position, for testing. |
| `/icbm blast spread ...` | Sets off a grid of blasts, for stress testing. |

## Building from source

You need JDK 21.

```sh
./gradlew build
```

The jar lands in `build/libs/`. On Windows use `gradlew.bat build`, or `bash ./gradlew build` inside WSL.

## Credits

**Create: ICBM is inspired by [ICBM-Classic](https://github.com/BuiltBrokenModding/ICBM-Classic)** by
[Built Broken Modding](https://github.com/BuiltBrokenModding), led by **Robin "DarkGuardsman" Seifert**
([@DarkGuardsman](https://github.com/DarkGuardsman)), and by the original **ICBM** mod by **Calclavia**
([@calclavia](https://github.com/calclavia)). Thank you for more than a decade of missiles. Much of the blast,
missile flight, radar and launcher code in this addon is built on ICBM-Classic's MIT-licensed source, and the sound
effects come from ICBM-Classic.

Thanks as well to everyone who worked on ICBM-Classic over the years: bl4ckscor3, GHXX, Dmod, kmecpp, Graugger, M1W3st,
Tgame14, LetterN, Vexatos and all its contributors.

Built for [Create](https://github.com/Creators-of-Create/Create) by simibubi and the Creators of Create. Create's
textures are not included in this mod; its models use them from your installed copy of Create.

Create: ICBM is an unofficial addon. It is not made by or affiliated with the ICBM-Classic or Create teams.

## Licence

Create: ICBM is © 2026 Navin Hariharan and licensed under
[Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).
You may share and adapt it with credit, but not for commercial purposes. See [LICENSE](LICENSE).

The parts of the code based on ICBM-Classic keep Built Broken Modding's MIT notice, which is included in the LICENSE
file. The sound effects come from ICBM-Classic and are not covered by this licence.
