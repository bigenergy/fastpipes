package com.piglinmine.fastpipes.network.energy;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.graph.NetworkGraphScannerResult;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyNetwork extends Network {
    private final EnergyStorage energyStorage;
    private final EnergyPipeType pipeType;

    public EnergyNetwork(BlockPos originPos, String id, EnergyPipeType pipeType) {
        super(originPos, id);

        this.pipeType = pipeType;
        this.energyStorage = new EnergyStorage(0);
        this.energyStorage.setMaxReceive(pipeType.getCapacity());
    }

    @Override
    public NetworkGraphScannerResult scanGraph(Level level, BlockPos pos) {
        NetworkGraphScannerResult result = super.scanGraph(level, pos);

        energyStorage.setCapacityAndMaxExtract(
            result.getFoundPipes()
                .stream()
                .filter(p -> p instanceof EnergyPipe)
                .mapToInt(p -> ((EnergyPipe) p).getType().getCapacity())
                .sum()
        );

        if (energyStorage.getEnergyStored() > energyStorage.getMaxEnergyStored()) {
            energyStorage.setStored(energyStorage.getMaxEnergyStored());
        }

        return result;
    }

    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public void update(Level level) {
        super.update(level);

        List<Destination> destinations = graph.getDestinations(DestinationType.ENERGY_STORAGE);

        if (!destinations.isEmpty()) {
            if (energyStorage.getEnergyStored() <= 0) {
                return;
            }

            for (Destination destination : destinations) {
                BlockEntity blockEntity = destination.getConnectedPipe().getLevel().getBlockEntity(destination.getReceiver());
                if (blockEntity == null) {
                    continue;
                }

                IEnergyStorage handler = blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, destination.getReceiver(), destination.getIncomingDirection().getOpposite());
                if (handler == null) {
                    continue;
                }

                if (!handler.canReceive()) {
                    continue;
                }

                int toOffer = Math.min(pipeType.getTransferRate(), energyStorage.getEnergyStored());
                if (toOffer <= 0) {
                    break;
                }

                toOffer = energyStorage.extractEnergy(toOffer, false);
                if (toOffer <= 0) {
                    break;
                }

                int accepted = handler.receiveEnergy(toOffer, false);

                int remainder = toOffer - accepted;
                if (remainder > 0) {
                    energyStorage.receiveEnergy(remainder, false);
                }
            }
        }
    }

    @Override
    public void onMergedWith(Network mainNetwork) {
        ((EnergyNetwork) mainNetwork).energyStorage.receiveEnergy(energyStorage.getEnergyStored(), false);
    }

    @Override
    public ResourceLocation getType() {
        return pipeType.getNetworkType();
    }

    public EnergyPipeType getPipeType() {
        return pipeType;
    }
} 