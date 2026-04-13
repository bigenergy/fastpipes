package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record TerminalRecipeTransferMessage(List<ItemStack> ingredients) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(ingredients.size());
        for (ItemStack stack : ingredients) {
            buf.writeItem(stack);
        }
    }

    public static TerminalRecipeTransferMessage decode(FriendlyByteBuf buf) {
        int size = buf.readByte();
        List<ItemStack> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        return new TerminalRecipeTransferMessage(items);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                terminal.handleRecipeTransfer(ingredients);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
