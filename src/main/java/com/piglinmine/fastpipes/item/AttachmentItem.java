package com.piglinmine.fastpipes.item;

import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AttachmentItem extends Item {
    private final AttachmentFactory type;

    public AttachmentItem(AttachmentFactory type) {
        super(new Item.Properties());

        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        type.addInformation(tooltip);

        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.hint1").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.fastpipes.attachment.hint2").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public AttachmentFactory getFactory() {
        return type;
    }
} 