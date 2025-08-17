package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;

public class ExtractorAttachmentMenuProvider implements MenuProvider {
    private final Pipe pipe;
    private final ExtractorAttachment attachment;

    public ExtractorAttachmentMenuProvider(Pipe pipe, ExtractorAttachment attachment) {
        this.pipe = pipe;
        this.attachment = attachment;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.fastpipes.extractor_attachment");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ExtractorAttachmentContainerMenu(
            windowId,
            player,
            pipe.getPos(),
            attachment.getDirection(),
            attachment.getRedstoneMode(),
            attachment.getBlacklistWhitelist(),
            attachment.getRoutingMode(),
            attachment.getStackSize(),
            attachment.isExactMode(),
            attachment.getType(),
            attachment.getItemFilter(),
            attachment.getFluidFilter(),
            attachment.isFluidMode()
        );
    }

    public static void open(Pipe pipe, ExtractorAttachment attachment, ServerPlayer player) {
        player.openMenu(new ExtractorAttachmentMenuProvider(pipe, attachment), buf -> {
            buf.writeBlockPos(pipe.getPos());
            buf.writeByte(attachment.getDirection().ordinal());
            buf.writeByte(attachment.getRedstoneMode().ordinal());
            buf.writeByte(attachment.getBlacklistWhitelist().ordinal());
            buf.writeByte(attachment.getRoutingMode().ordinal());
            buf.writeInt(attachment.getStackSize());
            buf.writeBoolean(attachment.isExactMode());
            buf.writeByte(attachment.getType().ordinal());
            buf.writeBoolean(attachment.isFluidMode());
        });
    }
} 