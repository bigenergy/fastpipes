package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
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
            }
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle UpdateFilterEntryMessage: " + e.getMessage()));
            return null;
        });
    }

    private static void applyFilterUpdate(ExtractorAttachment attachment, int slot, String filter) {
        if (slot < 0 || slot >= ExtractorAttachment.MAX_FILTER_SLOTS) return;

        if (filter.isEmpty()) {
            attachment.setTagOverride(slot, "");
            if (attachment.isFluidMode()) {
                attachment.getFluidFilter().setFluid(slot, FluidStack.EMPTY);
            } else {
                attachment.getItemFilter().setStackInSlot(slot, ItemStack.EMPTY);
            }
        } else if (filter.startsWith("#")) {
            attachment.setTagOverride(slot, filter);
            if (attachment.isFluidMode()) {
                attachment.getFluidFilter().setFluid(slot, FluidStack.EMPTY);
            } else {
                attachment.getItemFilter().setStackInSlot(slot, ItemStack.EMPTY);
            }
        } else {
            attachment.setTagOverride(slot, "");
            ResourceLocation id = ResourceLocation.tryParse(filter);
            if (id == null) return;
            if (attachment.isFluidMode()) {
                Fluid fluid = BuiltInRegistries.FLUID.get(id);
                if (fluid != Fluids.EMPTY) {
                    attachment.getFluidFilter().setFluid(slot, new FluidStack(fluid, 1000));
                }
            } else {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) {
                    attachment.getItemFilter().setStackInSlot(slot, new ItemStack(item));
                }
            }
        }
    }

    private static void applyFilterUpdate(InserterAttachment attachment, int slot, String filter) {
        if (slot < 0 || slot >= InserterAttachment.MAX_FILTER_SLOTS) return;

        if (filter.isEmpty()) {
            attachment.setTagOverride(slot, "");
            if (attachment.isFluidMode()) {
                attachment.getFluidFilter().setFluid(slot, FluidStack.EMPTY);
            } else {
                attachment.getItemFilter().setStackInSlot(slot, ItemStack.EMPTY);
            }
        } else if (filter.startsWith("#")) {
            attachment.setTagOverride(slot, filter);
            if (attachment.isFluidMode()) {
                attachment.getFluidFilter().setFluid(slot, FluidStack.EMPTY);
            } else {
                attachment.getItemFilter().setStackInSlot(slot, ItemStack.EMPTY);
            }
        } else {
            attachment.setTagOverride(slot, "");
            ResourceLocation id = ResourceLocation.tryParse(filter);
            if (id == null) return;
            if (attachment.isFluidMode()) {
                Fluid fluid = BuiltInRegistries.FLUID.get(id);
                if (fluid != Fluids.EMPTY) {
                    attachment.getFluidFilter().setFluid(slot, new FluidStack(fluid, 1000));
                }
            } else {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) {
                    attachment.getItemFilter().setStackInSlot(slot, new ItemStack(item));
                }
            }
        }
    }
}
