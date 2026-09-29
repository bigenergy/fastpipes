package com.piglinmine.fastpipes;

import com.mojang.serialization.MapCodec;
import com.piglinmine.fastpipes.config.CommonConfig;
import com.piglinmine.fastpipes.config.ContentEnabledCondition;
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
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FastPipes.MOD_ID)
public class FastPipes {
    public static final String MOD_ID = "fastpipes";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    public static final CommonConfig COMMON_CONFIG = new CommonConfig();

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);

    static {
        CONDITION_CODECS.register("content_enabled", () -> ContentEnabledCondition.CODEC);
    }

    public FastPipes(IEventBus modEventBus, ModContainer modContainer) {
        // Register DeferredRegisters
        FPipesBlocks.BLOCKS.register(modEventBus);
        FPipesItems.ITEMS.register(modEventBus);
        FPipesBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        FPipesContainerMenus.CONTAINER_MENUS.register(modEventBus);
        FPipesCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        CONDITION_CODECS.register(modEventBus);

        // Client-only setup
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.register(ClientSetup.class);
        }

        // Register server config
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        // The content toggles are COMMON, not SERVER — see CommonConfig.
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG.getSpec());

        // Register networking
        modEventBus.addListener(FastPipesNetwork::register);

        // Register capabilities
        modEventBus.addListener(FPipesCapabilities::registerCapabilities);

        // Register mod event listeners
        modEventBus.addListener(CommonSetup::onConstructMod);
        modEventBus.addListener(CommonSetup::onCommonSetup);

        // Register forge event listeners
        NeoForge.EVENT_BUS.addListener(CommonSetup::onLevelTick);
        NeoForge.EVENT_BUS.addListener(CommonSetup::onRightClickBlock);
    }
} 