package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record TerminalRecipeTransferMessage(List<ItemStack> ingredients) implements CustomPacketPayload {

    public static final Type<TerminalRecipeTransferMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_recipe_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalRecipeTransferMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TerminalRecipeTransferMessage decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readByte();
            List<ItemStack> items = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new TerminalRecipeTransferMessage(items);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TerminalRecipeTransferMessage msg) {
            buf.writeByte(msg.ingredients().size());
            for (ItemStack stack : msg.ingredients()) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final TerminalRecipeTransferMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) return;
            if (context.player().containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.handleRecipeTransfer(message.ingredients());
            }
        });
    }
}
