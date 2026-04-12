package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalSearchMessage(String query) implements CustomPacketPayload {

    public static final Type<TerminalSearchMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_search"));

    public static final StreamCodec<ByteBuf, TerminalSearchMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        TerminalSearchMessage::query,
        TerminalSearchMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final TerminalSearchMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            if (context.player().containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.setSearchText(message.query());
            }
        });
    }
}
