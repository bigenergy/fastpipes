package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TerminalSortMessage(int sortOrdinal) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(sortOrdinal);
    }

    public static TerminalSortMessage decode(FriendlyByteBuf buf) {
        int sortOrdinal = buf.readVarInt();
        return new TerminalSortMessage(sortOrdinal);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof TerminalContainerMenu terminal) {
                TerminalContainerMenu.SortMode[] modes = TerminalContainerMenu.SortMode.values();
                int ordinal = sortOrdinal;
                if (ordinal >= 0 && ordinal < modes.length) {
                    terminal.setSortMode(modes[ordinal]);
                    terminal.refreshNetworkItems();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
