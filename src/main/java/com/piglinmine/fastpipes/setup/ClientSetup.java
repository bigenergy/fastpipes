package com.piglinmine.fastpipes.setup;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentRegistry;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeType;
import com.piglinmine.fastpipes.render.FluidPipeBlockEntityRenderer;
import com.piglinmine.fastpipes.render.ItemPipeBlockEntityRenderer;
import com.piglinmine.fastpipes.render.PipeBakedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.ModelEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ClientSetup {
    private static final Logger LOGGER = LogManager.getLogger(ClientSetup.class);

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(FPipesBlockEntities.BASIC_ITEM_PIPE.get(), ItemPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.IMPROVED_ITEM_PIPE.get(), ItemPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.ADVANCED_ITEM_PIPE.get(), ItemPipeBlockEntityRenderer::new);

            BlockEntityRenderers.register(FPipesBlockEntities.BASIC_FLUID_PIPE.get(), FluidPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.IMPROVED_FLUID_PIPE.get(), FluidPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.ADVANCED_FLUID_PIPE.get(), FluidPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.ELITE_FLUID_PIPE.get(), FluidPipeBlockEntityRenderer::new);
            BlockEntityRenderers.register(FPipesBlockEntities.ULTIMATE_FLUID_PIPE.get(), FluidPipeBlockEntityRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerSpecialModels(ModelEvent.RegisterAdditional event) {
        LOGGER.debug("Registering special models for Fast Pipes");

        // In Forge 1.20.1, RegisterAdditional.register() takes plain ResourceLocation, NOT ModelResourceLocation
        for (AttachmentFactory factory : AttachmentRegistry.INSTANCE.all()) {
            LOGGER.debug("Registering attachment model {}", factory.getModelLocation());
            event.register(factory.getModelLocation());
        }

        for (String type : new String[]{"item", "fluid", "energy"}) {
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/core"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/extension"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/straight"));

            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/core"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/extension"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/straight"));

            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/core"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/extension"));
            event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/straight"));

            if (type.equals("fluid") || type.equals("energy")) {
                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/core"));
                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/extension"));
                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/straight"));

                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/core"));
                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/extension"));
                event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/straight"));
            }
        }

        event.register(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/attachment/inventory_attachment"));

        LOGGER.debug("Special models registered successfully");
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        LOGGER.debug("Modifying baking result for Fast Pipes");

        Map<ResourceLocation, BakedModel> attachmentModels = new HashMap<>();

        // Collect attachment models — use plain ResourceLocation as key (not MRL)
        for (AttachmentFactory factory : AttachmentRegistry.INSTANCE.all()) {
            BakedModel model = event.getModels().get(factory.getModelLocation());
            if (model != null) {
                attachmentModels.put(factory.getId(), model);
                LOGGER.debug("Collected attachment model for {}", factory.getId());
            } else {
                LOGGER.warn("Failed to find attachment model for {}", factory.getModelLocation());
            }
        }

        Map<ResourceLocation, PipeBakedModel> pipeModels = new HashMap<>();

        createPipeBakedModel(event, pipeModels, attachmentModels, ItemPipeType.BASIC.getId(), "item", "basic");
        createPipeBakedModel(event, pipeModels, attachmentModels, ItemPipeType.IMPROVED.getId(), "item", "improved");
        createPipeBakedModel(event, pipeModels, attachmentModels, ItemPipeType.ADVANCED.getId(), "item", "advanced");

        createPipeBakedModel(event, pipeModels, attachmentModels, FluidPipeType.BASIC.getId(), "fluid", "basic");
        createPipeBakedModel(event, pipeModels, attachmentModels, FluidPipeType.IMPROVED.getId(), "fluid", "improved");
        createPipeBakedModel(event, pipeModels, attachmentModels, FluidPipeType.ADVANCED.getId(), "fluid", "advanced");
        createPipeBakedModel(event, pipeModels, attachmentModels, FluidPipeType.ELITE.getId(), "fluid", "elite");
        createPipeBakedModel(event, pipeModels, attachmentModels, FluidPipeType.ULTIMATE.getId(), "fluid", "ultimate");

        createPipeBakedModel(event, pipeModels, attachmentModels, EnergyPipeType.BASIC.getId(), "energy", "basic");
        createPipeBakedModel(event, pipeModels, attachmentModels, EnergyPipeType.IMPROVED.getId(), "energy", "improved");
        createPipeBakedModel(event, pipeModels, attachmentModels, EnergyPipeType.ADVANCED.getId(), "energy", "advanced");
        createPipeBakedModel(event, pipeModels, attachmentModels, EnergyPipeType.ELITE.getId(), "energy", "elite");
        createPipeBakedModel(event, pipeModels, attachmentModels, EnergyPipeType.ULTIMATE.getId(), "energy", "ultimate");

        // Replace pipe block models in the model registry
        int replacedModels = 0;
        // We need to iterate over a copy of keys to avoid ConcurrentModificationException
        for (ResourceLocation key : new java.util.ArrayList<>(event.getModels().keySet())) {
            if (key instanceof ModelResourceLocation mrl) {
                for (Map.Entry<ResourceLocation, PipeBakedModel> entry : pipeModels.entrySet()) {
                    if (isPipeModel(mrl, entry.getKey())) {
                        event.getModels().put(key, entry.getValue());
                        replacedModels++;
                    }
                }
            }
        }

        LOGGER.info("Model baking modification completed - replaced {} pipe model variants", replacedModels);
    }

    private static void createPipeBakedModel(ModelEvent.ModifyBakingResult event, Map<ResourceLocation, PipeBakedModel> pipeModels,
                                             Map<ResourceLocation, BakedModel> attachmentModels, ResourceLocation pipeId, String type, String tier) {
        // In 1.20.1, additional models are stored under plain ResourceLocation keys
        BakedModel core = event.getModels().get(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/core"));
        BakedModel extension = event.getModels().get(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/extension"));
        BakedModel straight = event.getModels().get(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/straight"));
        BakedModel inventoryAttachment = event.getModels().get(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/attachment/inventory_attachment"));

        if (core != null && extension != null && straight != null && inventoryAttachment != null) {
            pipeModels.put(pipeId, new PipeBakedModel(core, extension, straight, inventoryAttachment, attachmentModels));
            LOGGER.debug("Created PipeBakedModel for {} {} {}", type, tier, pipeId);
        } else {
            LOGGER.warn("Failed to create PipeBakedModel for {} {} {} - missing models: core={}, extension={}, straight={}, inventoryAttachment={}",
                    type, tier, pipeId, core != null, extension != null, straight != null, inventoryAttachment != null);
        }
    }

    private static boolean isPipeModel(ModelResourceLocation modelId, ResourceLocation pipeId) {
        return modelId.getNamespace().equals(FastPipes.MOD_ID)
                && modelId.getPath().equals(pipeId.getPath())
                && !modelId.getVariant().equals("inventory");
    }
}
