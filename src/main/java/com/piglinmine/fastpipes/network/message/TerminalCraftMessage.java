package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalCraftMessage(boolean craftAll) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(craftAll);
    }

    public static TerminalCraftMessage decode(FriendlyByteBuf buf) {
        boolean craftAll = buf.readBoolean();
        return new TerminalCraftMessage(craftAll);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.performCraft(craftAll);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
