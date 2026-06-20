package com.piglinmine.fastpipes.setup;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.render.FluidPipeBlockEntityRenderer;
import com.piglinmine.fastpipes.render.ItemPipeBlockEntityRenderer;
import com.piglinmine.fastpipes.render.PipeBakedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientSetup {
    private static final Logger LOGGER = LogManager.getLogger(ClientSetup.class);

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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

    /**
     * Register the custom dynamic pipe block-state model under {@code fastpipes:pipe}.
     * Pipe blockstate JSONs reference this with {@code "type": "fastpipes:pipe"}.
     */
    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        LOGGER.debug("Registering custom block state model {}", PipeBakedModel.MODEL_TYPE);
        event.registerModel(PipeBakedModel.MODEL_TYPE, PipeBakedModel.Unbaked.MAP_CODEC);
    }
}
