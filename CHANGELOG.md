# Changelog
## [1.3.3] - 2026-05-15

### Added
- **Jade integration for attachments and terminal** — pipe attachments (Extractor / Inserter / Sensor / Void) now appear in Jade's WAILA tooltip with tier, side, redstone mode, blacklist/whitelist, routing mode, stack size, exact mode, and fluid mode. Pipe Terminal shows network status (unique stacks, total items, active user).
- **Mixed-tier Energy Pipe networks** — energy pipes of any tier now connect into a single network. Capacity is summed across all tiers; transfer rate is bottlenecked by the slowest tier in the network (no free upgrade — a single Ultimate pipe in a Basic network won't accelerate the whole network).
- **Terminal sort mode persistence** — the selected sort (Name / Count / Mod) is saved per-terminal and restored after closing and reopening the GUI.
- **Terminal single-user lock** — only one player can have a given terminal open at a time; others see "Terminal is in use by <name>". Auto-recovers from stale locks (crashed/offline holder or menu already closed).

### Fixed
- **Items no longer dropped in world on full destination** — extractor now pre-checks destination capacity before pulling; items stay in source if target inventory is full. Bounce-back and orphaned-transport fallbacks scan the entire network for space before resorting to a world drop.
- **Removed attachment didn't appear in inventory** — the dropped item used to spawn as an `ItemEntity`, which produced a client-side desync where the pickup sound played but the inventory didn't update until another container was opened. Items are now given directly via `ItemHandlerHelper.giveItemToPlayer` with proper sync.
- **Energy flow stopped after adding a second power source** — adjacent energy generators / consumers cached the pipe's energy capability handle which bound to a specific network instance; after a network rescan their cache pointed at an orphaned network and all flow stopped, with pipe energy appearing to reset to 0. The pipe now exposes a stable delegating storage that always queries the current network.
- **Wrench shift+right-click not breaking pipes** — wrench item now bypasses sneak-use suppression (`doesSneakBypassUse`), so shift+right-clicking a pipe with the wrench correctly breaks and drops it.
- **Barrel upgrade item duplication** — applying a barrel upgrade no longer drops a duplicate copy of the barrel's contents into the world while preserving them in the upgraded barrel. The old block entity is cleared before the block is replaced so `onRemove` has nothing to drop.
- **GUI dead zones on attachment edges** — clicking near the edge of an attachment now reliably opens its GUI (falls back to the clicked face when the bounding-box hit misses).
- **Terminal cursor sync delay** — items now appear on the cursor immediately after extraction instead of after a tick.
- **NPE on pipe network tick** — `Pipe.leaveNetwork` and `ItemTransport.update` are now defensive against transient null-network states during rescans.

### Changed
- Wrench right-click on an attachment opens the attachment GUI instead of toggling disconnect on that side. Toggle disconnect still works on bare pipe sides.
- Removing an attachment now clears a stale "disconnected" flag on that side so the pipe reconnects normally to the adjacent inventory.

### Notes
- Existing worlds with tier-specific energy networks load fine — legacy network IDs are mapped to the unified energy network type, and pipes merge on the next scan. Pre-existing buffered energy in those networks is not persisted (was never persisted) so it resets once after the update.

## [1.3.0] - 2026-04-13

### Added
- **Pipe Terminal** — access all inventories connected to the pipe network from a single block. Supports searching, extracting, inserting, and crafting directly from network resources. Place next to any item pipe to connect.
- **Tiered Barrels** — Oak (27 slots), Copper (45), Iron (54), Gold (72), Diamond (90), Netherite (108, blast resistant). Upgradeable in-place with barrel upgrades that preserve contents.
- **Waterlogged pipes** — all pipes can now be placed underwater. Water flows correctly around pipe geometry.

### Changed
- **Pipe crafting recipes reworked** — tier 2+ pipes now require the previous tier pipe as an ingredient (e.g. Improved Item Pipe = 4× Gold Ingots + Basic Item Pipe). More logical tier progression.

### Fixed
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
