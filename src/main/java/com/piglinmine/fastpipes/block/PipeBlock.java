package com.piglinmine.fastpipes.block;

import com.piglinmine.fastpipes.blockentity.PipeBlockEntity;
import com.piglinmine.fastpipes.item.AttachmentItem;
import com.piglinmine.fastpipes.item.WrenchItem;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentManager;
import com.piglinmine.fastpipes.network.pipe.attachment.sensor.SensorAttachment;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeCache;
import com.piglinmine.fastpipes.network.pipe.shape.PipeShapeProps;
import com.piglinmine.fastpipes.util.Raytracer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class PipeBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final BooleanProperty INV_NORTH = BooleanProperty.create("inv_north");
    public static final BooleanProperty INV_EAST = BooleanProperty.create("inv_east");
    public static final BooleanProperty INV_SOUTH = BooleanProperty.create("inv_south");
    public static final BooleanProperty INV_WEST = BooleanProperty.create("inv_west");
    public static final BooleanProperty INV_UP = BooleanProperty.create("inv_up");
    public static final BooleanProperty INV_DOWN = BooleanProperty.create("inv_down");

    private final PipeShapeCache shapeCache;

    // 1.21.11: Properties must carry the registry id (set by DeferredRegister.Blocks.registerBlock).
    // Subclasses receive Properties from the factory and apply pipe-specific tuning here.
    public PipeBlock(PipeShapeCache shapeCache, BlockBehaviour.Properties properties) {
        super(properties
            .destroyTime(0.35F)
            .explosionResistance(0.35F)
            .noOcclusion()
        );

        this.shapeCache = shapeCache;

        this.registerDefaultState(defaultBlockState()
            .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false)
            .setValue(INV_NORTH, false).setValue(INV_EAST, false).setValue(INV_SOUTH, false).setValue(INV_WEST, false).setValue(INV_UP, false).setValue(INV_DOWN, false)
            .setValue(BlockStateProperties.WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(
            NORTH, EAST, SOUTH, WEST, UP, DOWN,
            INV_NORTH, INV_EAST, INV_SOUTH, INV_WEST, INV_UP, INV_DOWN,
            BlockStateProperties.WATERLOGGED
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        if (!level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null && pipe.getNetwork() != null) {
                pipe.getNetwork().scanGraph(level, pos);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack held = player.getMainHandItem();

        // Dye: right-click with dye to color pipe
        // 26.1.2: DyeItem.getDyeColor() removed; the color is now a DataComponents.DYE on the stack.
        if (held.getItem() instanceof DyeItem) {
            if (!level.isClientSide()) {
                Pipe pipe = NetworkManager.get(level).getPipe(pos);
                if (pipe != null) {
                    DyeColor newColor = held.get(net.minecraft.core.component.DataComponents.DYE);
                    if (newColor == null) return InteractionResult.PASS;
                    if (pipe.getColor() != newColor) {
                        pipe.setColor(newColor);
                        NetworkManager.get(level).setDirty();

                        // Rescan network — color change may split/merge networks
                        if (pipe.getNetwork() != null) {
                            pipe.getNetwork().scanGraph(level, pos);
                        }
                        // Also rescan neighbors that might now disconnect
                        for (Direction dir : Direction.values()) {
                            Pipe neighbor = NetworkManager.get(level).getPipe(pos.relative(dir));
                            if (neighbor != null && neighbor.getNetwork() != null) {
                                neighbor.getNetwork().scanGraph(level, pos.relative(dir));
                            }
                        }

                        // Update block states with UPDATE_KNOWN_SHAPE to prevent cascading
                        int flags = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

                        // Update the dyed pipe itself
                        pipe.sendBlockUpdate();
                        BlockState newState = getState(level.getBlockState(pos), level, pos);
                        level.setBlock(pos, newState, flags);

                        // Update all neighbor pipe block states
                        for (Direction dir : Direction.values()) {
                            BlockPos neighborPos = pos.relative(dir);
                            BlockState neighborState = level.getBlockState(neighborPos);
                            if (neighborState.getBlock() instanceof PipeBlock neighborPipeBlock) {
                                Pipe neighborPipe = NetworkManager.get(level).getPipe(neighborPos);
                                if (neighborPipe != null) {
                                    neighborPipe.sendBlockUpdate();
                                }
                                BlockState newNeighborState = neighborPipeBlock.getState(neighborState, level, neighborPos);
                                level.setBlock(neighborPos, newNeighborState, flags);
                            }
                        }

                        if (!player.isCreative()) {
                            held.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Water bucket: right-click to remove color
        if (held.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide()) {
                Pipe pipe = NetworkManager.get(level).getPipe(pos);
                if (pipe != null && pipe.getColor() != null) {
                    pipe.setColor(null);
                    NetworkManager.get(level).setDirty();

                    if (pipe.getNetwork() != null) {
                        pipe.getNetwork().scanGraph(level, pos);
                    }
                    for (Direction dir : Direction.values()) {
                        Pipe neighbor = NetworkManager.get(level).getPipe(pos.relative(dir));
                        if (neighbor != null && neighbor.getNetwork() != null) {
                            neighbor.getNetwork().scanGraph(level, pos.relative(dir));
                        }
                    }

                    int flags = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

                    pipe.sendBlockUpdate();
                    level.setBlock(pos, getState(level.getBlockState(pos), level, pos), flags);

                    for (Direction dir : Direction.values()) {
                        BlockPos neighborPos = pos.relative(dir);
                        BlockState neighborState = level.getBlockState(neighborPos);
                        if (neighborState.getBlock() instanceof PipeBlock neighborPipeBlock) {
                            Pipe neighborPipe = NetworkManager.get(level).getPipe(neighborPos);
                            if (neighborPipe != null) {
                                neighborPipe.sendBlockUpdate();
                            }
                            level.setBlock(neighborPos, neighborPipeBlock.getState(neighborState, level, neighborPos), flags);
                        }
                    }

                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Wrench: shift+right-click to break pipe (supports cross-mod wrenches via ItemAbility)
        if (WrenchItem.isWrench(held) && player.isCrouching()) {
            if (!level.isClientSide()) {
                level.destroyBlock(pos, !player.isCreative(), player);
            }
            return InteractionResult.SUCCESS;
        }

        // Wrench: right-click on bare pipe side to toggle disconnect.
        // If the clicked side has an attachment, open its GUI instead.
        if (WrenchItem.isWrench(held)) {
            Direction dir = getAttachmentDirectionClicked(pos, hit.getLocation());
            if (dir == null) {
                dir = hit.getDirection(); // fallback to the face that was clicked
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PipeBlockEntity pipeBlockEntity
                && pipeBlockEntity.getAttachmentManager().hasAttachment(dir)) {
                return openAttachmentContainer(player, pos, pipeBlockEntity.getAttachmentManager(), dir);
            }
            return toggleDisconnect(level, pos, dir);
        }

        Direction dirClicked = getAttachmentDirectionClicked(pos, hit.getLocation());

        // Fallback: if click didn't precisely land on attachment bbox, but an attachment
        // exists on the clicked face, use that face. Prevents GUI "dead zones" on attachment edges.
        if (dirClicked == null) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PipeBlockEntity pipeBlockEntity
                && pipeBlockEntity.getAttachmentManager().hasAttachment(hit.getDirection())) {
                dirClicked = hit.getDirection();
            }
        }

        if (dirClicked != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (held.isEmpty() && player.isCrouching()) {
                return removeAttachment(level, pos, dirClicked, player);
            } else if (blockEntity instanceof PipeBlockEntity pipeBlockEntity && pipeBlockEntity.getAttachmentManager().hasAttachment(dirClicked)) {
                return openAttachmentContainer(player, pos, pipeBlockEntity.getAttachmentManager(), dirClicked);
            } else if (held.getItem() instanceof AttachmentItem) {
                return addAttachment(player, level, pos, held, dirClicked);
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult toggleDisconnect(Level level, BlockPos pos, Direction dir) {
        if (!level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null) {
                boolean disconnected = pipe.toggleDisconnect(dir);
                NetworkManager.get(level).setDirty();

                // Rescan the network to update connections
                if (pipe.getNetwork() != null) {
                    pipe.getNetwork().scanGraph(level, pos);
                }

                // Also rescan the neighbor pipe's network if it exists
                Pipe neighborPipe = NetworkManager.get(level).getPipe(pos.relative(dir));
                if (neighborPipe != null && neighborPipe.getNetwork() != null) {
                    neighborPipe.getNetwork().scanGraph(level, pos.relative(dir));
                }

                pipe.sendBlockUpdate();
                level.setBlockAndUpdate(pos, getState(level.getBlockState(pos), level, pos));

                // Update neighbor block state too
                BlockState neighborState = level.getBlockState(pos.relative(dir));
                if (neighborState.getBlock() instanceof PipeBlock neighborPipeBlock) {
                    level.setBlockAndUpdate(pos.relative(dir),
                        neighborPipeBlock.getState(neighborState, level, pos.relative(dir)));
                }

                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, disconnected ? 0.5F : 0.7F);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult addAttachment(Player player, Level level, BlockPos pos, ItemStack attachment, Direction dir) {
        if (!level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null && !pipe.getAttachmentManager().hasAttachment(dir)) {
                AttachmentFactory type = ((AttachmentItem) attachment.getItem()).getFactory();
                if (!type.canPlaceOnPipe(this)) {
                    return InteractionResult.SUCCESS;
                }

                pipe.getAttachmentManager().setAttachmentAndScanGraph(dir, type.create(pipe, dir));
                NetworkManager.get(level).setDirty();

                pipe.sendBlockUpdate();
                level.setBlockAndUpdate(pos, getState(level.getBlockState(pos), level, pos));

                if (!player.isCreative()) {
                    attachment.shrink(1);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult removeAttachment(Level level, BlockPos pos, Direction dir, Player player) {
        if (!level.isClientSide()) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null && pipe.getAttachmentManager().hasAttachment(dir)) {
                Attachment attachment = pipe.getAttachmentManager().getAttachment(dir);

                pipe.getAttachmentManager().removeAttachmentAndScanGraph(dir);

                // If this side was disconnected (e.g. wrench-toggle while attachment was installed),
                // clear the disconnect so the pipe reconnects to whatever's there now.
                if (pipe.isDisconnected(dir)) {
                    pipe.toggleDisconnect(dir);
                }

                NetworkManager.get(level).setDirty();

                pipe.sendBlockUpdate();
                level.setBlockAndUpdate(pos, getState(level.getBlockState(pos), level, pos));

                // Give directly to the player (handles sync + overflow drop properly).
                ItemHandlerHelper.giveItemToPlayer(player, attachment.getDrop());
            }

            return InteractionResult.SUCCESS;
        } else {
            return ((PipeBlockEntity) level.getBlockEntity(pos)).getAttachmentManager().hasAttachment(dir) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
    }

    private InteractionResult openAttachmentContainer(Player player, BlockPos pos, AttachmentManager attachmentManager, Direction dir) {
        if (player instanceof ServerPlayer) {
            attachmentManager.openAttachmentContainer(dir, (ServerPlayer) player);
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return getState(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos())
            .setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        // On client, don't recalculate connections — trust the server-sent block state.
        if (level instanceof Level lvl && lvl.isClientSide()) {
            return state;
        }
        // TODO 1.21.11: getState requires LevelAccessor; LevelReader from updateShape may be ok if hasConnection only reads
        if (level instanceof LevelAccessor accessor) {
            return getState(state, accessor, pos);
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return shapeCache.getShape(state, world, pos, ctx);
    }

    public BlockState getState(BlockState currentState, LevelAccessor world, BlockPos pos) {
        boolean waterlogged = currentState.hasProperty(BlockStateProperties.WATERLOGGED)
            && currentState.getValue(BlockStateProperties.WATERLOGGED);
        return currentState
            .setValue(BlockStateProperties.WATERLOGGED, waterlogged)
            .setValue(NORTH, hasConnection(world, pos, Direction.NORTH))
            .setValue(EAST, hasConnection(world, pos, Direction.EAST))
            .setValue(SOUTH, hasConnection(world, pos, Direction.SOUTH))
            .setValue(WEST, hasConnection(world, pos, Direction.WEST))
            .setValue(UP, hasConnection(world, pos, Direction.UP))
            .setValue(DOWN, hasConnection(world, pos, Direction.DOWN))
            .setValue(INV_NORTH, hasInvConnection(world, pos, Direction.NORTH))
            .setValue(INV_EAST, hasInvConnection(world, pos, Direction.EAST))
            .setValue(INV_SOUTH, hasInvConnection(world, pos, Direction.SOUTH))
            .setValue(INV_WEST, hasInvConnection(world, pos, Direction.WEST))
            .setValue(INV_UP, hasInvConnection(world, pos, Direction.UP))
            .setValue(INV_DOWN, hasInvConnection(world, pos, Direction.DOWN));
    }

    // Removed @Override - method signature changed in MC 1.21.1
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        Direction dirClicked = getAttachmentDirectionClicked(pos, target.getLocation());

        if (dirClicked != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PipeBlockEntity pipeBlockEntity) {
                return pipeBlockEntity.getAttachmentManager().getPickBlock(dirClicked);
            }
        }

        // Return default item stack since super method has different signature
        return new ItemStack(this);
    }

    @Nullable
    public Direction getAttachmentDirectionClicked(BlockPos pos, Vec3 hit) {
        if (Raytracer.inclusiveContains(PipeShapeProps.NORTH_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.NORTH;
        }

        if (Raytracer.inclusiveContains(PipeShapeProps.EAST_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.EAST;
        }

        if (Raytracer.inclusiveContains(PipeShapeProps.SOUTH_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.SOUTH;
        }

        if (Raytracer.inclusiveContains(PipeShapeProps.WEST_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.WEST;
        }

        if (Raytracer.inclusiveContains(PipeShapeProps.UP_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.UP;
        }

        if (Raytracer.inclusiveContains(PipeShapeProps.DOWN_ATTACHMENT_SHAPE.bounds().move(pos), hit)) {
            return Direction.DOWN;
        }

        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(BlockState state) {
        return true; // Pipes can be redstone sources when sensor attachment is active
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PipeBlockEntity pipeBlockEntity) {
            // Check if there's an active sensor on the opposite side
            // (direction is the side of the NEIGHBOR block querying, so we check opposite = our side facing that neighbor)
            Attachment att = pipeBlockEntity.getAttachmentManager().getAttachment(direction.getOpposite());
            if (att instanceof SensorAttachment sensor && sensor.isSignalActive()) {
                return 15;
            }

            // Also emit signal from any side if any sensor on this pipe is active
            for (Direction dir : Direction.values()) {
                Attachment a = pipeBlockEntity.getAttachmentManager().getAttachment(dir);
                if (a instanceof SensorAttachment s && s.isSignalActive()) {
                    return 15;
                }
            }
        }
        return 0;
    }

    protected abstract boolean hasConnection(LevelAccessor world, BlockPos pos, Direction direction);

    protected abstract boolean hasInvConnection(LevelAccessor world, BlockPos pos, Direction direction);

    /** Subclasses override to add tier/rate/capacity lines to the pipe item's hover tooltip. */
    public void appendPipeTooltip(Consumer<Component> tooltip) {
        // default: no-op
    }

    public static ChatFormatting tierColor(String tier) {
        return switch (tier) {
            case "BASIC" -> ChatFormatting.GRAY;
            case "IMPROVED" -> ChatFormatting.YELLOW;
            case "ADVANCED" -> ChatFormatting.GREEN;
            case "ELITE" -> ChatFormatting.AQUA;
            case "ULTIMATE" -> ChatFormatting.LIGHT_PURPLE;
            case "CREATIVE" -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }

    public static String tierDisplay(String tier) {
        if (tier == null || tier.isEmpty()) return tier;
        return tier.charAt(0) + tier.substring(1).toLowerCase();
    }
}
