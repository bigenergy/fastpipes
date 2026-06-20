package com.piglinmine.fastpipes.network.pipe.attachment.sensor;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public enum SensorAttachmentType {
    INSTANCE;

    public Identifier getId() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "sensor");
    }

    public Identifier getItemId() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "sensor_attachment");
    }

    public Identifier getModelLocation() {
        return Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "block/pipe/attachment/sensor");
    }

    public Item getItem() {
        return FPipesItems.SENSOR_ATTACHMENT.get();
    }

    public SensorAttachmentFactory getFactory() {
        return new SensorAttachmentFactory(this);
    }
}
