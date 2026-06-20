package com.piglinmine.fastpipes.network.pipe;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PipeRegistry {
    public static final PipeRegistry INSTANCE = new PipeRegistry();

    private final Map<Identifier, PipeFactory> factories = new HashMap<>();

    private PipeRegistry() {
    }

    public void addFactory(Identifier id, PipeFactory factory) {
        if (factories.containsKey(id)) {
            throw new RuntimeException("Cannot register duplicate pipe factory " + id.toString());
        }

        factories.put(id, factory);
    }

    @Nullable
    public PipeFactory getFactory(Identifier id) {
        return factories.get(id);
    }
} 