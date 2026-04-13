package com.piglinmine.fastpipes.network;

import com.piglinmine.fastpipes.network.message.*;
import com.piglinmine.fastpipes.network.message.UpdateFilterEntryMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class FastPipesNetwork {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Register all payloads
        registrar.playToClient(
            ItemTransportMessage.TYPE,
            ItemTransportMessage.STREAM_CODEC,
            ItemTransportMessage::handleClient
        );

        registrar.playToClient(
            FluidPipeMessage.TYPE,
            FluidPipeMessage.STREAM_CODEC,
            FluidPipeMessage::handleClient
        );

        registrar.playToClient(
            FluidFilterSlotUpdateMessage.TYPE,
            FluidFilterSlotUpdateMessage.STREAM_CODEC,
            FluidFilterSlotUpdateMessage::handleClient
        );

        registrar.playToServer(
            ChangeRedstoneModeMessage.TYPE,
            ChangeRedstoneModeMessage.STREAM_CODEC,
            ChangeRedstoneModeMessage::handleServer
        );

        registrar.playToServer(
            ChangeBlacklistWhitelistMessage.TYPE,
            ChangeBlacklistWhitelistMessage.STREAM_CODEC,
            ChangeBlacklistWhitelistMessage::handleServer
        );

        registrar.playToServer(
            ChangeRoutingModeMessage.TYPE,
            ChangeRoutingModeMessage.STREAM_CODEC,
            ChangeRoutingModeMessage::handleServer
        );

        registrar.playToServer(
            ChangeStackSizeMessage.TYPE,
            ChangeStackSizeMessage.STREAM_CODEC,
            ChangeStackSizeMessage::handleServer
        );

        registrar.playToServer(
            ChangeExactModeMessage.TYPE,
            ChangeExactModeMessage.STREAM_CODEC,
            ChangeExactModeMessage::handleServer
        );

        registrar.playToServer(
            UpdateFilterEntryMessage.TYPE,
            UpdateFilterEntryMessage.STREAM_CODEC,
            UpdateFilterEntryMessage::handleServer
        );

        // Terminal messages
        registrar.playToClient(
            TerminalItemsSyncMessage.TYPE,
            TerminalItemsSyncMessage.STREAM_CODEC,
            TerminalItemsSyncMessage::handleClient
        );

        registrar.playToServer(
            TerminalExtractMessage.TYPE,
            TerminalExtractMessage.STREAM_CODEC,
            TerminalExtractMessage::handleServer
        );

        registrar.playToServer(
            TerminalInsertMessage.TYPE,
            TerminalInsertMessage.STREAM_CODEC,
            TerminalInsertMessage::handleServer
        );

        registrar.playToServer(
            TerminalSearchMessage.TYPE,
            TerminalSearchMessage.STREAM_CODEC,
            TerminalSearchMessage::handleServer
        );

        registrar.playToServer(
            TerminalCraftMessage.TYPE,
            TerminalCraftMessage.STREAM_CODEC,
            TerminalCraftMessage::handleServer
        );

        registrar.playToServer(
            TerminalRecipeTransferMessage.TYPE,
            TerminalRecipeTransferMessage.STREAM_CODEC,
            TerminalRecipeTransferMessage::handleServer
        );

        registrar.playToServer(
            TerminalSortMessage.TYPE,
            TerminalSortMessage.STREAM_CODEC,
            TerminalSortMessage::handleServer
        );

        registrar.playToClient(
            TerminalStatusMessage.TYPE,
            TerminalStatusMessage.STREAM_CODEC,
            TerminalStatusMessage::handleClient
        );
    }

    // Convenience methods for sending packets
    public static void sendInArea(Level level, BlockPos pos, int radius, Object message) {
        if (level.isClientSide) return;

        if (message instanceof ItemTransportMessage msg) {
            PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel) level, null, 
                pos.getX(), pos.getY(), pos.getZ(), radius, msg);
        } else if (message instanceof FluidPipeMessage msg) {
            PacketDistributor.sendToPlayersNear((net.minecraft.server.level.ServerLevel) level, null, 
                pos.getX(), pos.getY(), pos.getZ(), radius, msg);
        }
    }

    public static void sendToServer(Object message) {
        if (message instanceof ChangeRedstoneModeMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof ChangeBlacklistWhitelistMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof ChangeRoutingModeMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof ChangeStackSizeMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof ChangeExactModeMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof UpdateFilterEntryMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalExtractMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalInsertMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalSearchMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalCraftMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalRecipeTransferMessage msg) {
            PacketDistributor.sendToServer(msg);
        } else if (message instanceof TerminalSortMessage msg) {
            PacketDistributor.sendToServer(msg);
        }
    }

    public static void sendToClient(ServerPlayer player, Object message) {
        if (message instanceof ItemTransportMessage msg) {
            PacketDistributor.sendToPlayer(player, msg);
        } else if (message instanceof FluidPipeMessage msg) {
            PacketDistributor.sendToPlayer(player, msg);
        } else if (message instanceof FluidFilterSlotUpdateMessage msg) {
            PacketDistributor.sendToPlayer(player, msg);
        } else if (message instanceof TerminalItemsSyncMessage msg) {
            PacketDistributor.sendToPlayer(player, msg);
        } else if (message instanceof TerminalStatusMessage msg) {
            PacketDistributor.sendToPlayer(player, msg);
        }
    }
} 