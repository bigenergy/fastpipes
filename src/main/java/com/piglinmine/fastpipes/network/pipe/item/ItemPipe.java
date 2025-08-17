package com.piglinmine.fastpipes.network.pipe.item;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.item.ItemNetwork;
import com.piglinmine.fastpipes.network.message.ItemTransportMessage;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ItemPipe extends Pipe {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item");

    private final List<ItemTransport> transports = new ArrayList<>();
    private final List<ItemTransport> transportsToAdd = new ArrayList<>();
    private final List<ItemTransport> transportsToRemove = new ArrayList<>();
    private final ItemPipeType type;

    public ItemPipe(Level level, BlockPos pos, ItemPipeType type) {
        super(level, pos);
        this.type = type;
    }

    public ItemPipeType getType() {
        return type;
    }

    @Override
    public CompoundTag writeToNbt(CompoundTag tag) {
        super.writeToNbt(tag);
        tag.putInt("type", type.ordinal());

        ListTag transports = new ListTag();
        for (ItemTransport transport : this.transports) {
            transports.add(transport.writeToNbt(new CompoundTag(), level.registryAccess()));
        }
        tag.put("transports", transports);

        return tag;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        super.readFromNbt(tag);
        // Type is set in constructor, no need to read from NBT
        
        // TODO: Read transports from NBT when ItemTransport.of() is fully implemented
        // ListTag transports = tag.getList("transports", Tag.TAG_COMPOUND);
        // for (Tag transportTag : transports) {
        //     ItemTransport transport = ItemTransport.of((CompoundTag) transportTag);
        //     if (transport != null) {
        //         this.transports.add(transport);
        //     }
        // }
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ResourceLocation getNetworkType() {
        return ItemNetwork.TYPE;
    }

    @Override
    public void update() {
        super.update();

        boolean structureChanged = false;

        // Add new transports
        if (!transportsToAdd.isEmpty()) {
            transports.addAll(transportsToAdd);
            structureChanged = true;
        }

        // Remove finished transports
        if (!transportsToRemove.isEmpty()) {
            transports.removeAll(transportsToRemove);
            structureChanged = true;
        }

        // Only mark dirty and send updates if structure changed
        if (structureChanged) {
            NetworkManager.get(level).setDirty();
            sendTransportUpdate();
        }

        transportsToAdd.clear();
        transportsToRemove.clear();

        // Update existing transports - only mark dirty if a transport was actually removed
        if (transports.removeIf(t -> t.update(network, this))) {
            NetworkManager.get(level).setDirty();
            sendTransportUpdate();
        }
    }

    public List<ItemTransport> getTransports() {
        return transports;
    }

    public void addTransport(ItemTransport transport) {
        transportsToAdd.add(transport);
    }

    public void removeTransport(ItemTransport transport) {
        transportsToRemove.add(transport);
    }

    public void sendTransportUpdate() {
        List<ItemTransportProps> props = new ArrayList<>();
        for (ItemTransport transport : transports) {
            props.add(transport.createProps(this));
        }

        // Send transport update message to clients in area
        FastPipesNetwork.sendInArea(level, pos, 32, new ItemTransportMessage(pos, props));
    }

    public int getMaxTicksInPipe() {
        return type.getMaxTicksInPipe();
    }
} 