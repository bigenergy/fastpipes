package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.block.EnergyPipeBlock;
import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.block.ItemPipeBlock;
import com.piglinmine.fastpipes.block.PipeBlock;
import com.piglinmine.fastpipes.block.TerminalBlock;
import net.minecraft.resources.Identifier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FastPipesJadePlugin implements IWailaPlugin {

    public static final Identifier ENERGY_PIPE_INFO = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "energy_pipe_info");
    public static final Identifier ITEM_PIPE_INFO = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "item_pipe_info");
    public static final Identifier FLUID_PIPE_INFO = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid_pipe_info");
    public static final Identifier PIPE_ATTACHMENTS = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "pipe_attachments");
    public static final Identifier TERMINAL_INFO = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // TODO 1.21.11: Jade 1.21.6+ rejects data providers that also implement IComponentProvider.
        // Our *ComponentProvider enums implement both IServerDataProvider and IBlockComponentProvider,
        // so registerBlockDataProvider(...) crashes with IllegalArgumentException.
        // Fix: split each enum into two classes (one data-only, one component-only) and re-register
        // the data-only halves here. For now we skip data registration — tooltips still show via
        // registerClient(...) below, but server-synced fields (Speed, ItemsInTransit, etc.) will
        // read empty CompoundTag on the client and the relevant lines are omitted.
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
