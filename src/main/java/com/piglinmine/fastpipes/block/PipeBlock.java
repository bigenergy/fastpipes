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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class PipeBlock extends Block implements EntityBlock {
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

    public PipeBlock(PipeShapeCache shapeCache) {
        super(BlockBehaviour.Properties.of()
            .destroyTime(0.35F)
            .explosionResistance(0.35F)
            .noOcclusion()
        );

        this.shapeCache = shapeCache;

        this.registerDefaultState(defaultBlockState()
            .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false)
            .setValue(INV_NORTH, false).setValue(INV_EAST, false).setValue(INV_SOUTH, false).setValue(INV_WEST, false).setValue(INV_UP, false).setValue(INV_DOWN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(
            NORTH, EAST, SOUTH, WEST, UP, DOWN,
            INV_NORTH, INV_EAST, INV_SOUTH, INV_WEST, INV_UP, INV_DOWN
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (!level.isClientSide) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null && pipe.getNetwork() != null) {
                pipe.getNetwork().scanGraph(level, pos);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        // Dye: right-click with dye to color pipe
        if (held.getItem() instanceof DyeItem dyeItem) {
            if (!level.isClientSide) {
                Pipe pipe = NetworkManager.get(level).getPipe(pos);
                if (pipe != null) {
                    DyeColor newColor = dyeItem.getDyeColor();
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
            if (!level.isClientSide) {
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
            if (!level.isClientSide) {
                level.destroyBlock(pos, !player.isCreative(), player);
            }
            return InteractionResult.SUCCESS;
        }

        // Wrench: right-click to toggle disconnect on a side (supports cross-mod wrenches)
        if (WrenchItem.isWrench(held)) {
            Direction dir = getAttachmentDirectionClicked(pos, hit.getLocation());
            if (dir == null) {
                dir = hit.getDirection(); // fallback to the face that was clicked
            }
            return toggleDisconnect(level, pos, dir);
        }

        Direction dirClicked = getAttachmentDirectionClicked(pos, hit.getLocation());

        if (dirClicked != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (held.isEmpty() && player.isCrouching()) {
                return removeAttachment(level, pos, dirClicked);
            } else if (blockEntity instanceof PipeBlockEntity pipeBlockEntity && pipeBlockEntity.getAttachmentManager().hasAttachment(dirClicked)) {
                return openAttachmentContainer(player, pos, pipeBlockEntity.getAttachmentManager(), dirClicked);
            } else if (held.getItem() instanceof AttachmentItem) {
                return addAttachment(player, level, pos, held, dirClicked);
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult toggleDisconnect(Level level, BlockPos pos, Direction dir) {
        if (!level.isClientSide) {
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
        if (!level.isClientSide) {
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

    private InteractionResult removeAttachment(Level level, BlockPos pos, Direction dir) {
        if (!level.isClientSide) {
            Pipe pipe = NetworkManager.get(level).getPipe(pos);

            if (pipe != null && pipe.getAttachmentManager().hasAttachment(dir)) {
                Attachment attachment = pipe.getAttachmentManager().getAttachment(dir);

                pipe.getAttachmentManager().removeAttachmentAndScanGraph(dir);
                NetworkManager.get(level).setDirty();

                pipe.sendBlockUpdate();
                level.setBlockAndUpdate(pos, getState(level.getBlockState(pos), level, pos));

                Block.popResource(level, pos.relative(dir), attachment.getDrop());
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
        return getState(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        // On client, don't recalculate connections — trust the server-sent block state.
        if (world instanceof Level lvl && lvl.isClientSide) {
            return state;
        }
        return getState(state, world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return shapeCache.getShape(state, world, pos, ctx);
    }

    public BlockState getState(BlockState currentState, LevelAccessor world, BlockPos pos) {
        return currentState
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
} 