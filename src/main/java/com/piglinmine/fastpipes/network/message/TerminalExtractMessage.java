package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalExtractMessage(ItemStack stack, int amount, boolean toCursor) {

    public void encode(FriendlyByteBuf buf) {
        // Force count=1 to avoid byte-overflow in writeItem (aggregated counts > 127 get corrupted,
        // which makes ItemStack.getItem() return AIR on the server and breaks identity matching).
        ItemStack identity = stack.copy();
        identity.setCount(1);
        buf.writeItem(identity);
        buf.writeVarInt(amount);
        buf.writeBoolean(toCursor);
    }

    public static TerminalExtractMessage decode(FriendlyByteBuf buf) {
        ItemStack stack = buf.readItem();
        int amount = buf.readVarInt();
        boolean toCursor = buf.readBoolean();
        return new TerminalExtractMessage(stack, amount, toCursor);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.extractItem(stack, amount, toCursor);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
