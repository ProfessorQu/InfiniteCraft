package com.professorqu.mixin;

import com.professorqu.generate.ItemGenerator;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin<C extends Inventory, T extends Recipe<C>> {
    @Shadow protected abstract Map<Identifier, RecipeEntry<T>> getAllOfType(RecipeType<T> type);

    @Inject(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void getFirstMatchInject(RecipeType<T> type, C inventory, World world, CallbackInfoReturnable<Optional<RecipeEntry<T>>> cir) {
        if (world.isClient || inventory.isEmpty() || type != RecipeType.CRAFTING) return;
        Optional<RecipeEntry<T>> vanillaRecipe = this.getAllOfType(type).values().stream().filter(recipe -> recipe.value().matches(inventory, world)).findFirst();

        if (vanillaRecipe.isPresent()) {
            cir.setReturnValue(vanillaRecipe);
        } else {
            RecipeEntry<T> recipeEntry = ItemGenerator.generateCraftingRecipe((RecipeInputInventory) inventory, (ServerWorld) world);
            cir.setReturnValue(Optional.of(recipeEntry));
        }

        cir.cancel();
    }
}
