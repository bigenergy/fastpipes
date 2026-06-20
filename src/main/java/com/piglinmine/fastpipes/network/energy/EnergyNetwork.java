package com.piglinmine.fastpipes.network.energy;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.graph.NetworkGraphScannerResult;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyNetwork extends Network {
    private final EnergyStorage energyStorage;
    // Effective pipe type, recomputed on scanGraph as the slowest tier present.
    private EnergyPipeType pipeType;

    public EnergyNetwork(BlockPos originPos, String id, EnergyPipeType pipeType) {
        super(originPos, id);

        this.pipeType = pipeType;
        this.energyStorage = new EnergyStorage(0);
        this.energyStorage.setMaxReceive(pipeType.getTransferRate());
    }

    @Override
    public NetworkGraphScannerResult scanGraph(Level level, BlockPos pos) {
        NetworkGraphScannerResult result = super.scanGraph(level, pos);

        int totalCapacity = result.getFoundPipes()
            .stream()
            .filter(p -> p instanceof EnergyPipe)
            .mapToInt(p -> ((EnergyPipe) p).getType().getCapacity())
            .sum();

        // Effective tier kept around for getType()/legacy uses (the network's own
        // EnergyStorage maxReceive/maxExtract are no longer the throughput bottleneck —
        // each boundary pipe rate-limits I/O individually via EnergyPipeBlockEntity).
        // Use the FASTEST tier so the shared buffer's max-rate isn't an artificial cap.
        EnergyPipeType fastestType = result.getFoundPipes()
            .stream()
            .filter(p -> p instanceof EnergyPipe)
            .map(p -> ((EnergyPipe) p).getType())
            .max(java.util.Comparator.comparingInt(EnergyPipeType::getTier))
            .orElse(pipeType);
        this.pipeType = fastestType;

        energyStorage.setCapacity(totalCapacity);
        // Network-level rates effectively unbounded — boundary pipes do the limiting.
        energyStorage.setMaxReceive(Integer.MAX_VALUE);
        energyStorage.setMaxExtract(Integer.MAX_VALUE);

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

                // TODO 1.21.11: Capabilities.Energy.BLOCK now returns EnergyHandler not IEnergyStorage; wrap via IEnergyStorage.of()
                var capEnergyHandler = blockEntity.getLevel().getCapability(Capabilities.Energy.BLOCK, destination.getReceiver(), destination.getIncomingDirection().getOpposite());
                IEnergyStorage handler = capEnergyHandler == null ? null : IEnergyStorage.of(capEnergyHandler);
                if (handler == null) {
                    continue;
                }

                if (!handler.canReceive()) {
                    continue;
                }

                // Per-boundary-pipe throughput: the pipe directly adjacent to this receiver
                // determines how fast energy can flow out. A Basic outlet limits to 250 RF/t
                // even if the trunk is Ultimate; an Ultimate outlet runs at 32k regardless of
                // the rest of the network.
                int outputRate = (destination.getConnectedPipe() instanceof EnergyPipe ep)
                    ? ep.getType().getTransferRate()
                    : pipeType.getTransferRate();
                int toOffer = Math.min(outputRate, energyStorage.getEnergyStored());
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
    public Identifier getType() {
        return pipeType.getNetworkType();
    }

    public EnergyPipeType getPipeType() {
        return pipeType;
    }

    @Override
    public net.minecraft.nbt.CompoundTag writeToNbt(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        tag = super.writeToNbt(tag, provider);
        tag.putInt("energy", energyStorage.getEnergyStored());
        return tag;
    }
} 