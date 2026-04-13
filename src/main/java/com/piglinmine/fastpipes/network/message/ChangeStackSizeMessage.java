package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeStackSizeMessage(BlockPos pos, Direction direction, int stackSize) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(direction.get3DDataValue());
        buf.writeVarInt(stackSize);
    }

    public static ChangeStackSizeMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction direction = Direction.from3DDataValue(buf.readByte());
        int stackSize = buf.readVarInt();
        return new ChangeStackSizeMessage(pos, direction, stackSize);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null || player.level() == null) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(pos);

            if (blockEntity instanceof PipeBlockEntity) {
                Attachment attachment = ((PipeBlockEntity) blockEntity).getAttachmentManager().getAttachment(direction);

                if (attachment instanceof ExtractorAttachment) {
                    ((ExtractorAttachment) attachment).setStackSize(stackSize);

                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
