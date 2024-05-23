package com.professorqu.screen.slot;

import com.professorqu.InfiniteCraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.slot.Slot;

import java.util.Optional;

public class RecipeResultSlot extends Slot {
    private Optional<RecipeEntry<?>> optionalRecipeEntry = Optional.empty();
    private boolean enabled = false;

    public RecipeResultSlot(int index) {
        super(new NoInventory(), index,
                186 + (index % InfiniteCraftClient.GRID_COLUMNS) * 18,
                8 + (index / InfiniteCraftClient.GRID_COLUMNS) * 18
        );
    }

    @Override
    public ItemStack getStack() {
        if (optionalRecipeEntry.isPresent()) {
            return optionalRecipeEntry.get().value().getResult(null);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) { }

    @Override
    public void markDirty() { }

    @Override
    public int getMaxItemCount() {
        return getStack().getMaxCount();
    }

    @Override
    public ItemStack takeStack(int amount) {
        return this.getStack();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    public Optional<RecipeEntry<?>> getOptionalRecipeEntry() {
        return this.optionalRecipeEntry;
    }

    public void setRecipeEntry(RecipeEntry<?> optionalRecipeEntry) {
        this.optionalRecipeEntry = Optional.of(optionalRecipeEntry);
    }

    public void clearRecipeEntry() {
        this.optionalRecipeEntry = Optional.empty();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
