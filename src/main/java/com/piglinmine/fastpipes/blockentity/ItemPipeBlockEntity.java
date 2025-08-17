package com.piglinmine.fastpipes.blockentity;

import com.piglinmine.fastpipes.FPipesBlockEntities;
import com.piglinmine.fastpipes.FPipesBlocks;
import com.piglinmine.fastpipes.network.pipe.Pipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipe;
import com.piglinmine.fastpipes.network.pipe.item.ItemPipeType;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransport;
import com.piglinmine.fastpipes.network.pipe.transport.ItemTransportProps;
import net.minecraft.core.BlockPos;
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

    // NeoForge Capability Handler
    public net.neoforged.neoforge.items.IItemHandler getItemHandler(net.minecraft.core.Direction side) {
        // TODO: Return actual item handler when network system is available
        // For now, return null to indicate no capability
        return null;
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