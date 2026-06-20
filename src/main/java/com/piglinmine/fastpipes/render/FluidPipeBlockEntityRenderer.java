package com.piglinmine.fastpipes.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.piglinmine.fastpipes.block.FluidPipeBlock;
import com.piglinmine.fastpipes.blockentity.FluidPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

public class FluidPipeBlockEntityRenderer
        implements BlockEntityRenderer<FluidPipeBlockEntity, FluidPipeBlockEntityRenderer.FluidPipeRenderState> {

    private static final float INSET = 0.001F;

    public FluidPipeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // No per-instance state needed.
    }

    @Override
    public FluidPipeRenderState createRenderState() {
        return new FluidPipeRenderState();
    }

    @Override
    public void extractRenderState(FluidPipeBlockEntity blockEntity,
                                   FluidPipeRenderState state,
                                   float partialTick,
                                   Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

        state.hasFluid = false;
        state.sprite = null;

        if (blockEntity.getLevel() == null) {
            return;
        }

        BlockState blockState = blockEntity.getBlockState();
        if (!(blockState.getBlock() instanceof FluidPipeBlock)) {
            return;
        }

        // Advance the smoothing animation here (matches old per-render-tick cadence).
        float fullness = blockEntity.updateAndGetRenderFullness(partialTick);
        state.fullness = fullness;

        FluidStack fluidStack = blockEntity.getFluid();
        if (fluidStack.isEmpty() || fullness <= 0F) {
            return;
        }

        // 26.1.2 gap: NeoForge IClientFluidTypeExtensions stack-context accessors (getTintColor(FluidStack),
        // getStillTexture(FluidStack)) were dropped — FluidTintSource is BlockState-keyed only, so
        // FluidStack-encoded tints (NBT-coloured fluids) cannot be honoured. Pass AIR as no-op state.
        FluidModel model = Minecraft.getInstance()
                .getModelManager()
                .getFluidStateModelSet()
                .get(fluidStack.getFluid().defaultFluidState());

        int color;
        if (model.fluidTintSource() != null) {
            color = model.fluidTintSource().color(net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        } else {
            color = -1; // untinted (white)
        }

        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 255;
        state.r = (color >> 16) & 0xFF;
        state.g = (color >> 8) & 0xFF;
        state.b = color & 0xFF;
        state.a = a;

        state.sprite = model.stillMaterial().sprite();

        state.north = blockState.getValue(FluidPipeBlock.NORTH);
        state.east = blockState.getValue(FluidPipeBlock.EAST);
        state.south = blockState.getValue(FluidPipeBlock.SOUTH);
        state.west = blockState.getValue(FluidPipeBlock.WEST);
        state.up = blockState.getValue(FluidPipeBlock.UP);
        state.down = blockState.getValue(FluidPipeBlock.DOWN);

        state.hasFluid = true;
    }

    @Override
    public void submit(FluidPipeRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector nodeCollector,
                       CameraRenderState cameraRenderState) {
        if (!state.hasFluid || state.sprite == null) {
            return;
        }

        final float fullness = state.fullness;
        final int r = state.r;
        final int g = state.g;
        final int b = state.b;
        final int a = state.a;
        final int light = state.lightCoords;
        final TextureAtlasSprite sprite = state.sprite;

        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, buffer) -> {
            // submitCustomGeometry receives a PoseStack.Pose, not a PoseStack — wrap it
            // so we can reuse the existing CubeBuilder which expects a PoseStack.
            PoseStack localPose = new PoseStack();
            localPose.last().pose().set(pose.pose());
            localPose.last().normal().set(pose.normal());

            // Horizontal connections — fluid fills from the centre outward and rises with fullness.
            if (state.north) {
                float x1 = 4, y1 = 4, z1 = 0;
                float x2 = 12, y2 = 4 + (fullness * (12 - 4)), z2 = 4;
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.SOUTH);
            }
            if (state.east) {
                float x1 = 12, y1 = 4, z1 = 4;
                float x2 = 16, y2 = 4 + (fullness * (12 - 4)), z2 = 12;
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.WEST);
            }
            if (state.south) {
                float x1 = 4, y1 = 4, z1 = 12;
                float x2 = 12, y2 = 4 + (fullness * (12 - 4)), z2 = 16;
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.NORTH);
            }
            if (state.west) {
                float x1 = 0, y1 = 4, z1 = 4;
                float x2 = 4, y2 = 4 + (fullness * (12 - 4)), z2 = 12;
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.EAST);
            }
            if (state.up) {
                float x1 = 4, y1 = 12, z1 = 4;
                float x2 = 12, y2 = 16, z2 = 12;
                float shrinkage = (1F - fullness) * 4F;
                x1 += shrinkage; z1 += shrinkage;
                x2 -= shrinkage; z2 -= shrinkage;
                y1 -= (1F - fullness) * (12F - 4F);
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.DOWN);
            }
            if (state.down) {
                float x1 = 4, y1 = 0, z1 = 4;
                float x2 = 12, y2 = 4, z2 = 12;
                float shrinkage = (1F - fullness) * 4F;
                x1 += shrinkage; z1 += shrinkage;
                x2 -= shrinkage; z2 -= shrinkage;
                CubeBuilder.INSTANCE.putCube(
                        localPose, buffer,
                        (x1 / 16F) + INSET, (y1 / 16F) + INSET, (z1 / 16F) + INSET,
                        (x2 / 16F) - INSET, (y2 / 16F) - INSET, (z2 / 16F) - INSET,
                        r, g, b, a, light, sprite, Direction.UP);
            }

            // Core (centre) cube — always rendered, but with faces selectively skipped where
            // a neighbour is connected (the connection cube already covers that face).
            {
                float x1 = 4, y1 = 4, z1 = 4;
                float x2 = 12, y2 = 4 + (fullness * (12 - 4)), z2 = 12;

                float fx1 = (x1 / 16F) + INSET;
                float fy1 = (y1 / 16F) + INSET;
                float fz1 = (z1 / 16F) + INSET;
                float fx2 = (x2 / 16F) - INSET;
                float fy2 = (y2 / 16F) - INSET;
                float fz2 = (z2 / 16F) - INSET;

                CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                        r, g, b, a, light, sprite, Direction.UP);
                CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                        r, g, b, a, light, sprite, Direction.DOWN);
                if (!state.north) {
                    CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                            r, g, b, a, light, sprite, Direction.NORTH);
                }
                if (!state.east) {
                    CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                            r, g, b, a, light, sprite, Direction.EAST);
                }
                if (!state.south) {
                    CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                            r, g, b, a, light, sprite, Direction.SOUTH);
                }
                if (!state.west) {
                    CubeBuilder.INSTANCE.putFace(localPose, buffer, fx1, fy1, fz1, fx2, fy2, fz2,
                            r, g, b, a, light, sprite, Direction.WEST);
                }
            }
        });
    }

    public static class FluidPipeRenderState extends BlockEntityRenderState {
        public boolean hasFluid;
        public float fullness;
        public int r, g, b, a;
        public @Nullable TextureAtlasSprite sprite;
        public boolean north, east, south, west, up, down;
    }
}
