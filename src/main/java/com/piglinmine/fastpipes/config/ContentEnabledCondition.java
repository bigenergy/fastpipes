package com.piglinmine.fastpipes.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.piglinmine.fastpipes.FastPipes;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition that drops a recipe when the matching {@link CommonConfig} toggle is off.
 * <p>
 * Used in the barrel and terminal recipe files as
 * {@code {"type": "fastpipes:content_enabled", "content": "barrels"}}. Evaluated once per data
 * pack load, so a config change needs a restart or {@code /reload} — the same as any other recipe
 * change.
 */
public record ContentEnabledCondition(Content content) implements ICondition {
    public static final MapCodec<ContentEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Content.CODEC.fieldOf("content").forGetter(ContentEnabledCondition::content)
        ).apply(instance, ContentEnabledCondition::new)
    );

    public enum Content implements net.minecraft.util.StringRepresentable {
        BARRELS("barrels"),
        TERMINAL("terminal");

        public static final com.mojang.serialization.Codec<Content> CODEC =
            net.minecraft.util.StringRepresentable.fromEnum(Content::values);

        private final String name;

        Content(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        boolean isEnabled() {
            return switch (this) {
                case BARRELS -> FastPipes.COMMON_CONFIG.isBarrelsEnabled();
                case TERMINAL -> FastPipes.COMMON_CONFIG.isTerminalEnabled();
            };
        }
    }

    @Override
    public boolean test(IContext context) {
        return content.isEnabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
