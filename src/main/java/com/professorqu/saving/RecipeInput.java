package com.professorqu.saving;

import com.google.common.collect.Iterables;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record RecipeInput(List<Integer> input) {
    /**
     * Create a RecipeInput from item stacks
     * @param itemStacks the item stacks to create the input from
     * @return the resulting input
     */
    public static RecipeInput fromItemStacks(List<ItemStack> itemStacks) {
        return new RecipeInput(convertToItemIds(itemStacks));
    }

    /**
     * Convert a list of ItemStacks to a list of item ids
     *
     * @param itemStacks a list of ItemStacks to convert to a list of item ids
     * @return a list of item ids
     */
    private static List<Integer> convertToItemIds(List<ItemStack> itemStacks) {
        List<Integer> inputList = new ArrayList<>(itemStacks.stream().map(ItemStack::getItem).map(Item::getRawId).toList());
        if (inputList.size() == 4) {
            inputList.add(2, 0);
        }

        while (inputList.size() < 9) {
            inputList.add(0);
        }

        while (inputList.get(0) == 0 && inputList.get(1) == 0 && inputList.get(2) == 0) {
            for (int i = 0; i < 9; i++) {
                inputList.set(i, Iterables.get(inputList, i + 3, 0));
            }
        }

        while (inputList.get(0) == 0 && inputList.get(3) == 0 && inputList.get(6) == 0) {
            for (int i = 0; i < 9; i += 3) {
                inputList.set(i, inputList.get(i + 1));
                inputList.set(i + 1, inputList.get(i + 2));
                inputList.set(i + 2, 0);
            }
        }

        return inputList;
    }

    /**
     * Get the input of the recipe
     * @return the input of the recipe
     */
    @Override
    public List<Integer> input() {
        return this.input;
    }
}
