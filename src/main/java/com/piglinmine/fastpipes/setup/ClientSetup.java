package com.piglinmine.fastpipes.setup;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.render.FluidPipeBlockEntityRenderer;
import com.piglinmine.fastpipes.render.ItemPipeBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO 1.21.11: Custom pipe model loading was disabled. ModelEvent.RegisterAdditional,
// ModelEvent.ModifyBakingResult, BakedModel and ModelResourceLocation moved/were removed
// in the 1.21.11 model overhaul. The previous handlers (registerSpecialModels,
// onModelBake, createPipeBakedModel, isPipeModel) and the PipeBakedModel install path
// have been removed. Pipes will render with their JSON block models only until the
// custom pipe-shape baked-model logic is ported to the new model API.
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
}
