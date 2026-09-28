package com.piglinmine.fastpipes.integration.jei;

import com.piglinmine.fastpipes.menu.BaseContainerMenu;
import com.piglinmine.fastpipes.menu.slot.FluidFilterSlot;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.UpdateFilterEntryMessage;
import com.piglinmine.fastpipes.screen.BaseScreen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows fluids to be dragged from JEI straight onto the fluid filter slots of the
 * attachment screens. Fluids that have no bucket item (for example Iron's Spells
 * inks) work too, because {@link FastPipesJEIPlugin} registers every non-empty
 * fluid as a JEI ingredient. Dragging a fluid container item (e.g. a water bucket)
 * onto a slot marks the fluid it holds.
 */
public class FluidFilterGhostIngredientHandler<T extends BaseScreen<? extends BaseContainerMenu>>
        implements IGhostIngredientHandler<T> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
        BaseContainerMenu menu = gui.getMenu();
        List<FluidFilterSlot> fluidSlots = menu.getFluidSlots();
        if (fluidSlots.isEmpty()) {
            return List.of();
        }

        FluidStack fluid = resolveFluid(ingredient);
        if (fluid.isEmpty()) {
            return List.of();
        }

        List<Target<I>> targets = new ArrayList<>(fluidSlots.size());
        for (FluidFilterSlot slot : fluidSlots) {
            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return new Rect2i(gui.getGuiLeftPos() + slot.x, gui.getGuiTopPos() + slot.y, 16, 16);
                }

                @Override
                public void accept(I pushed) {
                    applyFluid(menu, slot, fluid);
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    private static FluidStack resolveFluid(ITypedIngredient<?> ingredient) {
        FluidStack fluid = ingredient.getIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        if (!fluid.isEmpty()) {
            return fluid;
        }

        ItemStack stack = ingredient.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        }

        return FluidStack.EMPTY;
    }

    private static void applyFluid(BaseContainerMenu menu, FluidFilterSlot slot, FluidStack fluid) {
        int index = slot.getSlotIndex();

        // A "#tag" override takes precedence over the fluid entry, so drop it first
        // (Extractor / Inserter menus sync this to the server; the other attachment
        // menus have no tag-override support at all).
        if (!menu.getTagOverride(index).isEmpty()) {
            menu.setTagOverride(index, "");
        }

        // Show the fluid in the slot immediately.
        slot.getFluidInventory().setFluid(index, fluid);

        // The server owns the real filter inventory.
        BlockPos pos = menu.getPos();
        Direction dir = menu.getDirection();
        if (pos != null && dir != null) {
            String fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
            FastPipesNetwork.sendToServer(new UpdateFilterEntryMessage(pos, dir, index, fluidId));
        }
    }
}
