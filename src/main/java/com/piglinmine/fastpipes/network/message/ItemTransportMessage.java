package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ItemTransportMessage(BlockPos pos, List<ItemTransportProps> props) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(props.size());
        for (ItemTransportProps prop : props) {
            ItemTransportProps.encode(buf, prop);
        }
    }

    public static ItemTransportMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readVarInt();
        List<ItemTransportProps> props = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            props.add(ItemTransportProps.decode(buf));
        }
        return new ItemTransportMessage(pos, props);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);

            if (blockEntity instanceof ItemPipeBlockEntity itemPipeBlockEntity) {
                itemPipeBlockEntity.setProps(props);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
