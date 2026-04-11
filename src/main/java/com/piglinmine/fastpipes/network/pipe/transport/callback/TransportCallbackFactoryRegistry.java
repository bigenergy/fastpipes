package com.piglinmine.fastpipes.network.pipe.transport.callback;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TransportCallbackFactoryRegistry {
    public static final TransportCallbackFactoryRegistry INSTANCE = new TransportCallbackFactoryRegistry();
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<ResourceLocation, TransportCallbackFactory> factories = new HashMap<>();
    private boolean initialized = false;

    private TransportCallbackFactoryRegistry() {
    }

    public void initialize() {
        if (initialized) {
            return;
        }

        addFactory(ItemInsertTransportCallback.ID, new ItemInsertTransportCallbackFactory());
        addFactory(ItemPipeGoneTransportCallback.ID, new ItemPipeGoneTransportCallbackFactory());
        addFactory(ItemBounceBackTransportCallback.ID, new ItemBounceBackTransportCallbackFactory());

        initialized = true;
        LOGGER.info("Initialized transport callback factories");
    }

    public void addFactory(ResourceLocation id, TransportCallbackFactory factory) {
        if (factories.containsKey(id)) {
            throw new RuntimeException("Cannot register duplicate transport callback factory " + id.toString());
        }

        factories.put(id, factory);
    }

    @Nullable
    public TransportCallbackFactory getFactory(ResourceLocation id) {
        return factories.get(id);
    }

    @Nullable
    public static TransportCallback createCallback(ResourceLocation id, CompoundTag tag, HolderLookup.Provider registries) {
        TransportCallbackFactory factory = INSTANCE.getFactory(id);
        if (factory == null) {
            LOGGER.warn("Transport callback factory " + id + " no longer exists");
            return null;
        }

        TransportCallback callback = factory.create(tag, registries);
        if (callback == null) {
            LOGGER.warn("Transport callback factory " + id + " returned null!");
            return null;
        }

        return callback;
    }
}
