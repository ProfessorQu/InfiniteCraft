package com.professorqu.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.List;

public record RecipeInput(List<Integer> input) {
    public static List<Integer> convertItemStacksToIntegers(List<ItemStack> itemStacks) {
        return itemStacks.stream().map(ItemStack::getItem).map(Registries.ITEM::getRawId).toList();
    }

    public List<Integer> getInputStacks() {
        return this.input;
    }
}
