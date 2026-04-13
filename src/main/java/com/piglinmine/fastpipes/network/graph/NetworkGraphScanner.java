package com.piglinmine.fastpipes.network.graph;
import com.piglinmine.fastpipes.util.CapabilityUtil;

import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class NetworkGraphScanner {
    private final Set<Pipe> foundPipes = new HashSet<>();
    private final Set<Pipe> newPipes = new HashSet<>();
    private final Set<Pipe> removedPipes = new HashSet<>();
    private final Set<Destination> destinations = new HashSet<>();
    private final Set<Pipe> currentPipes;
    private final ResourceLocation requiredNetworkType;

    private final List<NetworkGraphScannerRequest> allRequests = new ArrayList<>();
    private final Queue<NetworkGraphScannerRequest> requests = new ArrayDeque<>();

    public NetworkGraphScanner(Set<Pipe> currentPipes, ResourceLocation requiredNetworkType) {
        this.currentPipes = currentPipes;
        this.removedPipes.addAll(currentPipes);
        this.requiredNetworkType = requiredNetworkType;
    }

    public NetworkGraphScannerResult scanAt(Level level, BlockPos pos) {
        addRequest(new NetworkGraphScannerRequest(level, pos, null, null));

        NetworkGraphScannerRequest request;
        while ((request = requests.poll()) != null) {
            singleScanAt(request);
        }

        return new NetworkGraphScannerResult(
            foundPipes,
            newPipes,
            removedPipes,
            destinations,
            allRequests
        );
    }

    private void singleScanAt(NetworkGraphScannerRequest request) {
        Pipe pipe = NetworkManager.get(request.getLevel()).getPipe(request.getPos());

        if (pipe != null) {
            if (!requiredNetworkType.equals(pipe.getNetworkType())) {
                return;
            }

            if (foundPipes.add(pipe)) {
                if (!currentPipes.contains(pipe)) {
                    newPipes.add(pipe);
                }

                removedPipes.remove(pipe);

                request.setSuccessful(true);

                for (Direction dir : Direction.values()) {
                    // Skip disconnected sides - wrench can disconnect pipe sides
                    if (pipe.isDisconnected(dir)) {
                        continue;
                    }

                    // Color check: colored pipes only connect to same color or uncolored
                    // Uncolored pipes connect to everything
                    Pipe neighborPipe = NetworkManager.get(request.getLevel()).getPipe(request.getPos().relative(dir));
                    if (neighborPipe != null) {
                        DyeColor myColor = pipe.getColor();
                        DyeColor theirColor = neighborPipe.getColor();
                        if (myColor != null && theirColor != null && myColor != theirColor) {
                            continue;
                        }
                    }

                    addRequest(new NetworkGraphScannerRequest(
                        request.getLevel(),
                        request.getPos().relative(dir),
                        dir,
                        request
                    ));
                }
            }
        } else if (request.getParent() != null) {
            Pipe connectedPipe = NetworkManager.get(request.getLevel()).getPipe(request.getParent().getPos());

            // Attachments block destinations by default (e.g. extractors).
            // Inserter attachments explicitly mark their side as a valid destination.
            Attachment att = connectedPipe.getAttachmentManager().getAttachment(request.getDirection());

            // Void attachments act as destinations without requiring an adjacent block entity
            if (att != null && att.isVoidDestination()) {
                destinations.add(new Destination(DestinationType.ITEM_HANDLER, request.getPos(), request.getDirection(), connectedPipe));
                destinations.add(new Destination(DestinationType.FLUID_HANDLER, request.getPos(), request.getDirection(), connectedPipe));
            } else if (att == null || att.isItemDestinationProvider()) {
                BlockEntity blockEntity = request.getLevel().getBlockEntity(request.getPos());

                if (blockEntity != null) {
                    // Check for Item Handler capability
                    var itemHandler = CapabilityUtil.getItemHandler(request.getLevel(), request.getPos(), request.getDirection().getOpposite());
                    if (itemHandler != null) {
                        destinations.add(new Destination(DestinationType.ITEM_HANDLER, request.getPos(), request.getDirection(), connectedPipe));
                    }

                    // Check for Fluid Handler capability
                    var fluidHandler = CapabilityUtil.getFluidHandler(request.getLevel(), request.getPos(), request.getDirection().getOpposite());
                    if (fluidHandler != null) {
                        destinations.add(new Destination(DestinationType.FLUID_HANDLER, request.getPos(), request.getDirection(), connectedPipe));
                    }

                    // Check for Energy Storage capability
                    var energyStorage = CapabilityUtil.getEnergyStorage(request.getLevel(), request.getPos(), request.getDirection().getOpposite());
                    if (energyStorage != null && !(energyStorage instanceof EnergyPipeEnergyStorage)) {
                        destinations.add(new Destination(DestinationType.ENERGY_STORAGE, request.getPos(), request.getDirection(), connectedPipe));
                    }
                }
            }
        }
    }

    private void addRequest(NetworkGraphScannerRequest request) {
        requests.add(request);
        allRequests.add(request);
    }
} 