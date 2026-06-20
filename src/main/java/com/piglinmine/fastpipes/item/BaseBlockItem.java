package com.piglinmine.fastpipes.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BaseBlockItem extends BlockItem {
    // 1.21.11: id is injected into Properties by DeferredRegister.Items.registerItem(...)
    public BaseBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
} 