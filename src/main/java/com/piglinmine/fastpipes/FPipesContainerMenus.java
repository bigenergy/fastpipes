package com.piglinmine.fastpipes;

import com.piglinmine.fastpipes.menu.ExtractorAttachmentContainerMenu;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.*;
import com.piglinmine.fastpipes.util.DirectionUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FPipesContainerMenus {
    public static final DeferredRegister<MenuType<?>> CONTAINER_MENUS = DeferredRegister.create(Registries.MENU, FastPipes.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorAttachmentContainerMenu>> EXTRACTOR_ATTACHMENT = CONTAINER_MENUS.register(
        "extractor_attachment",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            if (data != null) {
                // Server-side data available - use real values
                return new ExtractorAttachmentContainerMenu(
                    windowId,
                    inv.player,
                    data.readBlockPos(),
                    DirectionUtil.safeGet(data.readByte()),
                    RedstoneMode.get(data.readByte()),
                    BlacklistWhitelist.get(data.readByte()),
                    RoutingMode.get(data.readByte()),
                    data.readInt(),
                    data.readBoolean(),
                    ExtractorAttachmentType.get(data.readByte()),
                    ExtractorAttachment.createItemFilterInventory(null),
                    ExtractorAttachment.createFluidFilterInventory(null),
                    data.readBoolean()
                );
            } else {
                // Fallback for client-only constructor
                return new ExtractorAttachmentContainerMenu(windowId, inv.player);
            }
        })
    );
} 