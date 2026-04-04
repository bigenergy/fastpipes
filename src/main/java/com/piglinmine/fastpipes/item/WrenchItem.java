package com.piglinmine.fastpipes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class WrenchItem extends Item {
    /**
     * Common wrench ItemAbility used for cross-mod compatibility.
     * Other mods can check for this action to detect wrench items.
     */
    public static final ItemAbility WRENCH_ROTATE = ItemAbility.get("wrench_rotate");
    public static final ItemAbility WRENCH_DISASSEMBLE = ItemAbility.get("wrench_disassemble");

    public WrenchItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility == WRENCH_ROTATE || itemAbility == WRENCH_DISASSEMBLE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.fastpipes.wrench.hint1").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.fastpipes.wrench.hint2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Checks if the given ItemStack is a wrench — either our own or from another mod
     * that supports the wrench_rotate ItemAbility.
     */
    public static boolean isWrench(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof WrenchItem) return true;
        return stack.canPerformAction(WRENCH_ROTATE);
    }
}
