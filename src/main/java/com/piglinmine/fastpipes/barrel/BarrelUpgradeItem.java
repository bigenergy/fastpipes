package com.piglinmine.fastpipes.barrel;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BarrelUpgradeItem extends Item {
    private final BarrelTier targetTier;

    public BarrelUpgradeItem(BarrelTier targetTier) {
        super(new Properties().stacksTo(16));
        this.targetTier = targetTier;
    }

    public BarrelTier getTargetTier() {
        return targetTier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.fastpipes.barrel_upgrade",
            Component.translatable("block.fastpipes." + targetTier.getRegistryName()))
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
