package com.professorqu.saving;

import net.minecraft.registry.Registries;

public class RecipeResult {
    private int itemId;
    private final int count;

    public RecipeResult(float itemId, float count) {
        int itemIdInt = Math.round(itemId) % Registries.ITEM.size();
        int countInt = Math.round(count) % Registries.ITEM.get(itemIdInt).getMaxCount();
        countInt = Math.max(countInt, 1);

        this.itemId = itemIdInt;
        this.count = countInt;
    }

    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return this.count;
    }
}
