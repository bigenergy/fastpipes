package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record TerminalItemsSyncMessage(List<ItemStack> items, int sortOrdinal) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(items.size());
        for (ItemStack stack : items) {
            ItemStack single = stack.copy();
            single.setCount(1);
            buf.writeItem(single);
            buf.writeVarInt(stack.getCount());
        }
        buf.writeVarInt(sortOrdinal);
    }

    public static TerminalItemsSyncMessage decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItem();
            int count = buf.readVarInt();
            if (!stack.isEmpty()) {
                stack.setCount(count);
            }
            items.add(stack);
        }
        int sortOrdinal = buf.readVarInt();
        return new TerminalItemsSyncMessage(items, sortOrdinal);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof TerminalContainerMenu terminal) {
                TerminalContainerMenu.SortMode[] modes = TerminalContainerMenu.SortMode.values();
                if (sortOrdinal >= 0 && sortOrdinal < modes.length) {
                    terminal.applyClientSortMode(modes[sortOrdinal]);
                }
                terminal.receiveNetworkItems(items);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
