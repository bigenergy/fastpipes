package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidFilterSlotUpdateMessage(int containerSlot, FluidStack stack) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<FluidFilterSlotUpdateMessage> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid_filter_slot_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFilterSlotUpdateMessage> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        FluidFilterSlotUpdateMessage::containerSlot,
        FluidStack.STREAM_CODEC,
        FluidFilterSlotUpdateMessage::stack,
        FluidFilterSlotUpdateMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final FluidFilterSlotUpdateMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractContainerMenu container = Minecraft.getInstance().player.containerMenu;
            if (container == null) {
                return;
            }

            if (message.containerSlot() < 0 || message.containerSlot() >= container.slots.size()) {
                return;
            }

            Slot slot = container.getSlot(message.containerSlot());
            if (!(slot instanceof FluidFilterSlot)) {
                return;
            }

            ((FluidFilterSlot) slot).getFluidInventory().setFluid(slot.getSlotIndex(), message.stack());
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle FluidFilterSlotUpdateMessage: " + e.getMessage()));
            return null;
        });
    }
} 