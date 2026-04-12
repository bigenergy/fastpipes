package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalSortMessage(int sortOrdinal) implements CustomPacketPayload {

    public static final Type<TerminalSortMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_sort"));

    public static final StreamCodec<ByteBuf, TerminalSortMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        TerminalSortMessage::sortOrdinal,
        TerminalSortMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final TerminalSortMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            if (context.player().containerMenu instanceof TerminalContainerMenu terminal) {
                TerminalContainerMenu.SortMode[] modes = TerminalContainerMenu.SortMode.values();
                int ordinal = message.sortOrdinal();
                if (ordinal >= 0 && ordinal < modes.length) {
                    terminal.setSortMode(modes[ordinal]);
                    terminal.refreshNetworkItems();
                }
            }
        });
    }
}
