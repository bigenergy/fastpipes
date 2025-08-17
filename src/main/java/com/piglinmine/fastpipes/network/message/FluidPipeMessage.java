package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidPipeMessage(BlockPos pos, FluidStack fluid, float fullness) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<FluidPipeMessage> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastPipes.MOD_ID, "fluid_pipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPipeMessage> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        FluidPipeMessage::pos,
        FluidStack.STREAM_CODEC,
        FluidPipeMessage::fluid,
        ByteBufCodecs.FLOAT,
        FluidPipeMessage::fullness,
        FluidPipeMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final FluidPipeMessage message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos());

            if (blockEntity instanceof FluidPipeBlockEntity fluidPipeBlockEntity) {
                fluidPipeBlockEntity.setFluid(message.fluid());
                fluidPipeBlockEntity.setFullness(message.fullness());
            }
        }).exceptionally(e -> {
            context.disconnect(net.minecraft.network.chat.Component.literal("Failed to handle FluidPipeMessage: " + e.getMessage()));
            return null;
        });
    }
} 