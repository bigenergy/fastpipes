package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.screen.ExtractorAttachmentScreen;
import com.piglinmine.fastpipes.screen.InserterAttachmentScreen;
import com.piglinmine.fastpipes.screen.SensorAttachmentScreen;
import com.piglinmine.fastpipes.screen.VoidAttachmentScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = FastPipes.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FPipesScreens {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FPipesContainerMenus.EXTRACTOR_ATTACHMENT.get(), ExtractorAttachmentScreen::new);
        event.register(FPipesContainerMenus.INSERTER_ATTACHMENT.get(), InserterAttachmentScreen::new);
        event.register(FPipesContainerMenus.VOID_ATTACHMENT.get(), VoidAttachmentScreen::new);
        event.register(FPipesContainerMenus.SENSOR_ATTACHMENT.get(), SensorAttachmentScreen::new);
    }
} 