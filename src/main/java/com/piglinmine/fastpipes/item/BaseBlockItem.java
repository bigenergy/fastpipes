package com.piglinmine.fastpipes.item;

import com.piglinmine.fastpipes.block.PipeBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class BaseBlockItem extends BlockItem {
    public BaseBlockItem(Block block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
        if (getBlock() instanceof PipeBlock pipe) {
            pipe.appendPipeTooltip(tooltipAdder);
        }
    }
}
