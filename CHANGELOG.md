# Changelog
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
