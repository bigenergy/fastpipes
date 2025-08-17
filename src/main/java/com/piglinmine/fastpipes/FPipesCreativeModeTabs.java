package com.piglinmine.fastpipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FPipesCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FastPipes.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + FastPipes.MOD_ID + ".main"))
            .icon(() -> new ItemStack(FPipesItems.BASIC_ITEM_PIPE.get()))
            .displayItems((params, output) -> {
                // Item Pipes
                output.accept(FPipesItems.BASIC_ITEM_PIPE.get());
                output.accept(FPipesItems.IMPROVED_ITEM_PIPE.get());
                output.accept(FPipesItems.ADVANCED_ITEM_PIPE.get());
                
                // Fluid Pipes
                output.accept(FPipesItems.BASIC_FLUID_PIPE.get());
                output.accept(FPipesItems.IMPROVED_FLUID_PIPE.get());
                output.accept(FPipesItems.ADVANCED_FLUID_PIPE.get());
                output.accept(FPipesItems.ELITE_FLUID_PIPE.get());
                output.accept(FPipesItems.ULTIMATE_FLUID_PIPE.get());
                
                // Energy Pipes
                output.accept(FPipesItems.BASIC_ENERGY_PIPE.get());
                output.accept(FPipesItems.IMPROVED_ENERGY_PIPE.get());
                output.accept(FPipesItems.ADVANCED_ENERGY_PIPE.get());
                output.accept(FPipesItems.ELITE_ENERGY_PIPE.get());
                output.accept(FPipesItems.ULTIMATE_ENERGY_PIPE.get());
                
                // Attachments
                output.accept(FPipesItems.BASIC_EXTRACTOR_ATTACHMENT.get());
                output.accept(FPipesItems.IMPROVED_EXTRACTOR_ATTACHMENT.get());
                output.accept(FPipesItems.ADVANCED_EXTRACTOR_ATTACHMENT.get());
                output.accept(FPipesItems.ELITE_EXTRACTOR_ATTACHMENT.get());
                output.accept(FPipesItems.ULTIMATE_EXTRACTOR_ATTACHMENT.get());
            })
            .build()
    );
} 