package com.piglinmine.fastpipes.config;

import com.google.gson.JsonObject;
import com.piglinmine.fastpipes.FastPipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Recipe condition that drops a recipe when the matching {@link CommonConfig} toggle is off.
 * <p>
 * Used in the barrel and terminal recipe files as
 * {@code {"type": "fastpipes:content_enabled", "content": "barrels"}}. Evaluated once per data
 * pack load, so a config change needs a restart or {@code /reload} — the same as any other recipe
 * change.
 */
public record ContentEnabledCondition(Content content) implements ICondition {
    public static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "content_enabled");

    public enum Content {
        BARRELS("barrels"),
        TERMINAL("terminal");

        private final String name;

        Content(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return name;
        }

        static Content byName(String name) {
            for (Content content : values()) {
                if (content.name.equals(name)) {
                    return content;
                }
            }
            throw new IllegalArgumentException("Unknown fastpipes content toggle: " + name);
        }

        boolean isEnabled() {
            return switch (this) {
                case BARRELS -> FastPipes.COMMON_CONFIG.isBarrelsEnabled();
                case TERMINAL -> FastPipes.COMMON_CONFIG.isTerminalEnabled();
            };
        }
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return content.isEnabled();
    }

    public static class Serializer implements IConditionSerializer<ContentEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, ContentEnabledCondition condition) {
            json.addProperty("content", condition.content().getSerializedName());
        }

        @Override
        public ContentEnabledCondition read(JsonObject json) {
            return new ContentEnabledCondition(
                Content.byName(json.get("content").getAsString()));
        }

        @Override
        public ResourceLocation getID() {
            return ContentEnabledCondition.ID;
        }
    }
}
