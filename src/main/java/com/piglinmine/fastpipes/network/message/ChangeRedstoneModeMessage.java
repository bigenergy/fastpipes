package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.RedstoneMode;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeRedstoneModeMessage(BlockPos pos, Direction direction, int mode) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(direction.get3DDataValue());
        buf.writeVarInt(mode);
    }

    public static ChangeRedstoneModeMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction direction = Direction.from3DDataValue(buf.readByte());
        int mode = buf.readVarInt();
        return new ChangeRedstoneModeMessage(pos, direction, mode);
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

                RedstoneMode redstoneMode = RedstoneMode.get((byte) mode);
                if (attachment instanceof ExtractorAttachment) {
                    ((ExtractorAttachment) attachment).setRedstoneMode(redstoneMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                } else if (attachment instanceof InserterAttachment) {
                    ((InserterAttachment) attachment).setRedstoneMode(redstoneMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
