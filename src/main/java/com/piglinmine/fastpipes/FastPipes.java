package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.config.ServerConfig;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.setup.ClientSetup;
import com.piglinmine.fastpipes.setup.CommonSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(FastPipes.MOD_ID)
public class FastPipes {
    public static final String MOD_ID = "fastpipes";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public FastPipes(IEventBus modEventBus, ModContainer modContainer) {
        // Register DeferredRegisters
        FPipesBlocks.BLOCKS.register(modEventBus);
        FPipesItems.ITEMS.register(modEventBus);
        FPipesBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        FPipesContainerMenus.CONTAINER_MENUS.register(modEventBus);
        FPipesCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Client-only setup
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientSetup.class);
        }

        // Register server config
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        // Register networking
        modEventBus.addListener(FastPipesNetwork::register);

        // Register capabilities
        modEventBus.addListener(FPipesCapabilities::registerCapabilities);

        // Register mod event listeners
        modEventBus.addListener(CommonSetup::onConstructMod);
        modEventBus.addListener(CommonSetup::onCommonSetup);

        // Register forge event listeners
        NeoForge.EVENT_BUS.addListener(CommonSetup::onLevelTick);
    }
} 