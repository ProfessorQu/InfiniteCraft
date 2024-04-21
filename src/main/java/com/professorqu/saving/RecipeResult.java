package com.professorqu.saving;

import net.minecraft.registry.Registries;

public record RecipeResult(int itemId, int count) {
    /**
     * Create a new RecipeResult
     * @param itemId the id of the resulting item
     * @param count the number of items from the result
     */
    public static RecipeResult createRecipeResult(float itemId, float count) {
        int itemIdInt = Math.round(itemId) % Registries.ITEM.size();
        int countInt = Math.round(count) % Registries.ITEM.get(itemIdInt).getMaxCount();
        countInt = Math.max(countInt, 1);

        return new RecipeResult(itemIdInt, countInt);
    }

    public RecipeResult withItemId(int itemId) {
        return new RecipeResult(itemId, count());
    }
}
