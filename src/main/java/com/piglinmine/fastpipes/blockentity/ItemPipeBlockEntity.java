package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.NetworkManager;
import com.piglinmine.fastpipes.network.item.ItemNetwork;
import com.piglinmine.fastpipes.network.item.routing.DestinationPathCache;
import com.piglinmine.fastpipes.network.pipe.Destination;
import com.piglinmine.fastpipes.network.pipe.DestinationType;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.attachment.Attachment;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeType;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemBounceBackTransportCallback;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemInsertTransportCallback;
import com.piglinmine.fastpipes.network.pipe.transport.callback.ItemPipeGoneTransportCallback;
import com.piglinmine.fastpipes.routing.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ItemPipeBlockEntity extends PipeBlockEntity {
    private ItemPipeType type;
    private List<ItemTransportProps> props = new ArrayList<>();

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(getBlockEntityType(state), pos, state);
        this.type = getItemPipeType(state);
    }

    private static BlockEntityType<?> getBlockEntityType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_ITEM_PIPE.get()) {
            return FPipesBlockEntities.BASIC_ITEM_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_ITEM_PIPE.get()) {
            return FPipesBlockEntities.IMPROVED_ITEM_PIPE.get();
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_ITEM_PIPE.get()) {
            return FPipesBlockEntities.ADVANCED_ITEM_PIPE.get();
        }
        // Fallback to basic if unknown
        return FPipesBlockEntities.BASIC_ITEM_PIPE.get();
    }

    private static ItemPipeType getItemPipeType(BlockState state) {
        if (state.getBlock() == FPipesBlocks.BASIC_ITEM_PIPE.get()) {
            return ItemPipeType.BASIC;
        } else if (state.getBlock() == FPipesBlocks.IMPROVED_ITEM_PIPE.get()) {
            return ItemPipeType.IMPROVED;
        } else if (state.getBlock() == FPipesBlocks.ADVANCED_ITEM_PIPE.get()) {
            return ItemPipeType.ADVANCED;
        }
        // Fallback to basic if unknown
        return ItemPipeType.BASIC;
    }

    public static void tick(ItemPipeBlockEntity blockEntity) {
        blockEntity.props.forEach(ItemTransportProps::tick);
    }

    public List<ItemTransportProps> getProps() {
        return props;
    }

    public void setProps(List<ItemTransportProps> props) {
        this.props = props;
    }

    // NeoForge Capability Handler — exposes the pipe as a push target for adjacent machines
    public IItemHandler getItemHandler(Direction side) {
        if (level == null || level.isClientSide()) return null;
        Pipe pipe = NetworkManager.get(level).getPipe(worldPosition);
        if (!(pipe instanceof ItemPipe itemPipe)) return null;
        if (!(itemPipe.getNetwork() instanceof ItemNetwork network)) return null;
        DestinationPathCache cache = network.getDestinationPathCache();
        if (cache == null) return null;

        return new IItemHandler() {
            @Override public int getSlots() { return 1; }
            @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
            @Override public int getSlotLimit(int slot) { return 64; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return !stack.isEmpty(); }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                BlockPos sourcePos = worldPosition.relative(side);
                Destination destination = cache.findNearestDestination(
                    worldPosition, d -> isPushDestinationApplicable(d, sourcePos, side, stack));
                if (destination == null) return stack;
                if (simulate) return ItemStack.EMPTY;
                Path<BlockPos> path = cache.getPath(worldPosition, destination);
                if (path == null) return stack;
                itemPipe.addTransport(new ItemTransport(
                    stack.copy(),
                    sourcePos,
                    destination.getReceiver(),
                    path.toQueue(),
                    new ItemInsertTransportCallback(destination.getReceiver(), destination.getIncomingDirection(), stack),
                    new ItemBounceBackTransportCallback(sourcePos, side, stack),
                    new ItemPipeGoneTransportCallback(stack)
                ));
                return ItemStack.EMPTY;
            }
        };
    }

    private boolean isPushDestinationApplicable(Destination d, BlockPos sourcePos, Direction side, ItemStack stack) {
        if (d.getReceiver().equals(sourcePos)) return false;
        BlockEntity be = level.getBlockEntity(d.getReceiver());
        if (be == null) return false;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, d.getReceiver(), d.getIncomingDirection().getOpposite());
        if (handler == null) return false;
        Attachment att = d.getConnectedPipe().getAttachmentManager().getAttachment(d.getIncomingDirection());
        if (att != null && !att.canInsert(stack)) return false;
        return ItemHandlerHelper.insertItem(handler, stack, true).isEmpty();
    }

    protected void spawnDrops(Pipe pipe) {
        super.spawnDrops(pipe);

        if (pipe instanceof ItemPipe itemPipe) {
            for (ItemTransport transport : itemPipe.getTransports()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), transport.getStack());
            }
        }
    }

    protected Pipe createPipe(Level level, BlockPos pos) {
        return new ItemPipe(level, pos, type);
    }
} 