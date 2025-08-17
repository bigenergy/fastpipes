package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.block.EnergyPipeBlock;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FastPipesJadePlugin implements IWailaPlugin {
    
    public static final ResourceLocation ENERGY_PIPE_INFO = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "energy_pipe_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Register server data providers
        registration.registerBlockDataProvider(EnergyPipeComponentProvider.INSTANCE, EnergyPipeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register component providers for client-side display
        registration.registerBlockComponent(EnergyPipeComponentProvider.INSTANCE, EnergyPipeBlock.class);
    }
} 