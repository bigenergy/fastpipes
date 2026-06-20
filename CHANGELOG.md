# Changelog
## [1.3.4] - 2026-06-20

### Fixed
- Crash on load with Sable / contraption mods — pipes on simulated platforms no longer throw when sending block updates
- NPE when adding/removing attachments on a temporarily networkless pipe — caused desync (broken render, duplicate drop)
- Copper Barrel Upgrade now accepts both `minecraft:barrel` and `fastpipes:oak_barrel`

### Changed
- Mixed-tier energy networks: per-pipe throughput instead of MIN-tier bottleneck. Each boundary pipe rate-limits its own I/O, capacity still sums across all pipes

## [1.3.3] - 2026-05-15

### Added
- **Jade integration for attachments and terminal** — pipe attachments (Extractor / Inserter / Sensor / Void) now appear in Jade's WAILA tooltip with tier, side, redstone mode, blacklist/whitelist, routing mode, stack size, exact mode, and fluid mode. Pipe Terminal shows network status (unique stacks, total items, active user).
- **Mixed-tier Energy Pipe networks** — energy pipes of any tier now connect into a single network. Capacity is summed across all tiers; transfer rate is bottlenecked by the slowest tier in the network (no free upgrade — a single Ultimate pipe in a Basic network won't accelerate the whole network).
- **TConstruct support for barrels** — all barrels now support side inventory by crafting station

### Fixed
- **Attachment GUIs not opening** — fixed asymmetric `FluidStack` packet serialization between server and client. Server wrote a boolean-prefixed FluidStack but the client factory decoded the payload directly, causing a `NullPointerException` in Forge's `readRegistryId` on the first empty fluid filter slot and silently breaking every Extractor / Inserter / Sensor / Void menu.
- **Removed attachment didn't appear in inventory** — the dropped item used to spawn as an `ItemEntity`, which produced a client-side desync where the pickup sound played but the inventory didn't update until another container was opened. Items are now given directly via `ItemHandlerHelper.giveItemToPlayer` with proper sync.
- **Energy flow stopped after adding a second power source** — adjacent energy generators / consumers cached the pipe's `LazyOptional<IEnergyStorage>` which bound to a specific network instance; after a network rescan their cache pointed at an orphaned network and all flow stopped, with pipe energy appearing to reset to 0. The pipe now exposes a stable `LazyOptional` backed by a delegating storage that always queries the current network.

### Changed
- Wrench right-click on an attachment opens the attachment GUI instead of toggling disconnect on that side. Toggle disconnect still works on bare pipe sides.
- Removing an attachment now clears a stale "disconnected" flag on that side so the pipe reconnects normally to the adjacent inventory.
- Migrated all `new ResourceLocation(...)` calls to `ResourceLocation.fromNamespaceAndPath(...)` / `ResourceLocation.parse(...)` to remove deprecation warnings.

### Notes
- Existing worlds with tier-specific energy networks load fine — legacy network IDs are mapped to the unified energy network type, and pipes merge on the next scan. Pre-existing buffered energy in those networks is not persisted (was never persisted) so it resets once after the update.

## [1.3.2] - 2026-05-11

### Fixed
- **Barrel upgrade item duplication** — applying a barrel upgrade no longer drops a duplicate copy of the barrel's contents into the world while preserving them in the upgraded barrel. The old block entity is cleared before the block is replaced so `onRemove` has nothing to drop.
- **Wrench shift+right-click not breaking pipes** — wrench item now bypasses vanilla's sneak-use suppression (`doesSneakBypassUse`), so shift+right-clicking a pipe with the wrench correctly breaks and drops it.
- **Wrench right-click on attachment now opens its GUI** — previously the wrench's "toggle disconnect" action triggered first, silently disconnecting the pipe side instead of opening the attachment. Toggle disconnect still works on bare pipe sides.
- **Removing an attachment clears stale disconnect state** — if a side was toggled disconnected (e.g. accidentally with the wrench while the attachment was installed), removing the attachment now resets that side so the pipe reconnects to the adjacent inventory.

## [1.3.1] - 2026-04-19

### Fixed
- Items no longer dropped in world when destination inventory is full (pre-check + network-wide fallback)
- Terminal extraction broken for aggregated stacks > 127 items (byte-overflow in packet)
- GUI dead zones on attachment edges (clicks near borders now reliably open the GUI)
- Rare NPE crashes during pipe network rescans

### Added
- Terminal sort mode persists across GUI closes
- Terminal single-user lock — one player at a time; auto-recovers from stale locks

## [1.3.0] - 2026-04-13

### Added
- **Forge 1.20.1 backport** — full port from NeoForge 1.21.1 to Forge 1.20.1 (Java 17, Forge 47.3.0).
- **Pipe Terminal** — access all inventories connected to the pipe network from a single block. Supports searching, extracting, inserting, and crafting directly from network resources. Place next to any item pipe to connect.
- **Tiered Barrels** — Oak (27 slots), Copper (45), Iron (54), Gold (72), Diamond (90), Netherite (108, blast resistant). Upgradeable in-place with barrel upgrades that preserve contents.
- **Waterlogged pipes** — all pipes can now be placed underwater. Water flows correctly around pipe geometry.
- **JEI descriptions** for all items — barrels, barrel upgrades, Void Attachment, Sensor Attachment, and Terminal now have info pages in JEI.
- **JEI integration** in dev environment (`runClient`) via `fg.deobf()`.
- **Russian translation** fully updated — all new keys including JEI descriptions, barrel items, and terminal tooltips.

### Changed
- **Pipe crafting recipes reworked** — tier 2+ pipes now require the previous tier pipe as an ingredient (e.g. Improved Item Pipe = 4× Gold + Basic Item Pipe). More logical progression.
- **Terminal "in network" tooltip** externalized to translation key (`gui.fastpipes.terminal.in_network`) for proper localization support.
- Recipe format updated for Forge 1.20.1 (`recipes/` directory, `forge:` tags, `"item"` result keys).
- Loot table format updated for Forge 1.20.1 (`loot_tables/` directory).

### Fixed
- **Invisible pipes** — fixed model registration using plain `ResourceLocation` instead of `ModelResourceLocation` for Forge 1.20.1.
- **Missing crafting recipes** — fixed directory names, tag namespaces (`c:` → `forge:`), and result format for 1.20.1.
- **Pipes not connecting to Terminal** — Terminal now exposes `IItemHandler` capability so adjacent item pipes auto-connect.

## [1.2.2] - 2026-04-12

### Fixed
- Extractors now pull unstackable items (tools, armor, weapons) from multiple slots per operation, massively improving throughput for mob farms and similar setups

## [1.2.1] - 2026-04-12

### Fixed
- Fix NPE crash when deserializing item transport callbacks
- Added russian translate

## [1.2.0] - 2026-04-04

### Added
- **Colored Pipes** — right-click any pipe with a dye to color it. Same-color pipes connect, different colors don't. Uncolored pipes act as universal connectors (connect to everything). Use a water bucket to remove color.
- **Void Attachment** — destroys all items/fluids that pass through it. Useful for overflow protection and trash disposal.
- **Sensor Attachment** — emits a redstone signal (strength 15) when an item passes through the pipe. Works with all redstone components.
- **Wrench Disconnect** — right-click a pipe side with a wrench to disconnect/reconnect it. Allows precise control over pipe connections without breaking blocks.
- Cross-mod wrench support via NeoForge ItemAbility system.

### Changed
- Network splitting logic rewritten for stability with colored pipe networks.
- Pipes no longer show false inventory connection indicators toward adjacent pipes of the same type.
- Block state updates during dyeing use `UPDATE_KNOWN_SHAPE` flag to prevent cascading overwrites.
- Client-side `updateShape` no longer recalculates connections — trusts server-sent block state for reliability.

### Fixed
- Fixed crash when removing a pipe that bridges multiple colored networks (`splitNetworks` rewrite).
- Fixed `removeNetwork` throwing RuntimeException when network was already removed during cascade.
- Fixed colored pipes visually appearing connected on client despite being different colors (inventory connection indicator was rendering between color-blocked pipes).
- Fixed pipe connections not updating visually after dyeing on client side.

## [1.1.0] - 2026-03-01

### Added
- Initial release of FastPipes for Minecraft 1.21.1 (NeoForge).
- **Item Pipes** — Basic, Improved, and Advanced tiers with different transfer speeds.
- **Fluid Pipes** — transport fluids between tanks and machines.
- **Energy Pipes** — transport Forge Energy (FE) between generators and consumers.
- **Extractor Attachment** — pulls items/fluids from adjacent inventories into the pipe network.
- **Inserter Attachment** — pushes items/fluids from the pipe network into adjacent inventories.
- **Wrench** — tool for managing pipe connections and attachments.
- Visual item transport animation inside pipes.
- JEI integration for attachment recipes.
