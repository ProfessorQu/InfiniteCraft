package com.professorqu.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class RecipeDisplaySlot extends Slot {
    public RecipeDisplaySlot(Inventory inventory, int index) {
        super(inventory, index,
                -62 + (index % 3) * 18,
                8 + (index / 3) * 18
        );
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
