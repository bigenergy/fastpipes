package com.piglinmine.fastpipes.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class TerminalMenuProvider implements MenuProvider {
    private final BlockPos pos;

    public TerminalMenuProvider(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.fastpipes.terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new TerminalContainerMenu(windowId, player, pos);
    }

    public static void open(BlockPos pos, ServerPlayer player) {
        NetworkHooks.openScreen(player, new TerminalMenuProvider(pos), buf -> {
            buf.writeBlockPos(pos);
        });
    }
}
