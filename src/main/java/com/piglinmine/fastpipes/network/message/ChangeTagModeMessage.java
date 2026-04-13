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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChangeTagModeMessage(BlockPos pos, Direction direction, boolean tagMode) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChangeTagModeMessage> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "change_tag_mode"));

    public static final StreamCodec<ByteBuf, ChangeTagModeMessage> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ChangeTagModeMessage::pos,
        Direction.STREAM_CODEC,
        ChangeTagModeMessage::direction,
        ByteBufCodecs.BOOL,
        ChangeTagModeMessage::tagMode,
        ChangeTagModeMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final ChangeTagModeMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().level() == null) {
                return;
            }

            BlockEntity blockEntity = context.player().level().getBlockEntity(message.pos());

            // Deprecated: tag mode is now handled per-slot via UpdateFilterEntryMessage
            // This message type is kept for protocol compatibility but performs no action.
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle ChangeTagModeMessage: " + e.getMessage()));
            return null;
        });
    }
}
