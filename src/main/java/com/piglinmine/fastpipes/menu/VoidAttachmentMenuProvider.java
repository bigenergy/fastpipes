package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class VoidAttachmentMenuProvider implements MenuProvider {
    private final Pipe pipe;
    private final VoidAttachment attachment;

    public VoidAttachmentMenuProvider(Pipe pipe, VoidAttachment attachment) {
        this.pipe = pipe;
        this.attachment = attachment;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.fastpipes.void_attachment");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new VoidAttachmentContainerMenu(
            windowId,
            player,
            pipe.getPos(),
            attachment.getDirection(),
            attachment.getBlacklistWhitelist(),
            attachment.isExactMode(),
            attachment.getItemFilter(),
            attachment.getFluidFilter(),
            attachment.isFluidMode()
        );
    }

    public static void open(Pipe pipe, VoidAttachment attachment, ServerPlayer player) {
        player.openMenu(new VoidAttachmentMenuProvider(pipe, attachment), buf -> {
            buf.writeBlockPos(pipe.getPos());
            buf.writeByte(attachment.getDirection().ordinal());
            buf.writeByte(attachment.getBlacklistWhitelist().ordinal());
            buf.writeBoolean(attachment.isExactMode());
            buf.writeBoolean(attachment.isFluidMode());

            // Sync fluid filter contents to client
            for (int i = 0; i < VoidAttachment.MAX_FILTER_SLOTS; i++) {
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, attachment.getFluidFilter().getFluid(i));
            }
        });
    }
}
