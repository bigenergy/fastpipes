package com.piglinmine.fastpipes.network.pipe.attachment.extractor;

import com.piglinmine.fastpipes.network.item.ItemNetwork;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDestinationFinder {
    private final Attachment attachment;
    private int roundRobinIndex;

    public ItemDestinationFinder(Attachment attachment) {
        this.attachment = attachment;
    }

    public Destination find(RoutingMode routingMode, BlockPos sourcePos, ItemStack extracted) {
        ItemNetwork network = (ItemNetwork) attachment.getPipe().getNetwork();

        // Build the set of applicable destinations filtered by highest inserter priority.
        List<Destination> all = network.getDestinations(DestinationType.ITEM_HANDLER);
        List<Destination> applicable = buildApplicable(sourcePos, extracted, all);
        if (applicable.isEmpty()) {
            return null;
        }
        Set<Destination> applicableSet = new HashSet<>(applicable);

        switch (routingMode) {
            case NEAREST:
                return network.getDestinationPathCache()
                    .findNearestDestination(attachment.getPipe().getPos(), applicableSet::contains);
            case FURTHEST:
                return network.getDestinationPathCache()
                    .findFurthestDestination(attachment.getPipe().getPos(), applicableSet::contains);
            case RANDOM: {
                int idx = attachment.getPipe().getLevel().getRandom().nextInt(applicable.size());
                return applicable.get(idx);
            }
            case ROUND_ROBIN: {
                if (roundRobinIndex >= applicable.size()) {
                    roundRobinIndex = 0;
                }
                Destination dest = applicable.get(roundRobinIndex);
                roundRobinIndex++;
                return dest;
            }
            default:
                throw new RuntimeException("?");
        }
    }

    /**
     * Returns all applicable destinations filtered to only the highest-priority group.
     * Destinations guarded by a higher-priority inserter are preferred over lower-priority
     * or unguarded destinations.
     */
    private List<Destination> buildApplicable(BlockPos sourcePos, ItemStack extracted, List<Destination> all) {
        List<Destination> applicable = new ArrayList<>();
        for (Destination d : all) {
            if (isDestinationApplicable(sourcePos, extracted, d)) {
                applicable.add(d);
            }
        }
        if (applicable.size() <= 1) {
            return applicable;
        }

        int maxPriority = 0;
        for (Destination d : applicable) {
            maxPriority = Math.max(maxPriority, getDestinationPriority(d));
        }
        if (maxPriority == 0) {
            return applicable; // no priority differentiation
        }

        List<Destination> prioritized = new ArrayList<>();
        for (Destination d : applicable) {
            if (getDestinationPriority(d) >= maxPriority) {
                prioritized.add(d);
            }
        }
        return prioritized;
    }

    private int getDestinationPriority(Destination dest) {
        Attachment att = dest.getConnectedPipe().getAttachmentManager().getAttachment(dest.getIncomingDirection());
        return att != null ? att.getInsertionPriority() : 0;
    }

    private boolean isDestinationApplicable(BlockPos sourcePos, ItemStack extracted, Destination destination) {
        BlockEntity blockEntity = destination.getConnectedPipe().getLevel().getBlockEntity(destination.getReceiver());
        if (blockEntity == null) {
            return false;
        }

        IItemHandler handler = blockEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, destination.getReceiver(), destination.getIncomingDirection().getOpposite());
        if (handler == null) {
            return false;
        }

        // Avoid routing back to the source through the same side.
        if (destination.getReceiver().equals(sourcePos) && destination.getIncomingDirection() == attachment.getDirection()) {
            return false;
        }

        // Check inserter attachment filter on the destination side.
        Attachment att = destination.getConnectedPipe().getAttachmentManager().getAttachment(destination.getIncomingDirection());
        if (att != null && !att.canInsert(extracted)) {
            return false;
        }

        return ItemHandlerHelper.insertItem(handler, extracted, true).isEmpty();
    }

    public int getRoundRobinIndex() {
        return roundRobinIndex;
    }

    public void setRoundRobinIndex(int roundRobinIndex) {
        this.roundRobinIndex = roundRobinIndex;
    }
} 