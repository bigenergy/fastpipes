package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.block.EnergyPipeBlock;
import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.block.PipeBlock;
import com.piglinmine.fastpipes.block.TerminalBlock;
import com.piglinmine.fastpipes.blockentity.EnergyPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FastPipesJadePlugin implements IWailaPlugin {

    public static final ResourceLocation ENERGY_PIPE_INFO = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "energy_pipe_info");
    public static final ResourceLocation ITEM_PIPE_INFO = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_pipe_info");
    public static final ResourceLocation FLUID_PIPE_INFO = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid_pipe_info");
    public static final ResourceLocation PIPE_ATTACHMENTS = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "pipe_attachments");
    public static final ResourceLocation TERMINAL_INFO = ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EnergyPipeComponentProvider.INSTANCE, EnergyPipeBlockEntity.class);
        registration.registerBlockDataProvider(ItemPipeComponentProvider.INSTANCE, ItemPipeBlockEntity.class);
        registration.registerBlockDataProvider(FluidPipeComponentProvider.INSTANCE, FluidPipeBlockEntity.class);
        registration.registerBlockDataProvider(PipeAttachmentProvider.INSTANCE, PipeBlockEntity.class);
        registration.registerBlockDataProvider(TerminalInfoProvider.INSTANCE, TerminalBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EnergyPipeComponentProvider.INSTANCE, EnergyPipeBlock.class);
        registration.registerBlockComponent(ItemPipeComponentProvider.INSTANCE, ItemPipeBlock.class);
        registration.registerBlockComponent(FluidPipeComponentProvider.INSTANCE, FluidPipeBlock.class);
        registration.registerBlockComponent(PipeAttachmentProvider.INSTANCE, PipeBlock.class);
        registration.registerBlockComponent(TerminalInfoProvider.INSTANCE, TerminalBlock.class);
    }
}
