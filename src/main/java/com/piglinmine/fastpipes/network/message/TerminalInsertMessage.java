package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalInsertMessage(boolean all) implements CustomPacketPayload {

    public static final Type<TerminalInsertMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_insert"));

    public static final StreamCodec<ByteBuf, TerminalInsertMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        TerminalInsertMessage::all,
        TerminalInsertMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final TerminalInsertMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            if (context.player().containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.insertItem(terminal.getCarried());
            }
        });
    }
}
