package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.extractor.ExtractorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.inserter.InserterAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.attachment.void_attachment.VoidAttachment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PipeAttachmentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String NBT_KEY = "Attachments";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(NBT_KEY)) return;

        ListTag list = data.getList(NBT_KEY, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            CompoundTag att = list.getCompound(i);
            renderAttachment(tooltip, att);
        }
    }

    private void renderAttachment(ITooltip tooltip, CompoundTag att) {
        String itemId = att.contains("itemId") ? att.getString("itemId") : att.getString("id");
        Direction side = Direction.values()[att.getInt("side")];

        Component sideName = Component.translatable("misc.fastpipes.direction." + side.getName());
        Component typeName = Component.translatable("item." + itemId.replace(":", "."));
        tooltip.add(Component.literal("• ")
            .append(typeName.copy().withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" — ").withStyle(ChatFormatting.DARK_GRAY))
            .append(sideName.copy().withStyle(ChatFormatting.GRAY)));

        if (att.contains("redstone")) {
            tooltip.add(line("misc.fastpipes.redstone_mode",
                "misc.fastpipes.redstone_mode." + att.getString("redstone").toLowerCase()));
        }
        if (att.contains("bw")) {
            tooltip.add(line("misc.fastpipes.mode",
                "misc.fastpipes.mode." + att.getString("bw").toLowerCase()));
        }
        if (att.contains("routing")) {
            tooltip.add(line("misc.fastpipes.routing_mode",
                "misc.fastpipes.routing_mode." + att.getString("routing").toLowerCase()));
        }
        if (att.contains("stackSize")) {
            tooltip.add(Component.literal("  ")
                .append(Component.translatable("jade.fastpipes.stack_size").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": "))
                .append(Component.literal(String.valueOf(att.getInt("stackSize"))).withStyle(ChatFormatting.WHITE)));
        }
        if (att.contains("exact")) {
            tooltip.add(line("misc.fastpipes.exact_mode",
                "misc.fastpipes.exact_mode." + (att.getBoolean("exact") ? "on" : "off")));
        }
        if (att.contains("fluidMode") && att.getBoolean("fluidMode")) {
            tooltip.add(Component.literal("  ")
                .append(Component.translatable("jade.fastpipes.fluid_mode").withStyle(ChatFormatting.AQUA)));
        }
    }

    private Component line(String labelKey, String valueKey) {
        return Component.literal("  ")
            .append(Component.translatable(labelKey).withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.translatable(valueKey).withStyle(ChatFormatting.WHITE));
    }

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
            data.put(NBT_KEY, list);
        }
    }

    @Override
    public Identifier getUid() {
        return FastPipesJadePlugin.PIPE_ATTACHMENTS;
    }
}
