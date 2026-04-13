package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalInsertMessage(boolean all) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(all);
    }

    public static TerminalInsertMessage decode(FriendlyByteBuf buf) {
        boolean all = buf.readBoolean();
        return new TerminalInsertMessage(all);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.insertItem(terminal.getCarried());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
