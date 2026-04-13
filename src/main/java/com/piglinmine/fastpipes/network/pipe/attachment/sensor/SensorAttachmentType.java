package com.piglinmine.fastpipes.network.pipe.attachment.sensor;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public enum SensorAttachmentType {
    INSTANCE;

    public ResourceLocation getId() {
        return new ResourceLocation(FastPipes.MOD_ID, "sensor");
    }

    public ResourceLocation getItemId() {
        return new ResourceLocation(FastPipes.MOD_ID, "sensor_attachment");
    }

    public ResourceLocation getModelLocation() {
        return new ResourceLocation(FastPipes.MOD_ID, "block/pipe/attachment/sensor");
    }

    public Item getItem() {
        return FPipesItems.SENSOR_ATTACHMENT.get();
    }

    public SensorAttachmentFactory getFactory() {
        return new SensorAttachmentFactory(this);
    }
}
