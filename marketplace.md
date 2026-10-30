<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/banner.png" alt="Create: ICBM" width="100%">
</p>

<h3 align="center">Missiles, radar and launch networks, built the Create way.</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Version-1.0.4-8B5CF6?style=for-the-badge" alt="Version 1.0.4">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.219%2B-E2711D?style=for-the-badge" alt="NeoForge 21.1.219+">
  <img src="https://img.shields.io/badge/Create-6.0.10%2B-C9A45C?style=for-the-badge" alt="Create 6.0.10+">
  <img src="https://img.shields.io/badge/License-CC--BY--NC--4.0-3B82F6?style=for-the-badge" alt="CC BY-NC 4.0">
</p>

<p align="center">
  <a href="https://github.com/navin-hariharan/create-icbm#readme"><b>Full guide and every recipe on GitHub</b></a>
  &nbsp;&bull;&nbsp;
  <a href="https://github.com/navin-hariharan/create-icbm/issues"><b>Report a bug</b></a>
</p>

---

**Create: ICBM** is an addon for [Create](https://modrinth.com/mod/create) that brings **missiles, explosives, radar and launch networks** into your factory. Launch pads are wound up by shafts, missiles are put together on Mechanical Crafters and Deployers, explosives are mixed in basins over Blaze Burners, and every block looks like it came out of Create itself. Inspired by the classic **ICBM** mod.

> This page is the short version. The **[GitHub README](https://github.com/navin-hariharan/create-icbm#readme)** has the full guide: every machine, all 26 explosives in detail, every crafting recipe, all config options, commands and the changelog.

> **AI usage notice:** AI assistance was used in making Create: ICBM. Every feature is directed, reviewed and tested in game by the author.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines.png" alt="The machines of Create: ICBM" width="100%">
</p>

## ✨ Features

- **Rotation-powered machines.** Launch Pads, Cruise Turrets and both radars take a shaft and draw stress like any Create machine. No batteries, no electricity.
- **26 explosives in 4 tiers**, from a beefed-up TNT to antimatter and a red matter black hole. Each is a crate, a missile warhead and a bomb cart.
- **Blasts you see and hear from far away:** mushroom clouds for nuclear and antimatter, a blinding antimatter flash, and explosion sounds with the delay of distance for everyone within view distance.
- **Ender missile abduction:** a giant Enderman drags players through a purple warp tunnel to a spot far away.
- **Long-range missiles** that fly a ballistic arc up to 10,000 blocks, stay visible and fly smoothly all the way to the ground.
- **A real launch network:** chain pads, gantries and connectors to one Launch Controller and fire whole salvos.
- **Air defence:** a Radar Station spots incoming missiles and Cruise Turrets shoot them down with Surface to Air Missiles.
- **Strike back:** the new **Advanced Radar** sends your Launch Controllers and Cruise Turrets to the launch site of every incoming missile.
- **Delivery Missiles** drop a stack of items into a chest across the map, or leave a **Mail** parcel with the sender's name.
- **Made with Create:** every recipe uses only Create and vanilla items, on Create's machines or at a plain crafting table. Ponder scenes, goggles, tooltips and JEI included.
- **Built for big servers:** paced and adaptive blast budgets, a worker thread for blast maths, capped chunk loading, smooth client-side missile flight.

## 📥 Installation

| Needs | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 or newer |
| [Create](https://modrinth.com/mod/create) | 6.0.10 or newer |
| [JEI](https://modrinth.com/mod/jei), [BlueMap](https://modrinth.com/plugin/bluemap), Sable, Mekanism, ServerCore, Immersive Optimization, Tom's Simple Storage | optional |

Drop the jar into `mods` next to Create. It is needed on the server and on every client.

## 🚀 Your first launch

1. **Place a Launch Pad** and give it rotation through a shaft from below (8 SU per RPM; about 4 seconds to wind up at 64 RPM).
2. **Build the network:** a **Launch Gantry** beside the pad and a **Launch Controller** touching it. **Launch Connectors** work like cables.
3. **Make a missile:** Missile Casing, then Missile Module, then deploy an explosive crate onto it.
4. **Load it** by hand, or with a hopper, funnel or chute.
5. **Pick a target** with a **Laser Range Finder**, or type coordinates into the controller.
6. **Fire** from the controller, with redstone, or with a **Remote Detonator** or **Laser Designator** on the same radio channel.

## 🏭 Machines

| Machine | Power | In short |
|---|---|---|
| Launch Pad | 8 SU/RPM | Holds a missile and launches it in a ballistic arc, up to 10,000 blocks. |
| Launch Gantry / Connector | none | Link pads, controllers and storage into one launch network. |
| Launch Controller | none | Target, radio channel and the *Launch* button for a whole network. |
| Cruise Turret | 6 SU/RPM | Aims and fires a missile in a straight line, also fires Surface to Air Missiles. |
| Radar Station | 4 SU/RPM at 100 m, 16 RPM+ | Tracks incoming missiles up to 500 blocks and alerts turrets and controllers. Longer range, more stress. |
| Advanced Radar | Same as Radar Station | Sends launchers on its channel to the launch site of incoming missiles. |

## 🚀 Missiles

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles.png" alt="Every missile" width="100%">
</p>

- **Missile Module:** the motor and casing, the base of every missile.
- **Explosive missiles:** a module armed with any of the 26 explosives. Health 50 (tier 1) up to 200 (tier 4).
- **Surface to Air Missile:** hunts other missiles; a hit shoots one down without setting off its warhead.
- **Delivery Missile:** carries one stack of items to a container, or leaves a Mail parcel.

## 💣 Explosives

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives.png" alt="All 26 explosives" width="100%">
</p>

Tier 1 mixes in a plain Basin, tier 2 over a Blaze Burner, tiers 3 and 4 over a superheated one. Each is made from a lower explosive.

| Tier | Explosives |
|---|---|
| 1 | **Condensed** (radius 6), **Shrapnel** (900 shards), **Incendiary**, **Debilitation** and **Chemical** gas, **Anvil**, **Repulsive**, **Attractive**, **Colorful**, **Smoke Marking**, **Flashbang**, **Recon** |
| 2 | **Fragmentation**, **Contagious** gas, **Sonic**, **Breaching**, **Thermobaric**, **Phantom Enemy**, **Darkness** |
| 3 | **Nuclear** (levels 35 blocks, lethal 50, fallout 80), **Exothermic** (Nether land), **Endothermic** (frozen land), **Anti-Gravitational**, **Ender** (vortex and abduction) |
| 4 | **Antimatter** (erases 55 blocks, lethal 110, bedrock too), **Red Matter** (a growing black hole) |

Every explosive also makes a **bomb cart** (powered activator rail lights it) and a missile warhead.

## 🧰 Tools

- **Laser Range Finder:** reads a block's coordinates and hands them to a launcher.
- **Remote Detonator:** fires launchers and wired crates on its radio channel, at any distance.
- **Laser Designator:** paints a block and calls a strike on it.
- **Rocket Launcher: Direct / Ballistic:** shoulder launchers, straight line or high arc.

## 🛡️ Air defence

Spin a **Radar Station** at 16 RPM or more, set its range and radio channel, then put **Cruise Turrets** on the same channel loaded with Surface to Air Missiles. Incoming missiles trigger the turrets and the radar's redstone output.

To hit back, put an **Advanced Radar** on a second channel with a Launch Controller and armed pads, or Cruise Turrets loaded with missiles. When a missile comes in, they fire at the place it was launched from. A strike only counts once a missile really fires or is queued; otherwise the radar retries every second.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/advanced-radar-counterstrike.png" alt="Advanced Radar counter-strike" width="100%">
</p>

## 🎬 Effects and sounds

- **Mushroom clouds** for Nuclear (70 % size) and Antimatter, drawn by each client from one packet, with a configurable particle budget.
- **Antimatter flash** blinds everyone in the blast and up to 100 blocks beyond.
- **Sounds:** full-volume nuclear blast within 50 blocks, antimatter within 100, and a distant explosion for anyone within view distance.
- **Ender cutscene:** stare, purple warp tunnel, arrival. Configurable or off with `blast.ender.abduct_players`.

## 🖥️ Server performance

Paced block edits with a time budget that shrinks while the server lags, a blast worker thread (1 to 8), a cap of 512 missile chunk tickets per dimension, half the position updates with client-side smoothing, capped debris effects and clean-up on unload. Ops get `/icbm lag` and `/icbm remove`. Details in the [README](https://github.com/navin-hariharan/create-icbm#server-performance).

## 🔌 Compatibility

**JEI** (all recipes), **BlueMap** (missiles, impact clouds and Mail on a map layer), **Sable** (hit and fire from airships), **Mekanism** (Teleporter portals), **ServerCore** and **Immersive Optimization** (missiles keep flying far from players), **Tom's Simple Storage** (auto-stock launchers), claim mods like FTB Chunks (claimed land is spared). Everything optional is only touched when installed. See the [README](https://github.com/navin-hariharan/create-icbm#compatibility).

## 🔨 Crafting recipes

Almost everything can be made two ways: on Create's machines (Mechanical Crafting, Mixing, Deploying, Milling) or at a crafting table. Missiles and bomb carts are an explosive crate plus a Missile Module or a Minecart, on a Deployer or in the crafting grid.

**All recipes are listed in the [README crafting section](https://github.com/navin-hariharan/create-icbm#crafting-recipes)**, and JEI shows them in game.

## ⚙️ Configuration and commands

Config files are `create_icbm-*.toml` in the `config` folder (main, launcher, radar, missile, blast, client, thread, jei, bluemap). Commands are under `/icbm` (operators): `help`, `remove`, `lag`, `blast list`, `blast trigger`, `blast spread`. Every option is in the [README](https://github.com/navin-hariharan/create-icbm#configuration).

## 📝 Latest: 1.0.4

- **New: Advanced Radar**, counter-strikes at the launch site of incoming missiles, and a new circular radar scope.
- **Fixed counter-strikes:** unanswered strikes retry every second instead of waiting the full cooldown, delayed and aiming launches count as fired, Cruise Turrets turn down orders they can't carry out, and a cancelled target no longer fires at the old one.
- **Fixed radio:** Launch Controllers reconnect to their network and radio after loading, and removed blocks no longer answer twice.
- **Optimizer compatibility** now exempts every Create: ICBM entity, including surface-to-air missiles, blast controllers, missile seats, decoys and flying blocks. Diagnostics are opt-in. Full [changelog](https://github.com/navin-hariharan/create-icbm#changelog).

## 🙏 Credits

Inspired by [ICBM-Classic](https://github.com/BuiltBrokenModding/ICBM-Classic) by Built Broken Modding, led by Robin "DarkGuardsman" Seifert, and the original ICBM mod by Calclavia. Built for [Create](https://github.com/Creators-of-Create/Create) by simibubi and the Creators of Create. An unofficial addon, not affiliated with the ICBM-Classic or Create teams.

## 📜 Licence

© 2026 Navin Hariharan. Code and art: [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/). Sounds in 1.0.1 and earlier came from ICBM-Classic and keep their original licensing; from 1.0.2 they are CC BY-NC 4.0. Parts based on ICBM-Classic remain under its MIT licence (see LICENSE).
