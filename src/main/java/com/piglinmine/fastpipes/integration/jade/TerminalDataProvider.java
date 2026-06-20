package com.piglinmine.fastpipes.integration.jade;

import com.piglinmine.fastpipes.block.TerminalBlock;
import com.piglinmine.fastpipes.blockentity.TerminalBlockEntity;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.HashSet;
import java.util.Set;

public enum TerminalDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof TerminalBlockEntity terminal)) return;

        Pipe pipe = TerminalBlock.findConnectedPipe(accessor.getLevel(), accessor.getPosition());
        if (pipe == null || pipe.getNetwork() == null) {
            data.putBoolean("Connected", false);
            return;
        }

        long totalItems = 0;
        Set<String> uniqueKeys = new HashSet<>();
        Set<BlockPos> scanned = new HashSet<>();

        for (Pipe p : pipe.getNetwork().getPipes()) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = p.getPos().relative(dir);
                if (!scanned.add(neighborPos)) continue;
                if (NetworkManager.get(accessor.getLevel()).getPipe(neighborPos) != null) continue;

                var capJade = accessor.getLevel().getCapability(
                    Capabilities.Item.BLOCK, neighborPos, dir.getOpposite());
                IItemHandler handler = capJade == null ? null : IItemHandler.of(capJade);
                if (handler == null) continue;

                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    totalItems += stack.getCount();
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    uniqueKeys.add(itemId + "|" + stack.getComponents().hashCode());
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
    public Identifier getUid() {
        return FastPipesJadePlugin.TERMINAL_INFO;
    }
}
