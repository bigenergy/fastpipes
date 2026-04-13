# Fast Pipes

A modern, high-performance pipe system for items, fluids, and energy — optimized for low overhead on servers and clients. Built for **NeoForge 1.21.1** with optional JEI and Jade integration.

---

## Pipes

### Item Pipes

Transport items between inventories with visible item animations inside the pipes.

| Tier | Speed (ticks/pipe) |
|------|--------------------|
| Basic | 30 |
| Improved | 20 |
| Advanced | 10 |

### Fluid Pipes

Transport fluids between tanks with animated fluid fill level visualization.

| Tier | Capacity | Transfer Rate |
|------|----------|---------------|
| Basic | 1,000 mB | 100 mB/t |
| Improved | 4,000 mB | 400 mB/t |
| Advanced | 8,000 mB | 800 mB/t |
| Elite | 16,000 mB | 1,600 mB/t |
| Ultimate | 32,000 mB | 3,200 mB/t |

### Energy Pipes

Transport energy (FE/RF) between machines.

| Tier | Capacity | Transfer Rate |
|------|----------|---------------|
| Basic | 1,000 FE | 1,000 FE/t |
| Improved | 4,000 FE | 4,000 FE/t |
| Advanced | 8,000 FE | 8,000 FE/t |
| Elite | 16,000 FE | 16,000 FE/t |
| Ultimate | 32,000 FE | 32,000 FE/t |

---

## Attachments

Attachments are placed on pipe sides by right-clicking. Remove with Sneak + empty hand.

### Extractor Attachment

Actively pulls items/fluids from adjacent inventories into the pipe network.

| Tier | Items/op | Interval | Fluid/op | Filter Slots | Redstone | Whitelist/Blacklist | Routing | Exact |
|------|----------|----------|----------|--------------|----------|---------------------|---------|-------|
| Basic | 8 | 60t (3s) | 100 mB | 0 | - | - | - | - |
| Improved | 16 | 40t (2s) | 400 mB | 4 | Yes | - | - | Yes |
| Advanced | 32 | 20t (1s) | 800 mB | 8 | Yes | Yes | Yes | Yes |
| Elite | 64 | 10t (0.5s) | 1,600 mB | 12 | Yes | Yes | Yes | Yes |
| Ultimate | 64 | 10t (0.5s) | 3,200 mB | 15 | Yes | Yes | Yes | Yes |

**Routing Modes** (Advanced+):
- **Nearest first** — routes to the closest destination
- **Furthest first** — routes to the farthest destination
- **Random** — picks a random valid destination
- **Round robin** — cycles through all destinations evenly

**Redstone Modes** (Improved+):
- **Ignored** — always active
- **High** — only active when receiving redstone signal
- **Low** — only active when NOT receiving redstone signal

### Inserter Attachment

Marks a pipe side as a **destination** for routed items/fluids. Use these to control where items end up.

| Tier | Filter Slots | Redstone | Whitelist/Blacklist | Exact | Priority |
|------|--------------|----------|---------------------|-------|----------|
| Basic | 0 | - | - | - | 0 |
| Improved | 4 | Yes | - | - | 1 |
| Advanced | 8 | Yes | Yes | Yes | 2 |
| Elite | 12 | Yes | Yes | Yes | 3 |
| Ultimate | 15 | Yes | Yes | Yes | 4 |

Items are always routed to the **highest priority** inserter that accepts them. This allows overflow systems — high-priority inserters fill first, then items go to lower-priority destinations.

### Void Attachment

Destroys excess items/fluids. Acts as the **lowest priority** destination — items are only voided when no other destination can accept them.

- 15 filter slots
- Whitelist/Blacklist mode
- Exact mode

### Sensor Attachment

Emits a **redstone signal** (strength 15) for 1 second when matching items/fluids pass through the pipe.

- 15 filter slots
- Whitelist/Blacklist mode
- Exact mode

---

## Item Sorting Guide

Fast Pipes has a full item sorting system built in. Here's how to set it up:

### Basic Sorting Setup

1. Place an **Extractor** on the pipe facing your source inventory (e.g., an unsorted chest)
2. Connect pipes to all your destination chests
3. Place an **Inserter** on each pipe side facing a destination chest
4. Open each Inserter's GUI and switch to **Whitelist** mode
5. Add the items you want routed to that chest into the filter slots

Items will only be sent to a destination where the Inserter's filter accepts them.

### Priority-Based Overflow

Use inserter tiers to create overflow logic:

- **Elite Inserter** (priority 3) on your main storage — items go here first
- **Basic Inserter** (priority 0) on an overflow chest — catches everything else

### Round Robin Distribution

Use an Advanced+ Extractor with **Round Robin** routing mode to evenly distribute items across multiple destinations.

### Void Excess

Add a **Void Attachment** at the end of your system to destroy items that no inserter accepted. Since Void has the lowest priority, it only activates when all other destinations are full or filtered out.

---

## Colored Pipes

Right-click any pipe with a **dye** to color it. Pipes of different colors **will not connect** to each other, allowing you to run separate networks side by side.

- All 16 Minecraft dye colors are supported
- Right-click with a **water bucket** to remove the color
- Uncolored pipes connect to any color

---

## Pipe Wrench

- **Right-click** a pipe side to disconnect/reconnect it
- **Sneak + Right-click** to break the pipe

Compatible with other mods' wrenches that support `wrench_rotate`.

---

## Pipe Terminal

A centralized access point for all inventories connected to the pipe network.

- **Search** items across the entire network (supports `@mod` filter)
- **Extract** items by clicking — pulls directly from connected inventories
- **Insert** items by placing them in the player inventory area
- **Craft** using network resources — built-in 3x3 crafting grid that pulls ingredients from connected storage
- **Sort** by name, count, or mod

Place the Terminal next to any item pipe — it auto-connects to the network.

---

## Tiered Barrels

Expanded storage barrels with upgradeable capacity. Works seamlessly with pipe networks.

| Tier | Slots | Upgrade Item |
|------|-------|-------------|
| Oak | 27 | — |
| Copper | 45 | Copper Barrel Upgrade |
| Iron | 54 | Iron Barrel Upgrade |
| Gold | 72 | Gold Barrel Upgrade |
| Diamond | 90 | Diamond Barrel Upgrade |
| Netherite | 108 | Netherite Barrel Upgrade |

- Right-click a barrel with an upgrade item to upgrade it in-place (contents preserved)
- Netherite barrel is blast resistant
- All barrels connect to pipes via extractors/inserters

---

## Waterlogging

All pipes support waterlogging — they can be placed underwater and water flows correctly around pipe geometry.

---

## Compatibility

- **JEI** — item descriptions for all pipes, attachments, barrels, upgrades, and terminal
- **Jade** — tooltip overlays showing pipe stats (transfer rate, items in transit, fluid stored)

---

## Configuration

All values are fully configurable in the server config file. You can adjust:

- Pipe speeds, capacities, and transfer rates per tier
- Extractor extraction rates, intervals, and feature flags per tier
- Inserter filter slots, priorities, and feature flags per tier
- Barrel slot counts per tier
