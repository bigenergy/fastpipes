package com.piglinmine.fastpipes.integration.jei;

import com.piglinmine.fastpipes.FPipesContainerMenus;
import com.piglinmine.fastpipes.menu.TerminalContainerMenu;
import com.piglinmine.fastpipes.network.FastPipesNetwork;
import com.piglinmine.fastpipes.network.message.TerminalRecipeTransferMessage;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TerminalRecipeTransferHandler implements IRecipeTransferHandler<TerminalContainerMenu, RecipeHolder<CraftingRecipe>> {

    @Override
    public Class<TerminalContainerMenu> getContainerClass() {
        return TerminalContainerMenu.class;
    }

    @Override
    public Optional<MenuType<TerminalContainerMenu>> getMenuType() {
        return Optional.of(FPipesContainerMenus.TERMINAL.get());
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(TerminalContainerMenu container, RecipeHolder<CraftingRecipe> recipe,
                                                IRecipeSlotsView recipeSlotsView, Player player,
                                                boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }

        List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        List<ItemStack> ingredients = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (i < slotViews.size()) {
                Optional<ItemStack> displayed = slotViews.get(i).getDisplayedIngredient(VanillaTypes.ITEM_STACK);
                ingredients.add(displayed.orElse(ItemStack.EMPTY));
            } else {
                ingredients.add(ItemStack.EMPTY);
            }
        }

        FastPipesNetwork.sendToServer(new TerminalRecipeTransferMessage(ingredients));
        return null;
    }
}
