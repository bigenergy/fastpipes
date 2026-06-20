package com.piglinmine.fastpipes.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemPipeBlockEntityRenderer
        implements BlockEntityRenderer<ItemPipeBlockEntity, ItemPipeBlockEntityRenderer.ItemPipeRenderState> {

    private final ItemModelResolver itemModelResolver;

    public ItemPipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ItemPipeRenderState createRenderState() {
        return new ItemPipeRenderState();
    }

    @Override
    public void extractRenderState(ItemPipeBlockEntity blockEntity,
                                   ItemPipeRenderState state,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

        state.partialTick = partialTick;
        state.gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
        state.transports.clear();
        state.nextBlockEmpty.clear();

        List<ItemTransportProps> props = blockEntity.getProps();
        if (props.isEmpty()) {
            return;
        }

        Level level = blockEntity.getLevel();
        BlockPos blockPos = blockEntity.getBlockPos();
        long basePackedId = blockPos.asLong();

        int i = 0;
        for (ItemTransportProps prop : props) {
            TransportSnap snap = new TransportSnap();
            snap.direction = prop.getDirection();
            snap.initialDirection = prop.getInitialDirection();
            snap.firstPipe = prop.isFirstPipe();
            snap.lastPipe = prop.isLastPipe();
            snap.maxTicksInPipe = prop.getMaxTicksInPipe();
            snap.progress = prop.getProgress();
            snap.stack = prop.getStack().copy();

            // Snapshot whether the neighbouring block in the transport direction is empty
            // (used to suppress rendering when the pipe is gone). Must be sampled here because
            // submit() must not read the level.
            boolean nextEmpty = level != null && level.isEmptyBlock(blockPos.relative(prop.getDirection()));
            state.nextBlockEmpty.add(nextEmpty);

            ItemStackRenderState isrs = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(
                    isrs,
                    snap.stack,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    (int) basePackedId + i
            );
            snap.itemRenderState = isrs;

            state.transports.add(snap);
            i++;
        }
    }

    @Override
    public void submit(ItemPipeRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        if (state.transports.isEmpty()) {
            return;
        }

        float partialTick = state.partialTick;
        long gameTime = state.gameTime;

        for (int idx = 0; idx < state.transports.size(); idx++) {
            TransportSnap snap = state.transports.get(idx);
            if (snap.itemRenderState == null || snap.itemRenderState.isEmpty()) {
                continue;
            }

            Direction dir = snap.direction;

            double pipeLength = 1D;
            if (snap.firstPipe) {
                pipeLength = 1.25D;
            }
            if (snap.lastPipe) {
                pipeLength = 0.25D;
            }

            double maxTicksInPipe = (double) snap.maxTicksInPipe * pipeLength;
            double v = (((double) snap.progress + partialTick) / maxTicksInPipe) * pipeLength;

            if (snap.firstPipe && v < 0.25) {
                dir = snap.initialDirection;
            }

            if (snap.firstPipe) {
                v -= 0.25D;
            }

            // Suppress when the next pipe disappeared while we were mid-flight.
            if (v > 0.25 && state.nextBlockEmpty.get(idx)) {
                continue;
            }

            v = Math.min(1F, v);

            poseStack.pushPose();
            poseStack.translate(
                    0.5 + (dir.getStepX() * v),
                    0.5 + (dir.getStepY() * v),
                    0.5 + (dir.getStepZ() * v)
            );

            float rotationTime = (float) ((gameTime / 25D) % (Math.PI * 2) + (partialTick / 25D));
            poseStack.mulPose(Axis.YP.rotation(rotationTime));
            poseStack.scale(0.5F, 0.5F, 0.5F);

            snap.itemRenderState.submit(
                    poseStack,
                    nodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );

            poseStack.popPose();
        }
    }

    public static class ItemPipeRenderState extends BlockEntityRenderState {
        public float partialTick;
        public long gameTime;
        public final List<TransportSnap> transports = new ArrayList<>();
        // Parallel to transports — whether the block one step in the transport direction
        // was empty at extract time. Used to skip rendering an item whose downstream pipe is gone.
        public final List<Boolean> nextBlockEmpty = new ArrayList<>();
    }

    public static class TransportSnap {
        public Direction direction;
        public Direction initialDirection;
        public boolean firstPipe;
        public boolean lastPipe;
        public int maxTicksInPipe;
        public int progress;
        public ItemStack stack;
        public ItemStackRenderState itemRenderState;
    }
}
