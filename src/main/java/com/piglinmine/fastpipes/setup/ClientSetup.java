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
            // Render type is set via PipeBakedModel.getRenderTypes() — no need for setRenderLayer

            // Register block entity renderers
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
        
        // Register attachment models using ModelResourceLocation
        for (AttachmentFactory factory : AttachmentRegistry.INSTANCE.all()) {
            LOGGER.debug("Registering attachment model {}", factory.getModelLocation());
            event.register(new ModelResourceLocation(factory.getModelLocation(), "standalone"));
        }

        // Register pipe models for all types using ModelResourceLocation
        for (String type : new String[]{"item", "fluid", "energy"}) {
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/core"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/extension"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/basic/straight"), "standalone"));

            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/core"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/extension"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/improved/straight"), "standalone"));

            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/core"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/extension"), "standalone"));
            event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/advanced/straight"), "standalone"));

            if (type.equals("fluid") || type.equals("energy")) {
                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/core"), "standalone"));
                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/extension"), "standalone"));
                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/elite/straight"), "standalone"));

                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/core"), "standalone"));
                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/extension"), "standalone"));
                event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/ultimate/straight"), "standalone"));
            }
        }

        event.register(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/attachment/inventory_attachment"), "standalone"));
        
        LOGGER.debug("Special models registered successfully");
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        LOGGER.debug("Modifying baking result for Fast Pipes");
        
        Map<ResourceLocation, BakedModel> attachmentModels = new HashMap<>();

        // Collect attachment models
        for (AttachmentFactory factory : AttachmentRegistry.INSTANCE.all()) {
            BakedModel model = event.getModels().get(new ModelResourceLocation(factory.getModelLocation(), "standalone"));
            if (model != null) {
                attachmentModels.put(factory.getId(), model);
                LOGGER.debug("Collected attachment model for {}", factory.getId());
            } else {
                LOGGER.warn("Failed to find attachment model for {}", factory.getModelLocation());
            }
        }

        Map<ResourceLocation, PipeBakedModel> pipeModels = new HashMap<>();

        // Create pipe baked models
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

        // Replace pipe models in the registry
        int replacedModels = 0;
        for (ResourceLocation key : event.getModels().keySet()) {
            if (key instanceof ModelResourceLocation id) {
                for (Map.Entry<ResourceLocation, PipeBakedModel> entry : pipeModels.entrySet()) {
                    if (isPipeModel(id, entry.getKey())) {
                        event.getModels().put(id, entry.getValue());
                        replacedModels++;
                        // Only log individual replacements in trace mode to avoid spam
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Replaced pipe model for {}", id);
                        }
                    }
                }
            }
        }
        
        LOGGER.info("Model baking modification completed - replaced {} pipe model variants", replacedModels);
    }

    private static void createPipeBakedModel(ModelEvent.ModifyBakingResult event, Map<ResourceLocation, PipeBakedModel> pipeModels, 
                                           Map<ResourceLocation, BakedModel> attachmentModels, ResourceLocation pipeId, String type, String tier) {
        BakedModel core = event.getModels().get(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/core"), "standalone"));
        BakedModel extension = event.getModels().get(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/extension"), "standalone"));
        BakedModel straight = event.getModels().get(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/" + type + "/" + tier + "/straight"), "standalone"));
        BakedModel inventoryAttachment = event.getModels().get(new ModelResourceLocation(new ResourceLocation(FastPipes.MOD_ID, "block/pipe/attachment/inventory_attachment"), "standalone"));

        if (core != null && extension != null && straight != null && inventoryAttachment != null) {
            pipeModels.put(pipeId, new PipeBakedModel(core, extension, straight, inventoryAttachment, attachmentModels));
            LOGGER.trace("Created PipeBakedModel for {} {} {}", type, tier, pipeId);
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