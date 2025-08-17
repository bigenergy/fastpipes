package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ItemTransportMessage(BlockPos pos, List<ItemTransportProps> props) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<ItemTransportMessage> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "item_transport"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTransportMessage> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ItemTransportMessage::pos,
        ByteBufCodecs.collection(ArrayList::new, ItemTransportProps.STREAM_CODEC),
        ItemTransportMessage::props,
        ItemTransportMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final ItemTransportMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos());

            if (blockEntity instanceof ItemPipeBlockEntity itemPipeBlockEntity) {
                itemPipeBlockEntity.setProps(message.props());
            }
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle ItemTransportMessage: " + e.getMessage()));
            return null;
        });
    }
} 