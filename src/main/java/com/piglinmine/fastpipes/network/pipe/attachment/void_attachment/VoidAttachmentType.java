package com.piglinmine.fastpipes.network.pipe.attachment.void_attachment;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public enum VoidAttachmentType {
    INSTANCE;

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "void");
    }

    public Identifier getItemId() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "void_attachment");
    }

    public Identifier getModelLocation() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/void");
    }

    public Item getItem() {
        return FPipesItems.VOID_ATTACHMENT.get();
    }

    public VoidAttachmentFactory getFactory() {
        return new VoidAttachmentFactory(this);
    }
}
