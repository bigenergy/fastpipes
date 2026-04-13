package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.config.ServerConfig;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.setup.ClientSetup;
import com.piglinmine.fastpipes.setup.CommonSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(FastPipes.MOD_ID)
public class FastPipes {
    public static final String MOD_ID = "fastpipes";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();

    public FastPipes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

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
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());

        // Register networking
        FastPipesNetwork.register();

        // Register mod event listeners
        modEventBus.addListener(CommonSetup::onCommonSetup);

        // Register forge event listeners
        MinecraftForge.EVENT_BUS.addListener(CommonSetup::onLevelTick);
    }
}
