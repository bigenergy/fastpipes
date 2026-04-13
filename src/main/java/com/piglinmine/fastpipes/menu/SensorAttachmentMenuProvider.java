package com.piglinmine.fastpipes.menu;

import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class SensorAttachmentMenuProvider implements MenuProvider {
    private final Pipe pipe;
    private final SensorAttachment attachment;

    public SensorAttachmentMenuProvider(Pipe pipe, SensorAttachment attachment) {
        this.pipe = pipe;
        this.attachment = attachment;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.fastpipes.sensor_attachment");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new SensorAttachmentContainerMenu(
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

    public static void open(Pipe pipe, SensorAttachment attachment, ServerPlayer player) {
        NetworkHooks.openScreen(player, new SensorAttachmentMenuProvider(pipe, attachment), buf -> {
            buf.writeBlockPos(pipe.getPos());
            buf.writeByte(attachment.getDirection().ordinal());
            buf.writeByte(attachment.getBlacklistWhitelist().ordinal());
            buf.writeBoolean(attachment.isExactMode());
            buf.writeBoolean(attachment.isFluidMode());

            // Sync fluid filter contents to client
            for (int i = 0; i < SensorAttachment.MAX_FILTER_SLOTS; i++) {
                buf.writeFluidStack(attachment.getFluidFilter().getFluid(i));
            }
        });
    }
}
