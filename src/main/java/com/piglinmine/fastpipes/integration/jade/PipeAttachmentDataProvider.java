package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PipeAttachmentDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof PipeBlockEntity)) return;

        NetworkManager mgr = NetworkManager.get(accessor.getLevel());
        Pipe pipe = mgr.getPipe(accessor.getPosition());
        if (pipe == null) return;

        ListTag list = new ListTag();
        for (Direction dir : Direction.values()) {
            if (!pipe.getAttachmentManager().hasAttachment(dir)) continue;
            Attachment att = pipe.getAttachmentManager().getAttachment(dir);
            CompoundTag tag = new CompoundTag();
            tag.putString("id", att.getId().toString());

            var dropItem = att.getDrop().getItem();
            Identifier itemKey = BuiltInRegistries.ITEM.getKey(dropItem);
            if (itemKey != null) {
                tag.putString("itemId", itemKey.toString());
            }
            tag.putInt("side", dir.ordinal());

            if (att instanceof ExtractorAttachment ex) {
                tag.putString("redstone", ex.getRedstoneMode().name());
                tag.putString("bw", ex.getBlacklistWhitelist().name());
                tag.putString("routing", ex.getRoutingMode().name());
                tag.putInt("stackSize", ex.getStackSize());
                tag.putBoolean("exact", ex.isExactMode());
                tag.putBoolean("fluidMode", ex.isFluidMode());
            } else if (att instanceof InserterAttachment ins) {
                tag.putString("redstone", ins.getRedstoneMode().name());
                tag.putString("bw", ins.getBlacklistWhitelist().name());
                tag.putBoolean("exact", ins.isExactMode());
                tag.putBoolean("fluidMode", ins.isFluidMode());
            } else if (att instanceof SensorAttachment sen) {
                tag.putString("bw", sen.getBlacklistWhitelist().name());
                tag.putBoolean("exact", sen.isExactMode());
                tag.putBoolean("fluidMode", sen.isFluidMode());
            } else if (att instanceof VoidAttachment vd) {
                tag.putString("bw", vd.getBlacklistWhitelist().name());
                tag.putBoolean("exact", vd.isExactMode());
                tag.putBoolean("fluidMode", vd.isFluidMode());
            }

            list.add(tag);
        }

        if (!list.isEmpty()) {
            data.put(PipeAttachmentProvider.NBT_KEY, list);
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.PIPE_ATTACHMENTS;
    }
}
