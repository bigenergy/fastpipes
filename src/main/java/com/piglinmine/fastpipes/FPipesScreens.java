package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.screen.ExtractorAttachmentScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = FastPipes.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FPipesScreens {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FPipesContainerMenus.EXTRACTOR_ATTACHMENT.get(), ExtractorAttachmentScreen::new);
    }
} 