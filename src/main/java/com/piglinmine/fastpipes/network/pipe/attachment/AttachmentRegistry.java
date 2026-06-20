package com.piglinmine.fastpipes.network.pipe.attachment;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AttachmentRegistry {
    public static final AttachmentRegistry INSTANCE = new AttachmentRegistry();

    private final Map<Identifier, AttachmentFactory> factories = new HashMap<>();

    private AttachmentRegistry() {
    }

    public Collection<AttachmentFactory> all() {
        return factories.values();
    }

    public void addFactory(Identifier id, AttachmentFactory type) {
        if (factories.containsKey(id)) {
            throw new RuntimeException("Cannot register duplicate attachment factory " + id.toString());
        }

        factories.put(id, type);
    }

    @Nullable
    public AttachmentFactory getFactory(Identifier id) {
        return factories.get(id);
    }
} 