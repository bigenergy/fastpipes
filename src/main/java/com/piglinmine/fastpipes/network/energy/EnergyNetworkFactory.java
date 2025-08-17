package com.piglinmine.fastpipes.network.energy;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.NetworkFactory;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import com.piglinmine.fastpipes.util.StringUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class EnergyNetworkFactory implements NetworkFactory {
    private static final Logger LOGGER = LogManager.getLogger(EnergyNetworkFactory.class);

    private final EnergyPipeType pipeType;

    public EnergyNetworkFactory(EnergyPipeType pipeType) {
        this.pipeType = pipeType;
    }

    @Override
    public Network create(BlockPos pos) {
        return new EnergyNetwork(pos, StringUtil.randomString(new Random(), 8), pipeType);
    }

    @Override
    public Network create(CompoundTag tag) {
        EnergyNetwork network = new EnergyNetwork(BlockPos.of(tag.getLong("origin")), tag.getString("id"), pipeType);

        LOGGER.debug("Deserialized energy network {} of type {}", network.getId(), network.getType().toString());

        return network;
    }
} 