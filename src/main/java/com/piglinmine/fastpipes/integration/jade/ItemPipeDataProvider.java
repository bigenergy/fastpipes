package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ItemPipeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

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
    public Identifier getUid() {
        return FastPipesJadePlugin.ITEM_PIPE_INFO;
    }
}
