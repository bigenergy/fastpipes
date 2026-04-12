package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.screen.TerminalScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalStatusMessage(String messageKey) implements CustomPacketPayload {

    public static final Type<TerminalStatusMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_status"));

    public static final StreamCodec<ByteBuf, TerminalStatusMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        TerminalStatusMessage::messageKey,
        TerminalStatusMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final TerminalStatusMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof TerminalScreen screen) {
                screen.showStatus(net.minecraft.network.chat.Component.translatable(message.messageKey()).getString());
            }
        });
    }
}
