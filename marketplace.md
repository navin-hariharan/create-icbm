<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/banner.png" alt="Create: ICBM" width="100%">
</p>

<h3 align="center">Missiles, radar and EMP towers, built the Create way.</h3>

<p align="center">
  <img src="https://img.shields.io/badge/Version-1.0.2-8B5CF6?style=for-the-badge" alt="Version 1.0.2">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=for-the-badge" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/NeoForge-21.1.219%2B-E2711D?style=for-the-badge" alt="NeoForge 21.1.219+">
  <img src="https://img.shields.io/badge/Create-6.0.10%2B-C9A45C?style=for-the-badge" alt="Create 6.0.10+">
  <img src="https://img.shields.io/badge/License-CC--BY--NC--4.0-3B82F6?style=for-the-badge" alt="CC BY-NC 4.0">
</p>

<p align="center">
  <a href="https://github.com/navin-hariharan/create-icbm"><b>Source on GitHub</b></a>
  &nbsp;•&nbsp;
  <a href="https://github.com/navin-hariharan/create-icbm/issues"><b>Report a bug</b></a>
</p>

---

**Create: ICBM** is an addon for [Create](https://modrinth.com/mod/create) that brings **missiles, explosives, radar and EMP towers** into your factory. Launch pads are wound up by shafts, missiles are assembled on Mechanical Crafters and Deployers, explosives are mixed in basins over Blaze Burners, and every block looks like it came straight out of Create itself.

A brand new addon, inspired by the classic **ICBM** mod (see [Credits](#-credits)).

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/machines.png" alt="The machines of Create: ICBM" width="100%">
</p>

---

## ✨ Features

- ⚙️ **Rotation-powered machines.** Launch Pads, Cruise Turrets, Radar Stations and EMP Towers take a shaft and draw stress like any Create machine. No batteries, no energy cables.
- 💥 **27 explosives in 4 tiers**, from a beefed-up TNT to antimatter and a red matter black hole. Each one can be placed as a crate, fired as a missile or rolled in as a bomb cart.
- 🎭 **Tricks as well as bangs.** A blinding Flashbang, a Recon pulse that outlines enemies through walls, Phantom Enemies that only pretend to attack, and a cloud of Darkness. None of them break blocks.
- 🚀 **Long-range missiles** that fly a ballistic arc up to **10,000 blocks** and keep flying through unloaded chunks.
- 🛰️ **A real launch network.** Chain pads, gantries and connectors to one Launch Controller and fire whole salvos.
- 🛡️ **Air defence.** A spinning Radar Station spots incoming missiles and tells Cruise Turrets loaded with Surface to Air Missiles to shoot them down.
- 📦 **Delivery Missiles** that drop a stack of items straight into a chest on the other side of the map, or leave it in a **Mail** parcel with the sender's name on it.
- 🔧 **Made with Create.** Every recipe uses only Create and vanilla items. Ponder scenes, goggle overlays, "Hold [Shift] for Summary" tooltips and JEI support included.
- 🗄️ **Plays well with storage mods.** Hoppers, funnels, chutes and Tom's Simple Storage can stock your launchers.
- 🗺️ **Live on BlueMap.** With [BlueMap](https://modrinth.com/plugin/bluemap) installed, flying missiles move across its web map. Click one to see who launched it, and every impact leaves a mushroom cloud on the map for a minute.
- 🖥️ **Server friendly.** Big blasts are spread over several ticks with a time budget, so a salvo doesn't freeze the server.

---

## 📥 Requirements

| Needs | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219 or newer |
| [Create](https://modrinth.com/mod/create) | 6.0.10 or newer (brings Ponder and Flywheel with it) |
| [JEI](https://modrinth.com/mod/jei) | *Optional.* 19.x, shows every recipe |
| [Tom's Simple Storage](https://modrinth.com/mod/toms-storage) | *Optional.* For storage automation |
| [BlueMap](https://modrinth.com/plugin/bluemap) | *Optional.* 5.x, shows missiles on its web map |

Drop the `create-icbm` jar into your `mods` folder next to Create.

---

## 🚀 Your First Launch

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/launch-site.png" alt="A launch site: Launch Pad with a missile, Launch Gantry, Launch Controller and a shaft" width="85%">
</p>

1. **Place a Launch Pad** and give it rotation through a shaft on the side facing away from the missile. The pad draws 8 SU per RPM. At 64 RPM it winds up a full launch charge in about 4 seconds, and it only fires when fully wound.
2. **Build the launch network.** Put **Launch Gantries** next to the pad and a **Launch Controller** touching the gantries. Use **Launch Connectors** like cables to reach pads further away. Everything that touches forms one network, and the controller fires every pad in it.
3. **Make a missile.** Craft a Missile Casing, turn it into a Missile Module on a Mechanical Crafter, then arm it by deploying an explosive crate onto it with a Deployer (or combine both at a crafting table).
4. **Load it.** Right-click the pad with the missile, or feed it in with a hopper, funnel or chute.
5. **Pick a target.** Aim a **Laser Range Finder** at a block, then right-click the controller with it, or type the coordinates into the controller.
6. **Fire!** Press *Launch* in the controller, give it a redstone signal, or use a **Remote Detonator** or **Laser Designator** on the controller's radio channel.

Each pad has its own settings: the height the missile climbs before it turns, a group number (the controller fires one group at a time) and a firing delay for staggered salvos. Missiles land within a few blocks of the target: about 2 blocks at short range, up to 12 at the longest range, plus a block for every extra pad in the same salvo.

> 🎢 **Feeling brave?** Right-click a missile on a pad to sit on it. You go along for the ride when it launches.

---

## 🏭 Machines

| Machine | Power | What it does |
|---|---|---|
| **Launch Pad** | 8 SU/RPM, shaft on the side away from the missile | Holds a missile upright and launches it in a ballistic arc. Needs a full charge (about 4 s at 64 RPM). |
| **Launch Gantry** | None | Stands next to the missile and carries the launch network between pads and the controller. |
| **Launch Connector** | None | A flat cable that carries the launch network along the floor. |
| **Launch Controller** | None | Target, group and radio channel for every pad in its network, plus the *Launch* button. Fires on redstone too. |
| **Cruise Turret** | 6 SU/RPM, shaft below | A rotating turret that aims a missile and fires it straight at the target. Normal speed at 32 RPM, slower below, up to 4x faster above. |
| **Radar Station** | 4 SU/RPM, shaft below, 16 RPM minimum | Spins its dish and tracks missiles up to 500 blocks away. Raises a redstone signal and a radio alert when one heads into its trigger range. |
| **EMP Tower** | 16 SU/RPM, shaft below | Charges a coil, then releases a pulse that drains energy from machines and can knock missiles out of the sky. 150 block radius, plus 20 blocks per **EMP Coil Extender** stacked on top. |

Engineer's Goggles show each machine's speed, stress and charge. The Launch Pad, Cruise Turret, Radar Station and EMP Tower all have Ponder scenes: hold **W** over one in your inventory.

---

## 🚀 Missiles

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/missiles.png" alt="Every missile in Create: ICBM" width="100%">
</p>

- **Missile Casing:** a pressed sheet-metal rocket body.
- **Missile Module:** a casing fitted with an Electron Tube and a Blaze Powder motor. It flies, but carries only a small charge.
- **Explosive Missile:** a Missile Module armed with any of the 27 explosives. The warhead goes off on impact.
- **Surface to Air Missile:** hunts flying targets, other missiles included. Fire it from a Cruise Turret or a handheld launcher.
- **Delivery Missile:** carries one stack of anything a chest slot can hold, shulker boxes included. On impact it puts the stack into the chest, barrel or Create vault it hits. Anywhere else it leaves the stack in a **Mail** parcel where it lands. It does no damage.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/delivery-menu.png" alt="The Delivery Missile cargo slot" width="45%">
</p>

To load a Delivery Missile, **shift + right-click** it in your hand to open its cargo slot, or right-click a stack onto it in your inventory, like a bundle.

Missiles can be shot down by SAMs, EMP blasts or enough damage. A missile brought down by fire or an explosion sets off its warhead where it falls.

### 📬 Mail

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/mail.png" alt="A Mail parcel with the sender's name on its label" width="70%">
</p>

When a Delivery Missile has no container to fill, it leaves a **Mail** parcel where it lands, the size of a slab. Its label shows the name of the player who launched the missile, and looking at it shows *Mail from &lt;name&gt;*. **Right-click** the parcel to take the items straight into your inventory. Once it is empty it disappears; if your inventory is full, whatever didn't fit stays inside for later.

Mail holds one stack of anything a chest can hold. It can't be crafted, broken, pushed by pistons or taken from the Creative inventory, and ordinary explosions can't break it, so a delivery is never scattered on the ground. A parcel nobody has opened for 3 minutes starts to **ding** every now and then, so it's easy to find, until someone opens it.

---

## 💣 Explosives

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/explosives.png" alt="All 27 explosive crates" width="100%">
</p>

Every explosive comes as a **crate** you can place and light (redstone, fire or a Remote Detonator), as a **missile** warhead and as a **bomb cart**. Higher tiers need hotter mixing: **tier 1** in a plain basin, **tier 2** over a heated Blaze Burner, **tiers 3 and 4** over a superheated one.

<details>
<summary><b>🟢 Tier 1</b>: 12 explosives</summary>

| Explosive | Effect |
|---|---|
| Condensed | A stronger TNT blast that reaches about half again as far as TNT. |
| Shrapnel | Sprays hundreds of burning metal shards. They set targets alight but leave blocks untouched. |
| Incendiary | Scatters fire over a wide area and clears away ice. Breaks no blocks. |
| Debilitation | A white gas cloud that lingers for 30 seconds. Walls keep it out. |
| Chemical | A yellow toxic cloud that lingers for 30 seconds. Walls keep it out. |
| Anvil | Hurls hundreds of heavy anvil chunks, each about three times harder than shrapnel. |
| Repulsive | Throws back every mob, player and item within 12 blocks. |
| Attractive | Pulls in every mob, player and item within 12 blocks. |
| Colorful | A harmless rainbow gas cloud. |
| Smoke Marking | A column of smoke for 5 seconds, handy for marking a target or testing your aim. |
| Flashbang | Every player within 16 blocks sees a blinding white flash fill the screen for a second. Breaks nothing. |
| Recon | A radar pulse makes every mob and enemy player within 32 blocks glow through walls for 3 minutes. Whoever launched or lit it, their team and their pets are left alone. |

</details>

<details>
<summary><b>🟡 Tier 2</b>: 7 explosives</summary>

| Explosive | Effect |
|---|---|
| Fragmentation | Over 200 explosive fragments that blow up where they land. |
| Contagious | A green viral cloud that lingers for 30 seconds. |
| Sonic | A shockwave out to about 15 blocks that rips up blocks and flings mobs into the air. |
| Breaching | Punches a 3x3 tunnel up to 7 blocks deep, even through obsidian. |
| Thermobaric | Wipes out terrain within about 30 blocks and kills anything in the open within 50. |
| Phantom Enemy | Conjures fake zombies, husks, spiders and creepers around every player within 24 blocks. They close in and swing but never deal damage; hit one and it vanishes in a puff of smoke. |
| Darkness | Gives every player and mob within 24 blocks the Darkness effect for 3 minutes. Breaks nothing. |

</details>

<details>
<summary><b>🟠 Tier 3</b>: 6 explosives</summary>

| Explosive | Effect |
|---|---|
| Nuclear | Wipes out terrain within about 50 blocks and leaves radioactive fallout behind. |
| EMP | Drains stored energy from machines and carried items within 50 blocks. Breaks nothing. |
| Exothermic | Turns the land within about 30 blocks into netherrack, magma and lava. |
| Endothermic | Freezes the land within about 30 blocks: ice and snow, lava to obsidian. |
| Anti-Gravitational | For 2 minutes, lifts blocks, mobs and players within about 30 blocks into the sky. |
| Ender | Opens an ender vortex for 8 seconds that drags things in and teleports them. Right-click the crate with a Laser Range Finder to pick where they land. |

</details>

<details>
<summary><b>🔴 Tier 4</b>: 2 explosives</summary>

| Explosive | Effect |
|---|---|
| Antimatter | Erases everything within 55 blocks, even bedrock. |
| Red Matter | Spawns a black hole that devours blocks and grows as it feeds. |

</details>

Gas, radiation and contagion are cured by drinking an **Antidote**. **Radioactive Dirt** and **Radioactive Stone** are left behind by nuclear blasts and irradiate whoever stands on them; over time they decay back to normal blocks.

---

## 🛒 Bomb Carts

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/bomb-carts.png" alt="Bomb carts" width="85%">
</p>

A minecart with an explosive crate in it, one for every explosive. Run it over a powered **activator rail** to light the fuse. A cart that is broken while moving, burning or caught in a blast goes off at once.

---

## 🧰 Tools

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/tools.png" alt="Handheld tools" width="100%">
</p>

| Tool | Use |
|---|---|
| **Laser Range Finder** | Right-click to measure the coordinates of the block you aim at. Right-click a Launch Controller or Cruise Turret to hand the target over. |
| **Remote Detonator** | Sends a radio signal on its channel: sets off explosives and launches missiles on the same channel. |
| **Laser Designator** | Paints the block you aim at and radios it to every launcher on its channel, which fire at it. |
| **Rocket Launcher: Direct** | A shoulder launcher that fires a missile from your inventory in a straight line. |
| **Rocket Launcher: Ballistic** | Lobs a missile in a high arc onto where you aim. |

---

## 🛡️ Air Defence

1. Place a **Radar Station** and spin it at 16 RPM or more. Open it to set its detection range, trigger range and a radio channel.
2. Place one or more **Cruise Turrets** with a shaft below and load each with a **Surface to Air Missile**. Set them to the radar's radio channel.
3. When a missile heads into the trigger range, the radar raises the alarm and the turrets swing round and fire their SAMs at it. Turn on the radar's redstone output to sound sirens or close doors at the same time.

An **EMP Tower** wired to the radar's redstone output is the last line of defence: its pulse can knock missiles in range out of the sky.

---

## 🗄️ Storage and Automation

- Launch Pads and Cruise Turrets take missiles from hoppers, Create funnels and chutes. A Launch Controller or Launch Gantry accepts missiles for every pad in its network.
- **Tom's Simple Storage autostock:** put a **Basic Inventory Hopper** with its back on your storage network (a cable or connector), a missile in its filter slot and its front against the **Launch Controller**. It refills every empty pad in that launch network from storage. Gantries and controllers are hidden from Tom's Inventory Connector so missiles aren't counted twice; connect the pads themselves to see their missiles in the terminal.

---

## 🗺️ BlueMap

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/bluemap.png" alt="BlueMap markers: a missile in flight, a mushroom cloud at an impact and a parcel from a Delivery Missile" width="100%">
</p>

Install [BlueMap](https://modrinth.com/plugin/bluemap) next to Create: ICBM and every missile in the air shows up on its web map. Nothing to set up: the markers appear as soon as BlueMap has loaded.

- 🚀 **Missiles in flight** move across the map, with the icon pointing the way they fly. Missiles crossing unloaded chunks are shown too, along their path to the target.
- 👆 **Click a missile** to see what it is, **who launched it** and where it is heading.
- 🍄 **Impacts** leave a small **mushroom cloud** where the warhead went off. Click it to see the missile and who launched it. The cloud disappears from the map after **1 minute**.
- 📦 **Delivery Missiles** leave a **parcel** where their Mail is, with who sent it. It **stays on the map until someone opens the Mail**, even across restarts. A delivery that went straight into a chest shows its parcel for 1 minute.
- 👁️ The markers sit on their own **Missiles** layer, which you can hide in BlueMap's menu.

Create: ICBM only adds its own marker layer. It never touches BlueMap's rendered tiles, textures or settings, and BlueMap works exactly as before.

BlueMap publishes markers every 10 seconds, so a missile moves across the map in steps. Using your own webserver instead of BlueMap's built-in one? Set `write-markers-interval` in BlueMap's `plugin.conf` (10 seconds matches the built-in one), otherwise BlueMap only writes markers for the page every few minutes.

Blocks showing the wrong textures on the map come from BlueMap's browser cache after it re-rendered a map or rebuilt its texture list (for example after `/bluemap purge`). Click **Update Map** in BlueMap's menu or reload the page with Ctrl+F5.

Settings are in `create_icbm-mods-bluemap.toml`: `enabled`, `update_ticks` (how often markers move, default 100 ticks) and `impact_marker_seconds` (default 60, `0` turns impact markers off; parcels of unopened Mail stay until it is opened).

---

## 🔨 Crafting

Every recipe uses only Create and vanilla items. Almost everything can be made two ways: on Create's machines (Mechanical Crafting, Mixing, Deploying, Milling) or at a plain crafting table. Both give the same item, and JEI shows all of them.

Each explosive is mixed from a lower one, so you work your way up from Condensed Explosives.

<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/explosives-overview.png" alt="How the explosives are made from each other" width="100%">
</p>

<details>
<summary><b>🏭 Machine recipes</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-pad.png" alt="Launch Pad recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-controller.png" alt="Launch Controller recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launch-gantry.png" alt="Launch Gantry recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/launcher-connector.png" alt="Launch Connector recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/cruise-turret.png" alt="Cruise Turret recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/radar-station.png" alt="Radar Station recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/emp-tower.png" alt="EMP Tower recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/emp-tower-extender.png" alt="EMP Coil Extender recipe" width="80%">
</p>
</details>

<details>
<summary><b>🚀 Missile recipes</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missile-casing.png" alt="Missile Casing recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/missile-module.png" alt="Missile Module recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/explosive-missile.png" alt="Explosive Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/surface-to-air-missile.png" alt="Surface to Air Missile recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/delivery-missile.png" alt="Delivery Missile recipe" width="80%">
</p>
</details>

<details>
<summary><b>🧰 Tool, material and cart recipes</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/bomb-cart.png" alt="Bomb Cart recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/laser-range-finder.png" alt="Laser Range Finder recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/remote-detonator.png" alt="Remote Detonator recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/laser-designator.png" alt="Laser Designator recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/rocket-launcher.png" alt="Rocket Launcher: Direct recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/ballistic-rocket-launcher.png" alt="Rocket Launcher: Ballistic recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/poison-powder.png" alt="Poison Powder recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/antidote.png" alt="Antidote recipe" width="80%">
</p>

A plain TNT block still makes the vanilla TNT Minecart.
</details>

<details>
<summary><b>💣 Explosive recipes: Tier 1</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/condensed-explosives.png" alt="Condensed Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/shrapnel-explosives.png" alt="Shrapnel Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/incendiary-explosives.png" alt="Incendiary Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/debilitation-explosives.png" alt="Debilitation Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/chemical-explosives.png" alt="Chemical Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/anvil-explosives.png" alt="Anvil Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/repulsive-explosives.png" alt="Repulsive Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/attractive-explosives.png" alt="Attractive Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/colorful-explosives.png" alt="Colorful Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/smoke-marking-explosives.png" alt="Smoke Marking Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/flashbang-explosives.png" alt="Flashbang Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/recon-explosives.png" alt="Recon Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>💣 Explosive recipes: Tier 2</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/fragmentation-explosives.png" alt="Fragmentation Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/contagious-explosives.png" alt="Contagious Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/sonic-explosives.png" alt="Sonic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/breaching-explosives.png" alt="Breaching Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/thermobaric-explosives.png" alt="Thermobaric Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/phantom-enemy-explosives.png" alt="Phantom Enemy Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/darkness-explosives.png" alt="Darkness Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>💣 Explosive recipes: Tier 3</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/nuclear-explosives.png" alt="Nuclear Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/emp-explosives.png" alt="EMP Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/exothermic-explosives.png" alt="Exothermic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/endothermic-explosives.png" alt="Endothermic Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/anti-gravitational-explosives.png" alt="Anti-Gravitational Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/ender-explosives.png" alt="Ender Explosives recipe" width="80%">
</p>
</details>

<details>
<summary><b>💣 Explosive recipes: Tier 4</b></summary>
<p align="center">
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/antimatter-explosives.png" alt="Antimatter Explosives recipe" width="80%"><br>
  <img src="https://raw.githubusercontent.com/navin-hariharan/create-icbm/main/doc/images/recipes/red-matter-explosives.png" alt="Red Matter Explosives recipe" width="80%">
</p>
</details>

---

## ⚙️ Configuration and Commands

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
| `impact_marker_seconds` | 60 | BlueMap: how long impact markers stay on the map. See the BlueMap section above. |

**Operator commands**, all under `/icbm`:

| Command | Does |
|---|---|
| `/icbm help` | Lists the commands. |
| `/icbm remove <all\|missile\|explosion> [range]` | Removes missiles and/or running blasts around you. |
| `/icbm lag [range]` | Clears every running blast and ICBM entity around you. |
| `/icbm blast list` | Lists the explosive ids. |
| `/icbm blast trigger <explosive> ...` | Sets off a blast at a position, for testing. |
| `/icbm blast spread ...` | Sets off a grid of blasts, for stress testing. |

---

## 🙏 Credits

**Create: ICBM is inspired by [ICBM-Classic](https://github.com/BuiltBrokenModding/ICBM-Classic)** and the original **ICBM** mod. Parts of its code are based on ICBM-Classic's MIT-licensed code; ICBM-Classic's MIT notice is included in the [LICENSE](https://github.com/navin-hariharan/create-icbm/blob/main/LICENSE) file.

Create: ICBM is an independent, unofficial project and is not affiliated with the ICBM-Classic or Create teams.

Built for [Create](https://modrinth.com/mod/create) by simibubi and the Creators of Create. Create's textures are not included in this mod; its models use them from your installed copy of Create.

## 📜 Licence

**Code and art:** [Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).

**Sound effects:** Versions **1.0.1 and earlier** contain sound effects sourced from ICBM-Classic; those sounds are not covered by this project's licence and remain subject to their applicable original licensing terms. Beginning with **version 1.0.2**, sound effects are covered by this project's **CC BY-NC 4.0** licence.

The parts of the code based on ICBM-Classic remain under ICBM-Classic's MIT licence; see [LICENSE](https://github.com/navin-hariharan/create-icbm/blob/main/LICENSE).
