package com.professorqu.mixin.client;

import com.professorqu.InfiniteCraftClient;
import com.professorqu.Recipes;
import com.professorqu.screen.slot.RecipeDisplaySlot;
import com.professorqu.screen.slot.RecipeResultSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractRecipeScreenHandler<RecipeInputInventory>  {
    public CraftingScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void initInject(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        for (int i = 0; i < InfiniteCraftClient.MAX_SLOTS; i++) {
            RecipeResultSlot slot = new RecipeResultSlot(i);
            if (i < Recipes.getRecipeEntriesSize()) {
                slot.setRecipeEntry(Recipes.getRecipeEntry(i));
                slot.setEnabled(true);
            }
            this.addSlot(slot);
        }

        for (int i = 0; i < 9; i++) {
            RecipeDisplaySlot slot = new RecipeDisplaySlot(Recipes.getCurrentRecipe(), i);
            this.addSlot(slot);
        }
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private static void updateResultInject(
            ScreenHandler handler, World world, PlayerEntity player,
            RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory,
            CallbackInfo ci) {
        RecipeEntry<?> recipeEntry = resultInventory.getLastRecipe();
        if (recipeEntry != null && recipeEntry.value() instanceof ShapedRecipe) {
            @SuppressWarnings("unchecked")
            RecipeEntry<ShapedRecipe> shapedRecipeRecipeEntry = (RecipeEntry<ShapedRecipe>) recipeEntry;
            Recipes.setLastRecipe(shapedRecipeRecipeEntry);
        }
    }
}
