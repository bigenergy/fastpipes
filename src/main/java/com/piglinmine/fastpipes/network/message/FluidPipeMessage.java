package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record FluidPipeMessage(BlockPos pos, FluidStack fluid, float fullness) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        fluid.writeToPacket(buf);
        buf.writeFloat(fullness);
    }

    public static FluidPipeMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        FluidStack fluid = FluidStack.readFromPacket(buf);
        float fullness = buf.readFloat();
        return new FluidPipeMessage(pos, fluid, fullness);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);

            if (blockEntity instanceof FluidPipeBlockEntity fluidPipeBlockEntity) {
                fluidPipeBlockEntity.setFluid(fluid);
                fluidPipeBlockEntity.setFullness(fullness);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
