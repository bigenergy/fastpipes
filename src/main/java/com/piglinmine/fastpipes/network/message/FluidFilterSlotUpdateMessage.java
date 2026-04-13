package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record FluidFilterSlotUpdateMessage(int containerSlot, FluidStack stack) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerSlot);
        stack.writeToPacket(buf);
    }

    public static FluidFilterSlotUpdateMessage decode(FriendlyByteBuf buf) {
        int containerSlot = buf.readVarInt();
        FluidStack stack = FluidStack.readFromPacket(buf);
        return new FluidFilterSlotUpdateMessage(containerSlot, stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AbstractContainerMenu container = Minecraft.getInstance().player.containerMenu;
            if (container == null) {
                return;
            }

            if (containerSlot < 0 || containerSlot >= container.slots.size()) {
                return;
            }

            Slot slot = container.getSlot(containerSlot);
            if (!(slot instanceof FluidFilterSlot)) {
                return;
            }

            ((FluidFilterSlot) slot).getFluidInventory().setFluid(slot.getSlotIndex(), stack);
        });
        ctx.get().setPacketHandled(true);
    }
}
