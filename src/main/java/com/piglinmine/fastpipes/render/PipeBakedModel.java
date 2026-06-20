package com.piglinmine.fastpipes.render;

import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.piglinmine.fastpipes.FastPipes;
import com.piglinmine.fastpipes.block.PipeBlock;
import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentManager;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom block state model for the dynamic six-sided pipe geometry. Renders:
 *  - The "core" cube of the pipe.
 *  - An "extension" connector toward every side that is connected to another pipe.
 *  - A "straight" pass-through when (and only when) exactly two opposing sides are connected
 *    and nothing else is.
 *  - An "inventory_attachment" cube on sides where the pipe is connected to a vanilla
 *    inventory (or any capability provider) but has no attachment of its own.
 *  - A per-attachment model on sides where the pipe block entity reports an attachment.
 *
 * Per-direction parts are pre-baked by composing the unbaked {@link Variant} with
 * {@code withXRot}/{@code withYRot}. The variant for the NORTH direction is taken from
 * the JSON as-is — other directions are rotated to match what the multipart blockstates
 * used historically.
 */
public class PipeBakedModel implements DynamicBlockStateModel {
    private final BlockStateModelPart corePart;
    private final BlockStateModelPart[] extensionParts;
    private final BlockStateModelPart[] straightParts;
    private final BlockStateModelPart[] inventoryAttachmentParts;
    private final Map<Identifier, BlockStateModelPart[]> attachmentParts;
    private final Material.Baked particleMaterial;
    private final int materialFlags;

    public PipeBakedModel(BlockStateModelPart corePart,
                          BlockStateModelPart[] extensionParts,
                          BlockStateModelPart[] straightParts,
                          BlockStateModelPart[] inventoryAttachmentParts,
                          Map<Identifier, BlockStateModelPart[]> attachmentParts) {
        this.corePart = corePart;
        this.extensionParts = extensionParts;
        this.straightParts = straightParts;
        this.inventoryAttachmentParts = inventoryAttachmentParts;
        this.attachmentParts = attachmentParts;
        // 26.1.2: BlockStateModelPart exposes particleMaterial() (not particleIcon()).
        this.particleMaterial = corePart.particleMaterial();
        // OR together the material flags of every sub-part so the chunk batcher sets up
        // the correct layers (translucent / animated).
        int flags = corePart.materialFlags();
        for (BlockStateModelPart p : extensionParts) flags |= p.materialFlags();
        for (BlockStateModelPart p : straightParts) flags |= p.materialFlags();
        for (BlockStateModelPart p : inventoryAttachmentParts) flags |= p.materialFlags();
        for (BlockStateModelPart[] arr : attachmentParts.values()) {
            for (BlockStateModelPart p : arr) flags |= p.materialFlags();
        }
        this.materialFlags = flags;
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return materialFlags;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        boolean north = getBool(state, PipeBlock.NORTH);
        boolean east = getBool(state, PipeBlock.EAST);
        boolean south = getBool(state, PipeBlock.SOUTH);
        boolean west = getBool(state, PipeBlock.WEST);
        boolean up = getBool(state, PipeBlock.UP);
        boolean down = getBool(state, PipeBlock.DOWN);

        boolean invNorth = getBool(state, PipeBlock.INV_NORTH);
        boolean invEast = getBool(state, PipeBlock.INV_EAST);
        boolean invSouth = getBool(state, PipeBlock.INV_SOUTH);
        boolean invWest = getBool(state, PipeBlock.INV_WEST);
        boolean invUp = getBool(state, PipeBlock.INV_UP);
        boolean invDown = getBool(state, PipeBlock.INV_DOWN);

        // Resolve attachment ids per side from the block entity (client side).
        Identifier[] attachmentState = null;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PipeBlockEntity pipeBe) {
            AttachmentManager mgr = pipeBe.getAttachmentManager();
            if (mgr != null) {
                attachmentState = mgr.getState();
            }
        }

        // Geometry: core / straight / core + extensions.
        boolean noConnections = !(north || east || south || west || up || down);
        if (north && south && !east && !west && !up && !down) {
            parts.add(straightParts[Direction.NORTH.ordinal()]);
        } else if (!north && !south && east && west && !up && !down) {
            parts.add(straightParts[Direction.EAST.ordinal()]);
        } else if (!north && !south && !east && !west && up && down) {
            parts.add(straightParts[Direction.UP.ordinal()]);
        } else if (noConnections) {
            parts.add(corePart);
        } else {
            parts.add(corePart);
            if (north) parts.add(extensionParts[Direction.NORTH.ordinal()]);
            if (east) parts.add(extensionParts[Direction.EAST.ordinal()]);
            if (south) parts.add(extensionParts[Direction.SOUTH.ordinal()]);
            if (west) parts.add(extensionParts[Direction.WEST.ordinal()]);
            if (up) parts.add(extensionParts[Direction.UP.ordinal()]);
            if (down) parts.add(extensionParts[Direction.DOWN.ordinal()]);
        }

