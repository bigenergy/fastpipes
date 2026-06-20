package com.piglinmine.fastpipes.render;

import com.piglinmine.fastpipes.blockentity.ItemPipeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

// TODO 1.21.11: BlockEntityRenderer now requires a second type parameter
// <T extends BlockEntity, S extends BlockEntityRenderState> and the rendering pipeline
// switched from direct render(...) to extractRenderState(...) + submit(...). The original
// per-tick in-pipe ItemStack rendering loop was stripped; needs porting to the new
// render-state architecture (and a per-prop render-state holder).
public class ItemPipeBlockEntityRenderer implements BlockEntityRenderer<ItemPipeBlockEntity, BlockEntityRenderState> {

    public ItemPipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // Constructor signature is kept for BlockEntityRenderers.register(...) factory refs.
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        // TODO 1.21.11: return a real subclass carrying the snapshotted item transport
        // props (direction, progress, stack, max ticks, first/last flags).
        return new BlockEntityRenderState() {};
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        // TODO 1.21.11: no-op stub. Re-implement using SubmitNodeCollector + ItemModelResolver.
    }
}
