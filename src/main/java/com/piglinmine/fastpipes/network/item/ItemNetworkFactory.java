package com.piglinmine.fastpipes.network.item;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.NetworkFactory;
import com.piglinmine.fastpipes.util.StringUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ItemNetworkFactory implements NetworkFactory {
    private static final Logger LOGGER = LogManager.getLogger(ItemNetworkFactory.class);

    @Override
    public Network create(BlockPos pos) {
        return new ItemNetwork(pos, StringUtil.randomString(new Random(), 8));
    }

    @Override
    public Network create(CompoundTag tag) {
        ItemNetwork network = new ItemNetwork(BlockPos.of(tag.getLong("origin")), tag.getString("id"));

        LOGGER.debug("Deserialized item network {}", network.getId());

        return network;
    }
} 