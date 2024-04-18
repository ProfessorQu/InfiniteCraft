package com.professorqu.mixin;

import com.google.gson.Gson;
import com.professorqu.helpers.CraftingHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.*;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends JsonDataLoader {
    public RecipeManagerMixin(Gson gson, String dataType) {
        super(gson, dataType);
    }

    @Shadow protected abstract <C extends Inventory, T extends Recipe<C>> Map<Identifier, RecipeEntry<T>> getAllOfType(RecipeType<T> type);

    @Inject(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private <C extends Inventory, T extends Recipe<C>> void getFirstMatch(RecipeType<T> type, C inventory, World world, CallbackInfoReturnable<Optional<RecipeEntry<T>>> cir) {
        if (world.isClient || inventory.isEmpty()) return;
        Optional<RecipeEntry<T>> recipeEntry = this.getAllOfType(type).values().stream().filter(recipe -> recipe.value().matches(inventory, world)).findFirst();

        if (recipeEntry.isPresent()) {
            cir.setReturnValue(recipeEntry);
        } else {
            cir.setReturnValue(CraftingHelper.getRecipeEntry(type, (RecipeInputInventory) inventory, (ServerWorld) world));
        }

        cir.cancel();
    }
}
