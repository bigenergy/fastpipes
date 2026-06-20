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

    public EnergyNetworkFactory() {
    }

    @Override
    public Network create(BlockPos pos) {
        // Placeholder type — actual effective type is recomputed on scanGraph.
        return new EnergyNetwork(pos, StringUtil.randomString(new Random(), 8), EnergyPipeType.BASIC);
    }

    @Override
    public Network create(CompoundTag tag) {
        EnergyNetwork network = new EnergyNetwork(BlockPos.of(tag.getLongOr("origin", 0L)), tag.getStringOr("id", ""), EnergyPipeType.BASIC);

        if (tag.contains("energy")) {
            network.getEnergyStorage().setStored(tag.getIntOr("energy", 0));
        }

        LOGGER.debug("Deserialized energy network {} of type {}", network.getId(), network.getType().toString());

        return network;
    }
}
