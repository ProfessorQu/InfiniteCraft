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
//        return new RecipeInput(RecipeInput.stacksToItemIds(itemStacks));
        List<Integer> inputList = new ArrayList<>(itemStacks.stream().map(ItemStack::getItem).map(Item::getRawId).toList());
        transformList(inputList);
        return new RecipeInput(inputList);
    }

    /**
     * Pad the list of item ids
     *
     * @param ids the list of item ids of the recipe
     */
    private static void transformList(List<Integer> ids) {
        // Convert 2x2 grid to 3x3
        if (ids.size() == 4) {
            ids.add(2, 0);
        }

        while (ids.size() < 9) {
            ids.add(0);
        }

        // Move recipe up
        while (ids.get(0) == 0 && ids.get(1) == 0 && ids.get(2) == 0) {
            for (int i = 0; i < 9; i++) {
                ids.set(i, Iterables.get(ids, i + 3, 0));
            }
        }

        // Move recipe left
        while (ids.get(0) == 0 && ids.get(3) == 0 && ids.get(6) == 0) {
            for (int i = 0; i < 9; i += 3) {
                ids.set(i, ids.get(i + 1));
                ids.set(i + 1, ids.get(i + 2));
                ids.set(i + 2, 0);
            }
        }

    }
}
