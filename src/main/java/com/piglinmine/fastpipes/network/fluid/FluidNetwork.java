package com.piglinmine.fastpipes.network.fluid;

import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.graph.NetworkGraphScannerResult;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FluidNetwork extends Network {
    private final FluidTank fluidTank = new FluidTank(1000); // Default capacity

    private final FluidPipeType pipeType;

    public FluidNetwork(BlockPos originPos, String id, FluidPipeType pipeType) {
        super(originPos, id);

        this.pipeType = pipeType;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    @Override
    public NetworkGraphScannerResult scanGraph(Level level, BlockPos pos) {
        NetworkGraphScannerResult result = super.scanGraph(level, pos);

        fluidTank.setCapacity(
            result.getFoundPipes()
                .stream()
                .filter(p -> p instanceof FluidPipe)
                .mapToInt(p -> ((FluidPipe) p).getType().getCapacity())
                .sum()
        );

        if (fluidTank.getFluidAmount() > fluidTank.getCapacity()) {
            fluidTank.getFluid().setAmount(fluidTank.getCapacity());
        }

        return result;
    }

    @Override
    public void update(Level level) {
        super.update(level);

        List<Destination> destinations = graph.getDestinations(DestinationType.FLUID_HANDLER);

        if (fluidTank.getFluid().isEmpty() || destinations.isEmpty()) {
            return;
        }

        // Sort destinations by priority — higher priority first, void (-1000) last
        List<Destination> sorted = new ArrayList<>(destinations);
        sorted.sort(Comparator.comparingInt((Destination d) -> {
            Attachment a = d.getConnectedPipe().getAttachmentManager().getAttachment(d.getIncomingDirection());
            return a != null ? a.getInsertionPriority() : 0;
        }).reversed());
        destinations = sorted;

        for (Destination destination : destinations) {
            // Check if the connected pipe has an inserter/void attachment that filters this fluid
            Attachment att = destination.getConnectedPipe().getAttachmentManager().getAttachment(destination.getIncomingDirection());
            if (att != null && !att.canAcceptFluid(fluidTank.getFluid())) {
                continue;
            }

            // Void attachment — drain fluid and destroy it
            if (att != null && att.isVoidDestination()) {
                int toOfferAmount = Math.min(pipeType.getTransferRate(), fluidTank.getFluidAmount());
                if (toOfferAmount <= 0) {
                    break;
                }
                fluidTank.drain(toOfferAmount, IFluidHandler.FluidAction.EXECUTE);
                continue;
            }

            BlockEntity blockEntity = destination.getConnectedPipe().getLevel().getBlockEntity(destination.getReceiver());
            if (blockEntity == null) {
                continue;
            }

            IFluidHandler handler = blockEntity.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, destination.getReceiver(), destination.getIncomingDirection().getOpposite());
            if (handler == null) {
                continue;
            }

            int toOfferAmount = Math.min(pipeType.getTransferRate(), fluidTank.getFluidAmount());
            if (toOfferAmount <= 0) {
                break;
            }

            FluidStack toOffer = fluidTank.drain(toOfferAmount, IFluidHandler.FluidAction.EXECUTE);
            if (toOffer.isEmpty()) {
                break;
            }

            int accepted = handler.fill(toOffer, IFluidHandler.FluidAction.EXECUTE);

            int remainder = toOffer.getAmount() - accepted;
            if (remainder > 0) {
                FluidStack remainderStack = toOffer.copy();
                remainderStack.setAmount(remainder);

                fluidTank.fill(remainderStack, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public void onMergedWith(Network mainNetwork) {
        ((FluidNetwork) mainNetwork).getFluidTank().fill(fluidTank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public ResourceLocation getType() {
        return pipeType.getNetworkType();
    }

    public FluidPipeType getPipeType() {
        return pipeType;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag, HolderLookup.Provider provider) {
        tag = super.writeToNbt(tag, provider);
        tag.put("tank", fluidTank.writeToNBT(provider, new CompoundTag()));
        return tag;
    }
} 