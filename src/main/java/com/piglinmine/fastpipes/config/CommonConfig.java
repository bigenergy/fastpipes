package com.piglinmine.fastpipes.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Content toggles for pack authors who only want part of the mod.
 * <p>
 * These live in the COMMON config rather than the SERVER one, even though they only ever gate
 * content: the three places that read them all run where a server config is not available.
 * Creative tab contents are assembled on the client, including on the title screen before any
 * world exists; recipe conditions are evaluated as the data pack loads; and recipe viewers build
 * their item list from the creative tabs. A common config is loaded on both sides during mod
 * construction, so it is ready for all three. A pack ships the same file to client and server.
 * <p>
 * Disabled content stays registered, so flipping a switch back restores existing worlds intact —
 * blocks already placed keep working either way, they just cannot be obtained again.
 */
public class CommonConfig {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final ModConfigSpec.BooleanValue enableBarrels;
    private final ModConfigSpec.BooleanValue enableTerminal;

    public CommonConfig() {
        builder.push("content");
        {
            enableBarrels = builder
                .comment(
                    "Whether the tiered storage barrels and their upgrade items are available.",
                    "When false they are hidden from creative tabs and recipe viewers, and their",
                    "recipes are disabled. Already-placed barrels keep working.",
                    "Requires a restart or /reload to take effect."
                )
                .define("enableBarrels", true);

            enableTerminal = builder
                .comment(
                    "Whether the pipe terminal is available.",
                    "When false it is hidden from creative tabs and recipe viewers, and its recipe",
                    "is disabled. Already-placed terminals keep working.",
                    "Requires a restart or /reload to take effect."
                )
                .define("enableTerminal", true);
        }
        builder.pop();

        spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    public boolean isBarrelsEnabled() {
        return read(enableBarrels);
    }

    public boolean isTerminalEnabled() {
        return read(enableTerminal);
    }

    /**
     * Reading a value before its spec is loaded throws, and these are read from three different
     * points in the lifecycle (creative tabs, recipe conditions, recipe viewers). Anything that
     * manages to ask early enough gets the default rather than a crash, and the default is
     * "enabled" so a timing edge case can never quietly delete content from a pack.
     */
    private boolean read(ModConfigSpec.BooleanValue value) {
        return !spec.isLoaded() || value.get();
    }
}
