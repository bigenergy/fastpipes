package com.piglinmine.fastpipes.barrel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

public class TieredBarrelMenuProvider {
    public static void open(ServerPlayer player, TieredBarrelBlockEntity be) {
        NetworkHooks.openScreen(player, be, buf -> {
            buf.writeBlockPos(be.getBlockPos());
            buf.writeByte(be.getTier().ordinal());
        });
    }
}
