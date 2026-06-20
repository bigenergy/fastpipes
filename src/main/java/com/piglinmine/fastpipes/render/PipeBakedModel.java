package com.piglinmine.fastpipes.render;

import net.minecraft.resources.Identifier;

import java.util.Map;

// TODO 1.21.11: BakedModel, ItemOverrides, ModelData, ModelProperty, ChunkRenderTypeSet,
// QuadTransformers and the old per-quad model pipeline were removed/relocated in 1.21.11.
// This class was a full BakedModel implementation; it has been stubbed to a plain wrapper
// that only stores the constructor inputs so call sites still compile. All rendering logic
// (quad generation, attachment models, tinting, post-processing) needs to be ported to the
// new model/render-state architecture.
public class PipeBakedModel {
    // TODO 1.21.11: BakedModel no longer exists in this package — fields typed as Object
    // until the new model API is wired up.
    private final Object core;
    private final Object extension;
    private final Object straight;
    private final Object inventoryAttachment;
    private final Map<Identifier, Object> attachmentModels;

    public PipeBakedModel(Object core, Object extension, Object straight, Object inventoryAttachment, Map<Identifier, Object> attachmentModels) {
        this.core = core;
        this.extension = extension;
        this.straight = straight;
        this.inventoryAttachment = inventoryAttachment;
        this.attachmentModels = attachmentModels;
    }
}
