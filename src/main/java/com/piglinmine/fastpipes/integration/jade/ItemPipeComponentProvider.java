package com.piglinmine.fastpipes.integration.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ItemPipeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();

        if (serverData.contains("Speed")) {
            int speed = serverData.getIntOr("Speed", 0);
            int itemsInTransit = serverData.getIntOr("ItemsInTransit", 0);

            tooltip.add(Component.translatable("jade.fastpipes.item_speed", speed));
            if (itemsInTransit > 0) {
                tooltip.add(Component.translatable("jade.fastpipes.items_in_transit", itemsInTransit));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.ITEM_PIPE_INFO;
    }
}
