package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalSearchMessage(String query) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(query);
    }

    public static TerminalSearchMessage decode(FriendlyByteBuf buf) {
        String query = buf.readUtf();
        return new TerminalSearchMessage(query);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.setSearchText(query);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
