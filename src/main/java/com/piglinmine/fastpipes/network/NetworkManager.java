package com.piglinmine.fastpipes.network;

import com.mojang.serialization.Codec;
import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.PipeFactory;
import com.piglinmine.fastpipes.network.pipe.PipeRegistry;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;

public class NetworkManager extends SavedData {
    private static final String NAME = FastPipes.MOD_ID + "_networks";
    private static final Logger LOGGER = LogManager.getLogger(NetworkManager.class);
    private final Level level;
    private final Map<String, Network> networks = new HashMap<>();
    private final Map<BlockPos, Pipe> pipes = new HashMap<>();

    public NetworkManager(Level level) {
        this.level = level;
    }

    public static NetworkManager get(Level level) {
        return get((ServerLevel) level);
    }

    /**
     * SavedDataType lives per-level — built lazily so the NetworkManager carries the level
     * reference into network/pipe deserialization paths that still expect a Level.
     */
    public static SavedDataType<NetworkManager> savedDataType(ServerLevel level) {
        return new SavedDataType<>(
            NAME,
            srv -> new NetworkManager(srv),
            srv -> codec(srv),
            DataFixTypes.LEVEL
        );
    }

    public static NetworkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(savedDataType(level));
    }

    /**
     * Builds the {@link Codec} used by the {@link SavedDataType} — round-trips through the
     * pre-existing {@link #load(CompoundTag, HolderLookup.Provider)} / {@link #save(CompoundTag, HolderLookup.Provider)}
     * CompoundTag format, so all pipe/network factories continue to use their established NBT
     * schemas without per-class refactoring.
     */
    private static Codec<NetworkManager> codec(ServerLevel level) {
        return CompoundTag.CODEC.xmap(
            tag -> {
                NetworkManager mgr = new NetworkManager(level);
                mgr.load(tag, level.registryAccess());
                return mgr;
            },
            mgr -> mgr.save(new CompoundTag(), level.registryAccess())
        );
    }

    public void addNetwork(Network network) {
        if (networks.containsKey(network.getId())) {
            throw new RuntimeException("Duplicate network " + network.getId());
        }

        networks.put(network.getId(), network);

        LOGGER.debug("Network {} created", network.getId());

        setDirty();
    }

    public void removeNetwork(String id) {
        if (!networks.containsKey(id)) {
            LOGGER.warn("Network {} not found, skipping removal", id);
            return;
        }

        networks.remove(id);

        LOGGER.debug("Network {} removed", id);

        setDirty();
    }

    private void formNetworkAt(Level level, BlockPos pos, Identifier type) {
        Network network = NetworkRegistry.INSTANCE.getFactory(type).create(pos);

        addNetwork(network);

        network.scanGraph(level, pos);
    }

    public void addPipe(Pipe pipe) {
        if (pipes.containsKey(pipe.getPos())) {
            throw new RuntimeException("Pipe at " + pipe.getPos() + " already exists");
        }

        pipes.put(pipe.getPos(), pipe);

        LOGGER.debug("Pipe added at {}", pipe.getPos());

        setDirty();

        tryConnectPipe(pipe);
    }

    private void tryConnectPipe(Pipe pipe) {
        List<Pipe> adjacentPipes = findAdjacentPipes(pipe.getPos(), pipe.getNetworkType());

        if (adjacentPipes.isEmpty()) {
            formNetworkAt(pipe.getLevel(), pipe.getPos(), pipe.getNetworkType());
        } else {
            mergeNetworksIntoOne(adjacentPipes, pipe.getLevel(), pipe.getPos());
        }
    }

    private void mergeNetworksIntoOne(List<Pipe> candidates, Level level, BlockPos pos) {
        if (candidates.isEmpty()) {
            throw new RuntimeException("Cannot merge networks: no candidates");
        }

        Set<Network> networkCandidates = new HashSet<>();

        for (Pipe candidate : candidates) {
            if (candidate.getNetwork() == null) {
                LOGGER.warn("Pipe at {} has no network, will be handled by network update", candidate.getPos());
                // Create a temporary network for this pipe if it doesn't have one
                formNetworkAt(candidate.getLevel(), candidate.getPos(), candidate.getNetworkType());
            }

            if (candidate.getNetwork() != null) {
                networkCandidates.add(candidate.getNetwork());
            }
        }

        if (networkCandidates.isEmpty()) {
            // No networks found, create a new one
            formNetworkAt(level, pos, candidates.get(0).getNetworkType());
            return;
        }

        Iterator<Network> networks = networkCandidates.iterator();
        Network mainNetwork = networks.next();

        Set<Network> mergedNetworks = new HashSet<>();

        while (networks.hasNext()) {
            Network otherNetwork = networks.next();

            boolean canMerge = mainNetwork.getType().equals(otherNetwork.getType());

            if (canMerge) {
                mergedNetworks.add(otherNetwork);
                removeNetwork(otherNetwork.getId());
            }
        }

        mainNetwork.scanGraph(level, pos);
        mergedNetworks.forEach(n -> n.onMergedWith(mainNetwork));
    }

    private List<Pipe> findAdjacentPipes(BlockPos pos, Identifier networkType) {
        List<Pipe> adjacentPipes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            Pipe pipe = getPipe(pos.relative(dir));

            if (pipe != null && pipe.getNetworkType().equals(networkType)) {
                adjacentPipes.add(pipe);
            }
        }

        return adjacentPipes;
    }

    public void removePipe(BlockPos pos) {
        Pipe pipe = getPipe(pos);
        if (pipe == null) {
            throw new RuntimeException("Pipe at " + pos + " was not found");
        }

        if (pipe.getNetwork() == null) {
            LOGGER.warn("Removed pipe at {} has no associated network", pipe.getPos());
        }

        pipes.remove(pipe.getPos());

        LOGGER.debug("Pipe removed at {}", pipe.getPos());

        setDirty();

        if (pipe.getNetwork() != null) {
            splitNetworks(pipe);
        }
    }

    private void splitNetworks(Pipe originPipe) {
        Set<Network> affectedNetworks = new HashSet<>();
        if (originPipe.getNetwork() != null) {
            affectedNetworks.add(originPipe.getNetwork());
        }

        List<Pipe> adjacentPipes = findAdjacentPipes(originPipe.getPos(), originPipe.getNetworkType());

        for (Pipe adjacent : adjacentPipes) {
            if (adjacent.getNetwork() != null) {
                affectedNetworks.add(adjacent.getNetwork());
            }
        }

        if (adjacentPipes.isEmpty()) {
            if (originPipe.getNetwork() != null) {
                removeNetwork(originPipe.getNetwork().getId());
            }
            return;
        }

        for (Network net : affectedNetworks) {
            removeNetwork(net.getId());
        }

        for (Pipe adjacentPipe : adjacentPipes) {
            if (adjacentPipe.getNetwork() != null) {
                adjacentPipe.leaveNetwork();
            }
        }

        for (Pipe adjacentPipe : adjacentPipes) {
            if (adjacentPipe.getNetwork() == null) {
                formNetworkAt(adjacentPipe.getLevel(), adjacentPipe.getPos(), adjacentPipe.getNetworkType());
            }
        }
    }

    @Nullable
    public Pipe getPipe(BlockPos pos) {
        return pipes.get(pos);
    }

    public Collection<Network> getNetworks() {
        return networks.values();
    }

    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag pipes = tag.getListOrEmpty("pipes");
        for (Tag pipeTag : pipes) {
            CompoundTag pipeTagCompound = (CompoundTag) pipeTag;

            // @BC
            Identifier factoryId = pipeTagCompound.contains("id") ? Identifier.parse(pipeTagCompound.getStringOr("id", "")) : ItemPipe.ID;

            PipeFactory factory = PipeRegistry.INSTANCE.getFactory(factoryId);
            if (factory == null) {
                LOGGER.warn("Pipe {} no longer exists", factoryId.toString());
                continue;
            }

            Pipe pipe = factory.createFromNbt(level, pipeTagCompound);

            this.pipes.put(pipe.getPos(), pipe);
        }

        ListTag nets = tag.getListOrEmpty("networks");
        for (Tag netTag : nets) {
            CompoundTag netTagCompound = (CompoundTag) netTag;
            if (!netTagCompound.contains("type")) {
                LOGGER.warn("Skipping network without type");
                continue;
            }

            Identifier type = Identifier.parse(netTagCompound.getStringOr("type", ""));

            NetworkFactory factory = NetworkRegistry.INSTANCE.getFactory(type);
            if (factory == null) {
                LOGGER.warn("Unknown network type {}", type.toString());
                continue;
            }

            Network network = factory.create(netTagCompound, provider);

            networks.put(network.getId(), network);
        }

        LOGGER.debug("Read {} pipes", pipes.size());
        LOGGER.debug("Read {} networks", networks.size());
    }

    /**
     * Serializes the network manager to a CompoundTag. Re-implemented for the SavedDataType
     * codec path — writes every pipe via its factory NBT and every network via its writeToNbt.
     */
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag pipesList = new ListTag();
        for (Pipe pipe : pipes.values()) {
            CompoundTag pipeTag = new CompoundTag();
            pipe.writeToNbt(pipeTag);
            pipesList.add(pipeTag);
        }
        tag.put("pipes", pipesList);

        ListTag nets = new ListTag();
        for (Network network : networks.values()) {
            CompoundTag netTag = new CompoundTag();
            netTag.putString("type", network.getType().toString());
            network.writeToNbt(netTag, provider);
            nets.add(netTag);
        }
        tag.put("networks", nets);

        return tag;
    }
}
