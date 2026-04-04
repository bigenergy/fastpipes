package com.piglinmine.fastpipes.network.pipe.attachment.void_attachment;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public enum VoidAttachmentType {
    INSTANCE;

    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "void");
    }

    public ResourceLocation getItemId() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "void_attachment");
    }

    public ResourceLocation getModelLocation() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/void");
    }

    public Item getItem() {
        return FPipesItems.VOID_ATTACHMENT.get();
    }

    public VoidAttachmentFactory getFactory() {
        return new VoidAttachmentFactory(this);
    }
}
