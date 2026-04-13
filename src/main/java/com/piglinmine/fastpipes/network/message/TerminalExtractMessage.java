package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalExtractMessage(ItemStack stack, int amount, boolean toCursor) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeItem(stack);
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
