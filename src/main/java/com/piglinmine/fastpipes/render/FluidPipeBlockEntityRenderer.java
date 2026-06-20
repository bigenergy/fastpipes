package com.piglinmine.fastpipes.render;

import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

// TODO 1.21.11: BlockEntityRenderer now requires a second type parameter
// <T extends BlockEntity, S extends BlockEntityRenderState> and the rendering pipeline
// switched from direct render(...) calls against a MultiBufferSource/VertexConsumer to
// extractRenderState(...) + submit(...) against a SubmitNodeCollector. The original
// CubeBuilder-based fluid mesh assembly has been stripped; needs porting to the new
// render-state architecture.
public class FluidPipeBlockEntityRenderer implements BlockEntityRenderer<FluidPipeBlockEntity, BlockEntityRenderState> {

    public FluidPipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Constructor signature is kept for BlockEntityRenderers.register(...) factory refs.
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        // TODO 1.21.11: return a real subclass that carries the data extracted from
        // FluidPipeBlockEntity (fluid stack, fullness, neighbour connections, light).
        return new BlockEntityRenderState() {};
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        // TODO 1.21.11: no-op stub. Re-implement using SubmitNodeCollector.
    }
}
