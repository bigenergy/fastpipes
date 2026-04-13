package com.piglinmine.fastpipes.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.List;

public class WrenchItem extends Item {
    /**
     * Common wrench ToolAction used for cross-mod compatibility.
     * Other mods can check for this action to detect wrench items.
     */
    public static final ToolAction WRENCH_ROTATE = ToolAction.get("wrench_rotate");
    public static final ToolAction WRENCH_DISASSEMBLE = ToolAction.get("wrench_disassemble");

    public WrenchItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == WRENCH_ROTATE || toolAction == WRENCH_DISASSEMBLE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.fastpipes.wrench.hint1").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.fastpipes.wrench.hint2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Checks if the given ItemStack is a wrench — either our own or from another mod
     * that supports the wrench_rotate ToolAction.
     */
    public static boolean isWrench(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof WrenchItem) return true;
        return stack.canPerformAction(WRENCH_ROTATE);
    }
}
