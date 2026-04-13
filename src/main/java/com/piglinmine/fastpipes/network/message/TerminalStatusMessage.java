package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.screen.TerminalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalStatusMessage(String messageKey) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(messageKey);
    }

    public static TerminalStatusMessage decode(FriendlyByteBuf buf) {
        String messageKey = buf.readUtf();
        return new TerminalStatusMessage(messageKey);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof TerminalScreen screen) {
                screen.showStatus(Component.translatable(messageKey).getString());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
