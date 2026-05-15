package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.block.TerminalBlock;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Adds network status info (connected, unique stacks, total items, active user) to the Terminal tooltip.
 */
public enum TerminalInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains("Connected")) return;

        boolean connected = data.getBoolean("Connected");
        if (!connected) {
            tooltip.add(Component.translatable("jade.fastpipes.terminal.disconnected")
                .withStyle(ChatFormatting.RED));
            return;
        }

        int uniqueStacks = data.getInt("UniqueStacks");
        long totalItems = data.getLong("TotalItems");

        tooltip.add(Component.literal("• ")
            .append(Component.translatable("jade.fastpipes.terminal.unique_stacks").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.literal(String.valueOf(uniqueStacks)).withStyle(ChatFormatting.WHITE)));

        tooltip.add(Component.literal("• ")
            .append(Component.translatable("jade.fastpipes.terminal.total_items").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": "))
            .append(Component.literal(String.valueOf(totalItems)).withStyle(ChatFormatting.WHITE)));

        if (data.contains("ActiveUser")) {
            tooltip.add(Component.literal("• ")
                .append(Component.translatable("jade.fastpipes.terminal.in_use_by").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": "))
                .append(Component.literal(data.getString("ActiveUser")).withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof TerminalBlockEntity terminal)) return;

        Pipe pipe = TerminalBlock.findConnectedPipe(accessor.getLevel(), accessor.getPosition());
        if (pipe == null || pipe.getNetwork() == null) {
            data.putBoolean("Connected", false);
            return;
        }

        // Scan all neighbouring inventories of all pipes in this network
        long totalItems = 0;
        java.util.Set<String> uniqueKeys = new java.util.HashSet<>();
        java.util.Set<net.minecraft.core.BlockPos> scanned = new java.util.HashSet<>();

        for (Pipe p : pipe.getNetwork().getPipes()) {
            for (Direction dir : Direction.values()) {
                var neighborPos = p.getPos().relative(dir);
                if (!scanned.add(neighborPos)) continue;
                if (com.piglinmine.fastpipes.network.NetworkManager.get(accessor.getLevel())
                    .getPipe(neighborPos) != null) continue;

                IItemHandler handler = com.piglinmine.fastpipes.util.CapabilityUtil.getItemHandler(
                    accessor.getLevel(), neighborPos, dir.getOpposite());
                if (handler == null) continue;

                for (int i = 0; i < handler.getSlots(); i++) {
                    var stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    totalItems += stack.getCount();
                    uniqueKeys.add(net.minecraftforge.registries.ForgeRegistries.ITEMS
                        .getKey(stack.getItem()) + "|" + (stack.getTag() == null ? "" : stack.getTag().toString()));
                }
            }
        }

        data.putBoolean("Connected", true);
        data.putInt("UniqueStacks", uniqueKeys.size());
        data.putLong("TotalItems", totalItems);

        if (terminal.getActiveUserName() != null) {
            data.putString("ActiveUser", terminal.getActiveUserName());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FastPipesJadePlugin.TERMINAL_INFO;
    }
}
