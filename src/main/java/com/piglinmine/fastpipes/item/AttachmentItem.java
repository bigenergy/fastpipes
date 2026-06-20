package com.piglinmine.fastpipes.item;

import com.piglinmine.fastpipes.network.pipe.attachment.AttachmentFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AttachmentItem extends Item {
    private final AttachmentFactory type;

    public AttachmentItem(AttachmentFactory type, Item.Properties properties) {
        super(properties);

        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipAdder, flag);

        // TODO 1.21.11: appendHoverText changed to take Consumer<Component> instead of List<Component>; bridging here
        List<Component> tooltip = new ArrayList<>();
        type.addInformation(tooltip);
        for (Component line : tooltip) {
            tooltipAdder.accept(line);
        }
    }

    public AttachmentFactory getFactory() {
        return type;
    }
}
