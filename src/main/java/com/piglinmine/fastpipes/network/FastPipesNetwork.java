package com.piglinmine.fastpipes.network;

import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.network.message.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class FastPipesNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(FastPipes.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        // Client-bound messages
        INSTANCE.registerMessage(id++, ItemTransportMessage.class,
            ItemTransportMessage::encode, ItemTransportMessage::decode, ItemTransportMessage::handle);
        INSTANCE.registerMessage(id++, FluidPipeMessage.class,
            FluidPipeMessage::encode, FluidPipeMessage::decode, FluidPipeMessage::handle);
        INSTANCE.registerMessage(id++, FluidFilterSlotUpdateMessage.class,
            FluidFilterSlotUpdateMessage::encode, FluidFilterSlotUpdateMessage::decode, FluidFilterSlotUpdateMessage::handle);
        INSTANCE.registerMessage(id++, TerminalItemsSyncMessage.class,
            TerminalItemsSyncMessage::encode, TerminalItemsSyncMessage::decode, TerminalItemsSyncMessage::handle);
        INSTANCE.registerMessage(id++, TerminalStatusMessage.class,
            TerminalStatusMessage::encode, TerminalStatusMessage::decode, TerminalStatusMessage::handle);

        // Server-bound messages
        INSTANCE.registerMessage(id++, ChangeRedstoneModeMessage.class,
            ChangeRedstoneModeMessage::encode, ChangeRedstoneModeMessage::decode, ChangeRedstoneModeMessage::handle);
        INSTANCE.registerMessage(id++, ChangeBlacklistWhitelistMessage.class,
            ChangeBlacklistWhitelistMessage::encode, ChangeBlacklistWhitelistMessage::decode, ChangeBlacklistWhitelistMessage::handle);
        INSTANCE.registerMessage(id++, ChangeRoutingModeMessage.class,
            ChangeRoutingModeMessage::encode, ChangeRoutingModeMessage::decode, ChangeRoutingModeMessage::handle);
        INSTANCE.registerMessage(id++, ChangeStackSizeMessage.class,
            ChangeStackSizeMessage::encode, ChangeStackSizeMessage::decode, ChangeStackSizeMessage::handle);
        INSTANCE.registerMessage(id++, ChangeExactModeMessage.class,
            ChangeExactModeMessage::encode, ChangeExactModeMessage::decode, ChangeExactModeMessage::handle);
        INSTANCE.registerMessage(id++, TerminalExtractMessage.class,
            TerminalExtractMessage::encode, TerminalExtractMessage::decode, TerminalExtractMessage::handle);
        INSTANCE.registerMessage(id++, TerminalInsertMessage.class,
            TerminalInsertMessage::encode, TerminalInsertMessage::decode, TerminalInsertMessage::handle);
        INSTANCE.registerMessage(id++, TerminalSearchMessage.class,
            TerminalSearchMessage::encode, TerminalSearchMessage::decode, TerminalSearchMessage::handle);
        INSTANCE.registerMessage(id++, TerminalCraftMessage.class,
            TerminalCraftMessage::encode, TerminalCraftMessage::decode, TerminalCraftMessage::handle);
        INSTANCE.registerMessage(id++, TerminalRecipeTransferMessage.class,
            TerminalRecipeTransferMessage::encode, TerminalRecipeTransferMessage::decode, TerminalRecipeTransferMessage::handle);
        INSTANCE.registerMessage(id++, TerminalSortMessage.class,
            TerminalSortMessage::encode, TerminalSortMessage::decode, TerminalSortMessage::handle);
    }

    public static void sendInArea(Level level, BlockPos pos, int radius, Object message) {
        if (level.isClientSide) return;
        INSTANCE.send(PacketDistributor.NEAR.with(
            () -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, level.dimension())
        ), message);
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }

    public static void sendToClient(ServerPlayer player, Object message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
