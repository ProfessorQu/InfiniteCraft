package com.professorqu.mixin;

import com.google.gson.Gson;
import com.professorqu.generate.Generator;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin<C extends Inventory, T extends Recipe<C>> extends JsonDataLoader {
    public RecipeManagerMixin(Gson gson, String dataType) {
        super(gson, dataType);
    }

    @Shadow protected abstract Map<Identifier, RecipeEntry<T>> getAllOfType(RecipeType<T> type);

    @Inject(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void getFirstMatch(RecipeType<T> type, C inventory, World world, CallbackInfoReturnable<Optional<RecipeEntry<T>>> cir) {
        if (world.isClient || inventory.isEmpty() || type != RecipeType.CRAFTING) return;
        Optional<RecipeEntry<T>> recipeEntry = this.getAllOfType(type).values().stream().filter(recipe -> recipe.value().matches(inventory, world)).findFirst();

        if (recipeEntry.isPresent()) {
            cir.setReturnValue(recipeEntry);
        } else {
            cir.setReturnValue(Generator.generateCraftingRecipe((RecipeInputInventory) inventory, (ServerWorld) world));
        }

        cir.cancel();
    }

    @Inject(method = "getAllMatches", at = @At("HEAD"), cancellable = true)
    private void getAllMatches(RecipeType<T> type, C inventory, World world, CallbackInfoReturnable<List<RecipeEntry<T>>> cir) {
        if (world.isClient || inventory.isEmpty() || type != RecipeType.SMITHING) return;
        List<RecipeEntry<T>> result = this.getAllOfType(type).values().stream().filter(recipe -> recipe.value().matches(inventory, world)).sorted(Comparator.comparing(recipeEntry -> recipeEntry.value().getResult(world.getRegistryManager()).getTranslationKey())).collect(Collectors.toList());

        if (result.size() > 0) {
            cir.setReturnValue(result);
        } else {
            cir.setReturnValue(Generator.generateSmithingRecipe(inventory));
        }

        cir.cancel();
    }
}
