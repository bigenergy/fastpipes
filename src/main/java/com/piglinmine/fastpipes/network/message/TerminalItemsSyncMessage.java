package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record TerminalItemsSyncMessage(List<ItemStack> items) implements CustomPacketPayload {

    public static final Type<TerminalItemsSyncMessage> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "terminal_items_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalItemsSyncMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TerminalItemsSyncMessage decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<ItemStack> items = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int count = buf.readVarInt();
                if (!stack.isEmpty()) {
                    stack.setCount(count);
                }
                items.add(stack);
            }
            return new TerminalItemsSyncMessage(items);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TerminalItemsSyncMessage msg) {
            buf.writeVarInt(msg.items().size());
            for (ItemStack stack : msg.items()) {
                ItemStack single = stack.copy();
                single.setCount(1);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, single);
                buf.writeVarInt(stack.getCount());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final TerminalItemsSyncMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.receiveNetworkItems(message.items());
            }
        });
    }
}