        // Attachment cubes for inventory-only connections (where there's no attachment of our own).
        if (invNorth && !north && !hasAttachment(attachmentState, Direction.NORTH)) {
            parts.add(inventoryAttachmentParts[Direction.NORTH.ordinal()]);
        }
        if (invEast && !east && !hasAttachment(attachmentState, Direction.EAST)) {
            parts.add(inventoryAttachmentParts[Direction.EAST.ordinal()]);
        }
        if (invSouth && !south && !hasAttachment(attachmentState, Direction.SOUTH)) {
            parts.add(inventoryAttachmentParts[Direction.SOUTH.ordinal()]);
        }
        if (invWest && !west && !hasAttachment(attachmentState, Direction.WEST)) {
            parts.add(inventoryAttachmentParts[Direction.WEST.ordinal()]);
        }
        if (invUp && !up && !hasAttachment(attachmentState, Direction.UP)) {
            parts.add(inventoryAttachmentParts[Direction.UP.ordinal()]);
        }
        if (invDown && !down && !hasAttachment(attachmentState, Direction.DOWN)) {
            parts.add(inventoryAttachmentParts[Direction.DOWN.ordinal()]);
        }

        // Per-direction attachment cube models.
        if (attachmentState != null) {
            for (Direction dir : Direction.values()) {
                Identifier id = attachmentState[dir.ordinal()];
                if (id == null) continue;
                BlockStateModelPart[] parts6 = attachmentParts.get(id);
                if (parts6 != null) {
                    parts.add(parts6[dir.ordinal()]);
                }
            }
        }
    }

    private static boolean hasAttachment(Identifier[] state, Direction dir) {
        return state != null && state[dir.ordinal()] != null;
    }

    private static boolean getBool(BlockState state, net.minecraft.world.level.block.state.properties.BooleanProperty prop) {
        return state.hasProperty(prop) && state.getValue(prop);
    }

    /**
     * Rotates {@code base} so that its NORTH-facing baseline now faces {@code dir}.
     * Mirrors the rotations used by the legacy multipart blockstates:
     *   NORTH: identity, EAST: y90, SOUTH: y180, WEST: y270, UP: x270, DOWN: x90.
     */
    public static Variant rotateForDirection(Variant base, Direction dir) {
        return switch (dir) {
            case NORTH -> base;
            case EAST -> base.withYRot(Quadrant.R90);
            case SOUTH -> base.withYRot(Quadrant.R180);
            case WEST -> base.withYRot(Quadrant.R270);
            case UP -> base.withXRot(Quadrant.R270);
            case DOWN -> base.withXRot(Quadrant.R90);
        };
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Unbaked
    // ──────────────────────────────────────────────────────────────────────────

    public static final Identifier MODEL_TYPE = Identifier.fromNamespaceAndPath(FastPipes.MOD_ID, "pipe");

    public record Unbaked(Variant core,
                          Variant extension,
                          Variant straight,
                          Variant inventoryAttachment,
                          Map<Identifier, Variant> attachments) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<PipeBakedModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Variant.CODEC.fieldOf("core").forGetter(PipeBakedModel.Unbaked::core),
                Variant.CODEC.fieldOf("extension").forGetter(PipeBakedModel.Unbaked::extension),
                Variant.CODEC.fieldOf("straight").forGetter(PipeBakedModel.Unbaked::straight),
                Variant.CODEC.fieldOf("inventory_attachment").forGetter(PipeBakedModel.Unbaked::inventoryAttachment),
                Codec.unboundedMap(Identifier.CODEC, Variant.CODEC).optionalFieldOf("attachments", Map.of()).forGetter(PipeBakedModel.Unbaked::attachments))
                .apply(inst, PipeBakedModel.Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            core.resolveDependencies(resolver);
            extension.resolveDependencies(resolver);
            straight.resolveDependencies(resolver);
            inventoryAttachment.resolveDependencies(resolver);
            for (Variant v : attachments.values()) {
                v.resolveDependencies(resolver);
            }
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            BlockStateModelPart corePart = core.bake(baker);

            BlockStateModelPart[] extensionParts = bakeAllSides(baker, extension);
            BlockStateModelPart[] straightParts = bakeAllSides(baker, straight);
            BlockStateModelPart[] inventoryAttachmentParts = bakeAllSides(baker, inventoryAttachment);

            Map<Identifier, BlockStateModelPart[]> attachmentParts = new HashMap<>();
            for (Map.Entry<Identifier, Variant> e : attachments.entrySet()) {
                attachmentParts.put(e.getKey(), bakeAllSides(baker, e.getValue()));
            }

            return new PipeBakedModel(corePart, extensionParts, straightParts, inventoryAttachmentParts, attachmentParts);
        }

        private static BlockStateModelPart[] bakeAllSides(ModelBaker baker, Variant base) {
            BlockStateModelPart[] out = new BlockStateModelPart[Direction.values().length];
            for (Direction dir : Direction.values()) {
                out[dir.ordinal()] = rotateForDirection(base, dir).bake(baker);
            }
            return out;
        }
    }
}
