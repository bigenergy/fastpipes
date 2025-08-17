package com.piglinmine.fastpipes.setup;

import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.NetworkRegistry;
import com.piglinmine.fastpipes.network.energy.EnergyNetworkFactory;
import com.piglinmine.fastpipes.network.fluid.FluidNetworkFactory;
import com.piglinmine.fastpipes.network.item.ItemNetwork;
import com.piglinmine.fastpipes.network.item.ItemNetworkFactory;
import com.piglinmine.fastpipes.network.pipe.PipeRegistry;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentRegistry;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachmentFactory;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachmentType;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipe;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeFactory;
import com.piglinmine.fastpipes.network.pipe.energy.EnergyPipeType;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipe;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeFactory;
import com.piglinmine.fastpipes.network.pipe.fluid.FluidPipeType;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeFactory;
import com.piglinmine.fastpipes.network.pipe.transport.callback.TransportCallbackFactoryRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonSetup {
    private static final Logger LOGGER = LogManager.getLogger(CommonSetup.class);

    @SubscribeEvent
    public static void onConstructMod(FMLConstructModEvent event) {
        LOGGER.debug("Constructing Fast Pipes mod");
        
        // Initialize transport callback registry
        TransportCallbackFactoryRegistry.INSTANCE.initialize(null); // Will be called with proper HolderLookup.Provider when available
        
        // Register network factories
        registerNetworkFactories();
        
        // Register pipe factories
        registerPipeFactories();
        
        // Register attachment factories
        registerAttachmentFactories();
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.debug("Common setup for Fast Pipes");
    }

    private static void registerNetworkFactories() {
        LOGGER.debug("Registering network factories");
        
        // Register single Item Network Factory (all item pipes use the same network type)
        NetworkRegistry.INSTANCE.addFactory(ItemNetwork.TYPE, new ItemNetworkFactory());
        LOGGER.debug("Registered ItemNetworkFactory for type: {}", ItemNetwork.TYPE);
        
        // Register Fluid Network Factories for each pipe type
        for (FluidPipeType pipeType : FluidPipeType.values()) {
            NetworkRegistry.INSTANCE.addFactory(pipeType.getNetworkType(), new FluidNetworkFactory(pipeType));
            LOGGER.debug("Registered FluidNetworkFactory for type: {}", pipeType.getNetworkType());
        }
        
        // Register Energy Network Factories for each pipe type
        for (EnergyPipeType pipeType : EnergyPipeType.values()) {
            NetworkRegistry.INSTANCE.addFactory(pipeType.getNetworkType(), new EnergyNetworkFactory(pipeType));
            LOGGER.debug("Registered EnergyNetworkFactory for type: {}", pipeType.getNetworkType());
        }
        
        LOGGER.debug("Network factories registered successfully");
    }

    private static void registerPipeFactories() {
        LOGGER.debug("Registering pipe factories");
        
        // Register pipe factories for NBT deserialization
        PipeRegistry.INSTANCE.addFactory(ItemPipe.ID, new ItemPipeFactory());
        LOGGER.debug("Registered ItemPipeFactory for ID: {}", ItemPipe.ID);
        
        PipeRegistry.INSTANCE.addFactory(FluidPipe.ID, new FluidPipeFactory());
        LOGGER.debug("Registered FluidPipeFactory for ID: {}", FluidPipe.ID);
        
        PipeRegistry.INSTANCE.addFactory(EnergyPipe.ID, new EnergyPipeFactory());
        LOGGER.debug("Registered EnergyPipeFactory for ID: {}", EnergyPipe.ID);
        
        LOGGER.debug("Pipe factories registered successfully");
    }

    private static void registerAttachmentFactories() {
        LOGGER.debug("Registering attachment factories");
        
        // Register ExtractorAttachment factories for all types
        AttachmentRegistry.INSTANCE.addFactory(ExtractorAttachmentType.BASIC.getId(), new ExtractorAttachmentFactory(ExtractorAttachmentType.BASIC));
        LOGGER.debug("Registered ExtractorAttachmentFactory for type: {}", ExtractorAttachmentType.BASIC.getId());
        
        AttachmentRegistry.INSTANCE.addFactory(ExtractorAttachmentType.IMPROVED.getId(), new ExtractorAttachmentFactory(ExtractorAttachmentType.IMPROVED));
        LOGGER.debug("Registered ExtractorAttachmentFactory for type: {}", ExtractorAttachmentType.IMPROVED.getId());
        
        AttachmentRegistry.INSTANCE.addFactory(ExtractorAttachmentType.ADVANCED.getId(), new ExtractorAttachmentFactory(ExtractorAttachmentType.ADVANCED));
        LOGGER.debug("Registered ExtractorAttachmentFactory for type: {}", ExtractorAttachmentType.ADVANCED.getId());
        
        AttachmentRegistry.INSTANCE.addFactory(ExtractorAttachmentType.ELITE.getId(), new ExtractorAttachmentFactory(ExtractorAttachmentType.ELITE));
        LOGGER.debug("Registered ExtractorAttachmentFactory for type: {}", ExtractorAttachmentType.ELITE.getId());
        
        AttachmentRegistry.INSTANCE.addFactory(ExtractorAttachmentType.ULTIMATE.getId(), new ExtractorAttachmentFactory(ExtractorAttachmentType.ULTIMATE));
        LOGGER.debug("Registered ExtractorAttachmentFactory for type: {}", ExtractorAttachmentType.ULTIMATE.getId());
        
        LOGGER.debug("Attachment factories registered successfully");
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // Update all networks on server side
        if (!event.getLevel().isClientSide()) {
            NetworkManager.get(event.getLevel()).getNetworks().forEach(n -> n.update(event.getLevel()));
        }
    }
} 