package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.barrel.TieredBarrelScreen;
import com.piglinmine.fastpipes.screen.ExtractorAttachmentScreen;
import com.piglinmine.fastpipes.screen.InserterAttachmentScreen;
import com.piglinmine.fastpipes.screen.SensorAttachmentScreen;
import com.piglinmine.fastpipes.screen.TerminalScreen;
import com.piglinmine.fastpipes.screen.VoidAttachmentScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FastPipes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FPipesScreens {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(FPipesContainerMenus.EXTRACTOR_ATTACHMENT.get(), ExtractorAttachmentScreen::new);
            MenuScreens.register(FPipesContainerMenus.INSERTER_ATTACHMENT.get(), InserterAttachmentScreen::new);
            MenuScreens.register(FPipesContainerMenus.VOID_ATTACHMENT.get(), VoidAttachmentScreen::new);
            MenuScreens.register(FPipesContainerMenus.SENSOR_ATTACHMENT.get(), SensorAttachmentScreen::new);
            MenuScreens.register(FPipesContainerMenus.TERMINAL.get(), TerminalScreen::new);
            MenuScreens.register(FPipesContainerMenus.TIERED_BARREL.get(), TieredBarrelScreen::new);
        });
    }
}
