package com.piglinmine.fastpipes.render;

import com.google.common.collect.ImmutableList;
import com.piglinmine.fastpipes.block.PipeBlock;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockMath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.common.util.TransformationHelper;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PipeBakedModel implements BakedModel {
    private static final Logger LOGGER = LogManager.getLogger(PipeBakedModel.class);
    private static final Map<Direction, Transformation> SIDE_TRANSFORMS = new EnumMap<>(Direction.class);
    
    private final BakedModel core;
    private final BakedModel extension;
    private final BakedModel straight;
    private final BakedModel inventoryAttachment;
    private final Map<ResourceLocation, BakedModel> attachmentModels;
    private final Map<PipeState, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    public PipeBakedModel(BakedModel core, BakedModel extension, BakedModel straight, BakedModel inventoryAttachment, Map<ResourceLocation, BakedModel> attachmentModels) {
        this.core = core;
        this.extension = extension;
        this.straight = straight;
        this.inventoryAttachment = inventoryAttachment;
        this.attachmentModels = attachmentModels;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
        ResourceLocation[] attachmentState = extraData.get(PipeBlockEntity.ATTACHMENTS_PROPERTY);
        PipeState pipeState = new PipeState(state, attachmentState, side, rand);

        return cache.computeIfAbsent(pipeState, this::createQuads);
    }

    private List<BakedQuad> createQuads(PipeState state) {
        List<BakedQuad> quads = new ArrayList<>();

        if (state.getState() != null) {
            boolean north = state.getState().getValue(PipeBlock.NORTH);
            boolean east = state.getState().getValue(PipeBlock.EAST);
            boolean south = state.getState().getValue(PipeBlock.SOUTH);
            boolean west = state.getState().getValue(PipeBlock.WEST);
            boolean up = state.getState().getValue(PipeBlock.UP);
            boolean down = state.getState().getValue(PipeBlock.DOWN);

            // Determine pipe shape and add appropriate quads
            if (north && south && !east && !west && !up && !down) {
                // Straight north-south pipe
                quads.addAll(straight.getQuads(state.getState(), state.getSide(), state.getRand(), ModelData.EMPTY, null));
            } else if (!north && !south && east && west && !up && !down) {
                // Straight east-west pipe (rotated)
                quads.addAll(getTransformedQuads(straight, Direction.EAST, state));
            } else if (!north && !south && !east && !west && up && down) {
                // Straight up-down pipe (rotated)
                quads.addAll(getTransformedQuads(straight, Direction.UP, state));
            } else if (!north && !south && !east && !west && !up && !down) {
                // Core only (no connections)
                quads.addAll(core.getQuads(state.getState(), state.getSide(), state.getRand(), ModelData.EMPTY, null));
            } else {
                // Complex shape with core and extensions
                quads.addAll(core.getQuads(state.getState(), state.getSide(), state.getRand(), ModelData.EMPTY, null));

                if (north) {
                    quads.addAll(extension.getQuads(state.getState(), state.getSide(), state.getRand(), ModelData.EMPTY, null));
                }

                if (east) {
                    quads.addAll(getTransformedQuads(extension, Direction.EAST, state));
                }

                if (south) {
                    quads.addAll(getTransformedQuads(extension, Direction.SOUTH, state));
                }

                if (west) {
                    quads.addAll(getTransformedQuads(extension, Direction.WEST, state));
                }

                if (up) {
                    quads.addAll(getTransformedQuads(extension, Direction.UP, state));
                }

                if (down) {
                    quads.addAll(getTransformedQuads(extension, Direction.DOWN, state));
                }
            }
        }

        // Add attachment models
        if (state.getAttachmentState() != null) {
            LOGGER.debug("Processing attachments for pipe at state: {}", Arrays.toString(state.getAttachmentState()));
            for (Direction dir : Direction.values()) {
                ResourceLocation attachmentId = state.getAttachmentState()[dir.ordinal()];

                if (attachmentId != null) {
                    LOGGER.debug("Found attachment {} in direction {}", attachmentId, dir);
                    BakedModel attachmentModel = attachmentModels.get(attachmentId);
                    if (attachmentModel != null) {
                        LOGGER.debug("Adding attachment model for {} in direction {}", attachmentId, dir);
                        quads.addAll(getTransformedQuads(attachmentModel, dir, state));
                    } else {
                        LOGGER.warn("No attachment model found for ID: {}", attachmentId);
                    }
                }
            }
        } else {
            LOGGER.debug("No attachment state found for pipe");
        }

        // Add inventory attachment indicators
        if (state.getState() != null) {
            boolean invNorth = state.getState().getValue(PipeBlock.INV_NORTH);
            boolean invEast = state.getState().getValue(PipeBlock.INV_EAST);
            boolean invSouth = state.getState().getValue(PipeBlock.INV_SOUTH);
            boolean invWest = state.getState().getValue(PipeBlock.INV_WEST);
            boolean invUp = state.getState().getValue(PipeBlock.INV_UP);
            boolean invDown = state.getState().getValue(PipeBlock.INV_DOWN);

            if (invNorth && !state.hasAttachmentState(Direction.NORTH)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.NORTH, state));
            }

            if (invEast && !state.hasAttachmentState(Direction.EAST)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.EAST, state));
            }

            if (invSouth && !state.hasAttachmentState(Direction.SOUTH)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.SOUTH, state));
            }

            if (invWest && !state.hasAttachmentState(Direction.WEST)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.WEST, state));
            }

            if (invUp && !state.hasAttachmentState(Direction.UP)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.UP, state));
            }

            if (invDown && !state.hasAttachmentState(Direction.DOWN)) {
                quads.addAll(getTransformedQuads(inventoryAttachment, Direction.DOWN, state));
            }
        }

        return quads;
    }

    private List<BakedQuad> getTransformedQuads(BakedModel model, Direction facing, PipeState state) {
        Transformation transformation = SIDE_TRANSFORMS.computeIfAbsent(facing, face -> {
            Quaternionf quaternion;
            if (face == Direction.UP) {
                quaternion = TransformationHelper.quatFromXYZ(new Vector3f(90, 0, 0), true);
            } else if (face == Direction.DOWN) {
                quaternion = TransformationHelper.quatFromXYZ(new Vector3f(270, 0, 0), true);
            } else {
                double r = Math.PI * (360 - face.getOpposite().get2DDataValue() * 90) / 180d;
                quaternion = TransformationHelper.quatFromXYZ(new Vector3f(0, (float) r, 0), false);
            }

            return BlockMath.blockCenterToCorner(new Transformation(null, quaternion, null, null));
        });

        ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
        Direction side = state.getSide();

        if (side != null && side.get2DDataValue() > -1) {
            int faceOffset = 4 + Direction.NORTH.get2DDataValue() - facing.get2DDataValue();
            side = Direction.from2DDataValue((side.get2DDataValue() + faceOffset) % 4);
        }

        IQuadTransformer transformer = QuadTransformers.applying(transformation);
        
        for (BakedQuad quad : model.getQuads(state.getState(), side, state.getRand(), ModelData.EMPTY, null)) {
            BakedQuad transformedQuad = transformer.process(quad);
            quads.add(transformedQuad);
        }

        return quads.build();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return core.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return core.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return core.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        return core.getParticleIcon(modelData);
    }

    @Override
    public ItemOverrides getOverrides() {
        return core.getOverrides();
    }

    @Override
    public ItemTransforms getTransforms() {
        return core.getTransforms();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        // Return the render types for block rendering - typically cutout for pipes
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    @Override
    public List<BakedModel> getRenderPasses(net.minecraft.world.item.ItemStack itemStack, boolean fabulous) {
        // For item rendering, return this model
        return List.of(this);
    }

    @Override
    public List<RenderType> getRenderTypes(net.minecraft.world.item.ItemStack itemStack, boolean fabulous) {
        // For item rendering, use cutout render type
        return List.of(RenderType.cutout());
    }
} 