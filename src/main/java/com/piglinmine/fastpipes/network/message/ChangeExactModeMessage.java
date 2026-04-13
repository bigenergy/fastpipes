package com.piglinmine.fastpipes.network.message;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ChangeExactModeMessage(BlockPos pos, Direction direction, boolean exactMode) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(direction.get3DDataValue());
        buf.writeBoolean(exactMode);
    }

    public static ChangeExactModeMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction direction = Direction.from3DDataValue(buf.readByte());
        boolean exactMode = buf.readBoolean();
        return new ChangeExactModeMessage(pos, direction, exactMode);
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
                    ((ExtractorAttachment) attachment).setExactMode(exactMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                } else if (attachment instanceof InserterAttachment) {
                    ((InserterAttachment) attachment).setExactMode(exactMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                } else if (attachment instanceof VoidAttachment) {
                    ((VoidAttachment) attachment).setExactMode(exactMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                } else if (attachment instanceof SensorAttachment) {
                    ((SensorAttachment) attachment).setExactMode(exactMode);
                    NetworkManager.get(blockEntity.getLevel()).setDirty();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
