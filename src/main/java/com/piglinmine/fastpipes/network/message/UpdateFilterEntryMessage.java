package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.inventory.fluid.FluidInventory;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateFilterEntryMessage(BlockPos pos, Direction direction, int slotIndex, String filterString) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateFilterEntryMessage> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "update_filter_entry"));

    public static final StreamCodec<ByteBuf, UpdateFilterEntryMessage> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateFilterEntryMessage::pos,
        Direction.STREAM_CODEC, UpdateFilterEntryMessage::direction,
        ByteBufCodecs.VAR_INT, UpdateFilterEntryMessage::slotIndex,
        ByteBufCodecs.STRING_UTF8, UpdateFilterEntryMessage::filterString,
        UpdateFilterEntryMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final UpdateFilterEntryMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().level() == null) return;

            var blockEntity = context.player().level().getBlockEntity(message.pos());
            if (!(blockEntity instanceof PipeBlockEntity pbe)) return;

            Attachment attachment = pbe.getAttachmentManager().getAttachment(message.direction());

            if (attachment instanceof ExtractorAttachment ext) {
                applyFilterUpdate(ext, message.slotIndex(), message.filterString());
                NetworkManager.get(blockEntity.getLevel()).setDirty();
            } else if (attachment instanceof InserterAttachment ins) {
                applyFilterUpdate(ins, message.slotIndex(), message.filterString());
                NetworkManager.get(blockEntity.getLevel()).setDirty();
            } else if (attachment instanceof VoidAttachment vo) {
                applyFilterUpdate(vo.getItemFilter(), vo.getFluidFilter(), vo.isFluidMode(),
                    message.slotIndex(), VoidAttachment.MAX_FILTER_SLOTS, message.filterString());
                NetworkManager.get(blockEntity.getLevel()).setDirty();
            } else if (attachment instanceof SensorAttachment sensor) {
                applyFilterUpdate(sensor.getItemFilter(), sensor.getFluidFilter(), sensor.isFluidMode(),
                    message.slotIndex(), SensorAttachment.MAX_FILTER_SLOTS, message.filterString());
                NetworkManager.get(blockEntity.getLevel()).setDirty();
            }
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle UpdateFilterEntryMessage: " + e.getMessage()));
            return null;
        });
    }

    private static void applyFilterUpdate(ExtractorAttachment attachment, int slot, String filter) {
        if (slot < 0 || slot >= ExtractorAttachment.MAX_FILTER_SLOTS) return;

        attachment.setTagOverride(slot, filter.startsWith("#") ? filter : "");
        applyFilterUpdate(attachment.getItemFilter(), attachment.getFluidFilter(), attachment.isFluidMode(),
            slot, ExtractorAttachment.MAX_FILTER_SLOTS, filter);
    }

    private static void applyFilterUpdate(InserterAttachment attachment, int slot, String filter) {
        if (slot < 0 || slot >= InserterAttachment.MAX_FILTER_SLOTS) return;

        attachment.setTagOverride(slot, filter.startsWith("#") ? filter : "");
        applyFilterUpdate(attachment.getItemFilter(), attachment.getFluidFilter(), attachment.isFluidMode(),
            slot, InserterAttachment.MAX_FILTER_SLOTS, filter);
    }

    /**
     * Resolves {@code filter} against a filter slot: an empty string or a "#tag"
     * clears the slot, anything else is treated as a registry id and resolves to
     * the matching item or fluid.
     */
    private static void applyFilterUpdate(ItemStackHandler itemFilter, FluidInventory fluidFilter,
                                          boolean fluidMode, int slot, int maxSlots, String filter) {
        if (slot < 0 || slot >= maxSlots) return;

        if (filter.isEmpty() || filter.startsWith("#")) {
            if (fluidMode) {
                fluidFilter.setFluid(slot, FluidStack.EMPTY);
            } else {
                itemFilter.setStackInSlot(slot, ItemStack.EMPTY);
            }
            return;
        }

        ResourceLocation id = ResourceLocation.tryParse(filter);
        if (id == null) return;

        if (fluidMode) {
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            if (fluid != Fluids.EMPTY) {
                fluidFilter.setFluid(slot, new FluidStack(fluid, 1000));
            }
        } else {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != Items.AIR) {
                itemFilter.setStackInSlot(slot, new ItemStack(item));
            }
        }
    }
}
