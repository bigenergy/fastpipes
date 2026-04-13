package com.piglinmine.fastpipes.integration.jei;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.FPipesItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JeiPlugin
public class FastPipesJEIPlugin implements IModPlugin {
    private static final Logger LOGGER = LogManager.getLogger(FastPipesJEIPlugin.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FastPipes.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
    }

    private static void addInfo(IRecipeRegistration reg, ItemLike item, String key) {
        reg.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM_STACK, Component.translatable(key));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        LOGGER.debug("Registering Fast Pipes ingredient information with JEI");

        addInfo(registration, FPipesItems.BASIC_ITEM_PIPE.get(), "jei.fastpipes.basic_item_pipe.description");
        addInfo(registration, FPipesItems.IMPROVED_ITEM_PIPE.get(), "jei.fastpipes.improved_item_pipe.description");
        addInfo(registration, FPipesItems.ADVANCED_ITEM_PIPE.get(), "jei.fastpipes.advanced_item_pipe.description");

        addInfo(registration, FPipesItems.BASIC_FLUID_PIPE.get(), "jei.fastpipes.basic_fluid_pipe.description");
        addInfo(registration, FPipesItems.IMPROVED_FLUID_PIPE.get(), "jei.fastpipes.improved_fluid_pipe.description");
        addInfo(registration, FPipesItems.ADVANCED_FLUID_PIPE.get(), "jei.fastpipes.advanced_fluid_pipe.description");
        addInfo(registration, FPipesItems.ELITE_FLUID_PIPE.get(), "jei.fastpipes.elite_fluid_pipe.description");
        addInfo(registration, FPipesItems.ULTIMATE_FLUID_PIPE.get(), "jei.fastpipes.ultimate_fluid_pipe.description");

        addInfo(registration, FPipesItems.BASIC_ENERGY_PIPE.get(), "jei.fastpipes.basic_energy_pipe.description");
        addInfo(registration, FPipesItems.IMPROVED_ENERGY_PIPE.get(), "jei.fastpipes.improved_energy_pipe.description");
        addInfo(registration, FPipesItems.ADVANCED_ENERGY_PIPE.get(), "jei.fastpipes.advanced_energy_pipe.description");
        addInfo(registration, FPipesItems.ELITE_ENERGY_PIPE.get(), "jei.fastpipes.elite_energy_pipe.description");
        addInfo(registration, FPipesItems.ULTIMATE_ENERGY_PIPE.get(), "jei.fastpipes.ultimate_energy_pipe.description");

        addInfo(registration, FPipesItems.BASIC_EXTRACTOR_ATTACHMENT.get(), "jei.fastpipes.basic_extractor_attachment.description");
        addInfo(registration, FPipesItems.IMPROVED_EXTRACTOR_ATTACHMENT.get(), "jei.fastpipes.improved_extractor_attachment.description");
        addInfo(registration, FPipesItems.ADVANCED_EXTRACTOR_ATTACHMENT.get(), "jei.fastpipes.advanced_extractor_attachment.description");
        addInfo(registration, FPipesItems.ELITE_EXTRACTOR_ATTACHMENT.get(), "jei.fastpipes.elite_extractor_attachment.description");
        addInfo(registration, FPipesItems.ULTIMATE_EXTRACTOR_ATTACHMENT.get(), "jei.fastpipes.ultimate_extractor_attachment.description");

        addInfo(registration, FPipesItems.BASIC_INSERTER_ATTACHMENT.get(), "jei.fastpipes.basic_inserter_attachment.description");
        addInfo(registration, FPipesItems.IMPROVED_INSERTER_ATTACHMENT.get(), "jei.fastpipes.improved_inserter_attachment.description");
        addInfo(registration, FPipesItems.ADVANCED_INSERTER_ATTACHMENT.get(), "jei.fastpipes.advanced_inserter_attachment.description");
        addInfo(registration, FPipesItems.ELITE_INSERTER_ATTACHMENT.get(), "jei.fastpipes.elite_inserter_attachment.description");
        addInfo(registration, FPipesItems.ULTIMATE_INSERTER_ATTACHMENT.get(), "jei.fastpipes.ultimate_inserter_attachment.description");

        addInfo(registration, FPipesItems.WRENCH.get(), "jei.fastpipes.wrench.description");

        LOGGER.debug("Finished registering Fast Pipes ingredient information with JEI");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new TerminalRecipeTransferHandler(), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    }
}
