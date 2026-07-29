package com.piglinmine.fastpipes.network.item;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.Network;
import com.piglinmine.fastpipes.network.graph.NetworkGraphScannerResult;
import com.piglinmine.fastpipes.network.item.routing.DestinationPathCache;
import com.piglinmine.fastpipes.network.item.routing.DestinationPathCacheFactory;
import com.piglinmine.fastpipes.network.item.routing.EdgeFactory;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import com.piglinmine.fastpipes.routing.Edge;
import com.piglinmine.fastpipes.routing.Graph;
import com.piglinmine.fastpipes.routing.Node;
import com.piglinmine.fastpipes.routing.NodeIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemNetwork extends Network {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item");

    private DestinationPathCache destinationPathCache;

    /** Items dispatched toward a receiver but not yet inserted. Rebuilt every tick, never saved. */
    private final Map<BlockPos, Integer> pendingInserts = new HashMap<>();
    private long pendingInsertsTick = Long.MIN_VALUE;

    public ItemNetwork(BlockPos originPos, String id) {
        super(originPos, id);
    }

    @Override
    public NetworkGraphScannerResult scanGraph(Level level, BlockPos pos) {
        NetworkGraphScannerResult result = super.scanGraph(level, pos);

        updateRouting(result, graph.getDestinations(DestinationType.ITEM_HANDLER));

        return result;
    }

    @Override
    public void onMergedWith(Network mainNetwork) {

    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    private void updateRouting(NetworkGraphScannerResult result, List<Destination> destinations) {
        List<Node<BlockPos>> nodes = buildNodes(result.getFoundPipes());

        NodeIndex<BlockPos> nodeIndex = NodeIndex.of(nodes);

        EdgeFactory edgeFactory = new EdgeFactory(nodeIndex, result.getRequests());
        List<Edge<BlockPos>> edges = edgeFactory.create();

        Graph<BlockPos> graph = new Graph<>(nodes, edges);

        DestinationPathCacheFactory destinationPathCacheFactory = new DestinationPathCacheFactory(graph, nodeIndex, destinations);

        this.destinationPathCache = destinationPathCacheFactory.create();
    }

    private List<Node<BlockPos>> buildNodes(Set<Pipe> pipes) {
        return pipes.stream().map(p -> new Node<>(p.getPos())).collect(Collectors.toList());
    }

    /**
     * How many items are already on their way to {@code receiver}. Extractors must subtract
     * this from the free space a capability simulation reports: the simulation sees the
     * inventory as it is now, not as it will be once the in-flight items land. Without it an
     * extractor keeps dispatching against the same free slot every tick, and the surplus
     * bounces on arrival — dropping in the world when the source is output-only.
     */
    public int getPendingInsertCount(Level level, BlockPos receiver) {
        refreshPendingInserts(level);
        return pendingInserts.getOrDefault(receiver, 0);
    }

    /**
     * Book {@code count} items against {@code receiver} for the rest of this tick. The transport
     * that was just created won't be visible to a rebuild until the next tick, so extractors
     * looping over several slots would otherwise each see the same free space.
     */
    public void reservePendingInsert(Level level, BlockPos receiver, int count) {
        refreshPendingInserts(level);
        pendingInserts.merge(receiver, count, Integer::sum);
    }

    /**
     * Recomputed from the live transports rather than tracked incrementally: transports are
     * dropped on several paths (arrival, bounce, pipe removed, chunk unload, world reload,
     * network split/merge) and a missed decrement would permanently wedge a destination.
     * Rebuilding from ground truth once per tick cannot drift, and also heals the intra-tick
     * bookings made by {@link #reservePendingInsert}.
     */
    private void refreshPendingInserts(Level level) {
        long now = level.getGameTime();
        if (pendingInsertsTick == now) {
            return;
        }
        pendingInsertsTick = now;
        pendingInserts.clear();

        for (Pipe pipe : getPipes()) {
            if (!(pipe instanceof ItemPipe itemPipe)) {
                continue;
            }
            for (ItemTransport transport : itemPipe.getTransports()) {
                pendingInserts.merge(transport.getDestination(), transport.getValue().getCount(), Integer::sum);
            }
            for (ItemTransport transport : itemPipe.getQueuedTransports()) {
                pendingInserts.merge(transport.getDestination(), transport.getValue().getCount(), Integer::sum);
            }
        }
    }

    public DestinationPathCache getDestinationPathCache() {
        return destinationPathCache;
    }
} 