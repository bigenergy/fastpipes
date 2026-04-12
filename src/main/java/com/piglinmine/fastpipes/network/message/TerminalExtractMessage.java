package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalExtractMessage(ItemStack stack, int amount, boolean toCursor) implements CustomPacketPayload {

    public static final Type<TerminalExtractMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_extract"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalExtractMessage> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC,
        TerminalExtractMessage::stack,
        ByteBufCodecs.VAR_INT,
        TerminalExtractMessage::amount,
        ByteBufCodecs.BOOL,
        TerminalExtractMessage::toCursor,
        TerminalExtractMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final TerminalExtractMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            if (context.player().containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.extractItem(message.stack(), message.amount(), message.toCursor());
            }
        });
    }
}
