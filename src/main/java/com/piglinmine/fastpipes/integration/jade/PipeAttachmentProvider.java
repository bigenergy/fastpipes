package com.piglinmine.fastpipes.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PipeAttachmentProvider implements IBlockComponentProvider {
    INSTANCE;

    static final String NBT_KEY = "Attachments";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(NBT_KEY)) return;

        ListTag list = data.getListOrEmpty(NBT_KEY);
        if (list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            CompoundTag att = list.getCompoundOrEmpty(i);
            renderAttachment(tooltip, att);
        }
    }

    private void renderAttachment(ITooltip tooltip, CompoundTag att) {
        String itemId = att.contains("itemId") ? att.getStringOr("itemId", "") : att.getStringOr("id", "");
        Direction side = Direction.values()[att.getIntOr("side", 0)];

        Component sideName = Component.translatable("misc.fastpipes.direction." + side.getName());
        Component typeName = Component.translatable("item." + itemId.replace(":", "."));
        tooltip.add(Component.literal("• ")
            .append(typeName.copy().withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" — ").withStyle(ChatFormatting.DARK_GRAY))
            .append(sideName.copy().withStyle(ChatFormatting.GRAY)));

        if (att.contains("redstone")) {
            tooltip.add(line("misc.fastpipes.redstone_mode",
                "misc.fastpipes.redstone_mode." + att.getStringOr("redstone", "").toLowerCase()));
        }
        if (att.contains("bw")) {
            tooltip.add(line("misc.fastpipes.mode",
                "misc.fastpipes.mode." + att.getStringOr("bw", "").toLowerCase()));
        }
        if (att.contains("routing")) {
            tooltip.add(line("misc.fastpipes.routing_mode",
                "misc.fastpipes.routing_mode." + att.getStringOr("routing", "").toLowerCase()));
        }
        if (att.contains("stackSize")) {
            tooltip.add(Component.literal("  ")
                .append(Component.translatable("jade.fastpipes.stack_size").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": "))
                .append(Component.literal(String.valueOf(att.getIntOr("stackSize", 0))).withStyle(ChatFormatting.WHITE)));
        }
        if (att.contains("exact")) {
            tooltip.add(line("misc.fastpipes.exact_mode",
                "misc.fastpipes.exact_mode." + (att.getBooleanOr("exact", false) ? "on" : "off")));
        }
        if (att.contains("fluidMode") && att.getBooleanOr("fluidMode", false)) {
            tooltip.add(Component.literal("  ")
                .append(Component.translatable("jade.fastpipes.fluid_mode").withStyle(ChatFormatting.AQUA)));
        }
    }

    private Component line(String labelKey, String valueKey) {
        return Component.literal("  ")
            .append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.translatable(valueKey).withStyle(ChatFormatting.WHITE));
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.PIPE_ATTACHMENTS;
    }
}
