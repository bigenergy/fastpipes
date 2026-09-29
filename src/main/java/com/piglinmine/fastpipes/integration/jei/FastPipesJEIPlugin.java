package com.piglinmine.fastpipes.integration.jei;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import com.piglinmine.fastpipes.screen.ExtractorAttachmentScreen;
import com.piglinmine.fastpipes.screen.InserterAttachmentScreen;
import com.piglinmine.fastpipes.screen.SensorAttachmentScreen;
import com.piglinmine.fastpipes.screen.VoidAttachmentScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class FastPipesJEIPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger(FastPipesJEIPlugin.class);
    
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Register any item subtypes if needed
        // For now, Refined Pipes doesn't need special item subtypes
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        // Register extensions for vanilla categories if needed
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        // JEI only lists fluids that report themselves as a source, which leaves out
        // fluids that cannot be placed in the world and have no bucket item - the
        // Iron's Spells inks being the motivating case. Register every other
        // non-empty fluid so they show up in JEI and can be dragged onto a filter.
        List<FluidStack> fluids = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == Fluids.EMPTY || fluid == Fluids.FLOWING_WATER || fluid == Fluids.FLOWING_LAVA) {
                continue;
            }
            fluids.add(new FluidStack(fluid, 1000));
        }

        if (!fluids.isEmpty()) {
            registration.addExtraIngredients(NeoForgeTypes.FLUID_STACK, fluids);
            LOGGER.debug("Registered {} extra fluid ingredients with JEI", fluids.size());
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        LOGGER.debug("Registering Fast Pipes ingredient information with JEI");
        
        // Add information about our items to JEI using the correct API
        // According to JEI documentation, addIngredientInfo expects ItemLike and Component...
        
        // Add all our pipe items to JEI's ingredient list
        registration.addIngredientInfo(FPipesItems.BASIC_ITEM_PIPE.get(),
            Component.translatable("jei.fastpipes.basic_item_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.IMPROVED_ITEM_PIPE.get(),
            Component.translatable("jei.fastpipes.improved_item_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ADVANCED_ITEM_PIPE.get(),
            Component.translatable("jei.fastpipes.advanced_item_pipe.description"));

        registration.addIngredientInfo(FPipesItems.BASIC_FLUID_PIPE.get(),
            Component.translatable("jei.fastpipes.basic_fluid_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.IMPROVED_FLUID_PIPE.get(),
            Component.translatable("jei.fastpipes.improved_fluid_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ADVANCED_FLUID_PIPE.get(),
            Component.translatable("jei.fastpipes.advanced_fluid_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ELITE_FLUID_PIPE.get(),
            Component.translatable("jei.fastpipes.elite_fluid_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ULTIMATE_FLUID_PIPE.get(),
            Component.translatable("jei.fastpipes.ultimate_fluid_pipe.description"));

        registration.addIngredientInfo(FPipesItems.BASIC_ENERGY_PIPE.get(),
            Component.translatable("jei.fastpipes.basic_energy_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.IMPROVED_ENERGY_PIPE.get(),
            Component.translatable("jei.fastpipes.improved_energy_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ADVANCED_ENERGY_PIPE.get(),
            Component.translatable("jei.fastpipes.advanced_energy_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ELITE_ENERGY_PIPE.get(),
            Component.translatable("jei.fastpipes.elite_energy_pipe.description"));
            
        registration.addIngredientInfo(FPipesItems.ULTIMATE_ENERGY_PIPE.get(),
            Component.translatable("jei.fastpipes.ultimate_energy_pipe.description"));

        // Add extractor attachments
        registration.addIngredientInfo(FPipesItems.BASIC_EXTRACTOR_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.basic_extractor_attachment.description"));
            
        registration.addIngredientInfo(FPipesItems.IMPROVED_EXTRACTOR_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.improved_extractor_attachment.description"));
            
        registration.addIngredientInfo(FPipesItems.ADVANCED_EXTRACTOR_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.advanced_extractor_attachment.description"));
            
        registration.addIngredientInfo(FPipesItems.ELITE_EXTRACTOR_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.elite_extractor_attachment.description"));
            
        registration.addIngredientInfo(FPipesItems.ULTIMATE_EXTRACTOR_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.ultimate_extractor_attachment.description"));

        // Add inserter attachments
        registration.addIngredientInfo(FPipesItems.BASIC_INSERTER_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.basic_inserter_attachment.description"));

        registration.addIngredientInfo(FPipesItems.IMPROVED_INSERTER_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.improved_inserter_attachment.description"));

        registration.addIngredientInfo(FPipesItems.ADVANCED_INSERTER_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.advanced_inserter_attachment.description"));

        registration.addIngredientInfo(FPipesItems.ELITE_INSERTER_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.elite_inserter_attachment.description"));

        registration.addIngredientInfo(FPipesItems.ULTIMATE_INSERTER_ATTACHMENT.get(),
            Component.translatable("jei.fastpipes.ultimate_inserter_attachment.description"));

        // Wrench
        registration.addIngredientInfo(FPipesItems.WRENCH.get(),
            Component.translatable("jei.fastpipes.wrench.description"));

        LOGGER.debug("Finished registering Fast Pipes ingredient information with JEI");
        
        // Note: Vanilla crafting recipes should be automatically detected by JEI
        // if they are properly registered in the recipe manager.
        // The recipes are loaded from data/fastpipes/recipes/*.json files
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new TerminalRecipeTransferHandler(), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Let fluids (and fluid containers) be dragged from JEI onto the fluid
        // filter slots of the attachment screens.
        registration.addGhostIngredientHandler(ExtractorAttachmentScreen.class, new FluidFilterGhostIngredientHandler<>());
        registration.addGhostIngredientHandler(InserterAttachmentScreen.class, new FluidFilterGhostIngredientHandler<>());
        registration.addGhostIngredientHandler(VoidAttachmentScreen.class, new FluidFilterGhostIngredientHandler<>());
        registration.addGhostIngredientHandler(SensorAttachmentScreen.class, new FluidFilterGhostIngredientHandler<>());
    }
} 