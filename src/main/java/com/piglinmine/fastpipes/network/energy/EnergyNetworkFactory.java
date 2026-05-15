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

    /**
     * Unified factory — all tiers share one network type now. The network's effective tier
     * is recomputed on each scanGraph based on member pipes; BASIC here is just a placeholder
     * for the initial state before the first scan.
     */
    @Override
    public Network create(BlockPos pos) {
        return new EnergyNetwork(pos, StringUtil.randomString(new Random(), 8), EnergyPipeType.BASIC);
    }

    @Override
    public Network create(CompoundTag tag) {
        EnergyNetwork network = new EnergyNetwork(BlockPos.of(tag.getLong("origin")), tag.getString("id"), EnergyPipeType.BASIC);
        LOGGER.debug("Deserialized energy network {}", network.getId());
        return network;
    }
} 