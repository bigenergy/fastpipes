package com.piglinmine.fastpipes.integration.jei;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        LOGGER.debug("Finished registering Fast Pipes ingredient information with JEI");
        
        // Note: Vanilla crafting recipes should be automatically detected by JEI
        // if they are properly registered in the recipe manager.
        // The recipes are loaded from data/fastpipes/recipes/*.json files
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Register GUI handlers for JEI integration
        // This would be used for recipe transfer handlers, click areas, etc.
        // For now, we don't need special GUI handlers for Fast Pipes
    }
} 