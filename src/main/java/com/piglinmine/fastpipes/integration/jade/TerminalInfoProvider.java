package com.piglinmine.fastpipes.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TerminalInfoProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("Connected")) return;

        boolean connected = data.getBooleanOr("Connected", false);
        if (!connected) {
            tooltip.add(Component.translatable("jade.fastpipes.terminal.disconnected")
                .withStyle(ChatFormatting.RED));
            return;
        }

        int uniqueStacks = data.getIntOr("UniqueStacks", 0);
        long totalItems = data.getLongOr("TotalItems", 0L);

        tooltip.add(Component.literal("• ")
            .append(Component.translatable("jade.fastpipes.terminal.unique_stacks").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.literal(String.valueOf(uniqueStacks)).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.literal("• ")
            .append(Component.translatable("jade.fastpipes.terminal.total_items").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.literal(String.valueOf(totalItems)).withStyle(ChatFormatting.WHITE)));

        if (data.contains("ActiveUser")) {
            tooltip.add(Component.literal("• ")
                .append(Component.translatable("jade.fastpipes.terminal.in_use_by").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": "))
                .append(Component.literal(data.getStringOr("ActiveUser", "")).withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.TERMINAL_INFO;
    }
}
