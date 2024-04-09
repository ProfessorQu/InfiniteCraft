package com.professorqu.saving;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RecipeInput(List<Integer> input) {
    /**
     * Convert a list of ItemStacks to a list of item ids
     * @param itemStacks    a list of ItemStacks to convert to a list of item ids
     * @return              a list of item ids
     */
    public static @NotNull List<Integer> convertToItemIds(@NotNull List<ItemStack> itemStacks) {
        return itemStacks.stream().map(ItemStack::getItem).map(Item::getRawId).toList();
    }

    /**
     * Get the input of the recipe
     * @return  the input of the recipe
     */
    public List<Integer> getInput() {
        return this.input;
    }
}
