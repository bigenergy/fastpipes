package com.piglinmine.fastpipes.barrel;

import net.minecraft.server.level.ServerPlayer;

public class TieredBarrelMenuProvider {
    public static void open(ServerPlayer player, TieredBarrelBlockEntity be) {
        player.openMenu(be, buf -> {
            buf.writeBlockPos(be.getBlockPos());
            buf.writeByte(be.getTier().ordinal());
        });
    }
}
