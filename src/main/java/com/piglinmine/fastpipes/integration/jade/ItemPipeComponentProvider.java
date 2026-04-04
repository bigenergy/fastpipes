package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ItemPipeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();

        if (serverData.contains("Speed")) {
            int speed = serverData.getInt("Speed");
            int itemsInTransit = serverData.getInt("ItemsInTransit");

            tooltip.add(Component.translatable("jade.fastpipes.item_speed", speed));
            if (itemsInTransit > 0) {
                tooltip.add(Component.translatable("jade.fastpipes.items_in_transit", itemsInTransit));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ItemPipeBlockEntity) {
            NetworkManager networkManager = NetworkManager.get(accessor.getLevel());
            Pipe pipe = networkManager.getPipe(accessor.getPosition());

            if (pipe instanceof ItemPipe itemPipe) {
                data.putInt("Speed", itemPipe.getMaxTicksInPipe());
                data.putInt("ItemsInTransit", itemPipe.getTransports().size());
            } else {
                data.putInt("Speed", 0);
                data.putInt("ItemsInTransit", 0);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FastPipesJadePlugin.ITEM_PIPE_INFO;
    }
}
