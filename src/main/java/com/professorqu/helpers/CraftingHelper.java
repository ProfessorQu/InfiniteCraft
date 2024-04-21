package com.professorqu.helpers;

import com.professorqu.InfiniteCraft;
import com.professorqu.saving.RecipeResult;
import com.professorqu.saving.RecipeInput;
import com.professorqu.saving.RecipesState;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;


public class CraftingHelper {
    private static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");
    private static Generator CURRENT_GENERATOR;

    /**
     * Create a new Generator
     * @param server the server the Generator is run on
     */
    public static void createGenerator(MinecraftServer server) {
        InfiniteCraft.LOGGER.info("Created new Generator");
        CURRENT_GENERATOR = new Generator(server);
    }

    /**
     * Destroy the current Generator
     */
    public static void destroyGenerator() {
        InfiniteCraft.LOGGER.info("Destroyed current Generator");
        CURRENT_GENERATOR = null;
    }

    /**
     * Get the recipe entry
     * @param type the recipe type
     * @param inventory the inventory where the recipe is
     * @param world the world of the server
     * @return an optional recipe entry
     * @param <C> extends Inventory
     * @param <T> extends Recipe<C>
     */
    public static<C extends Inventory, T extends Recipe<C>> Optional<RecipeEntry<T>> getRecipeEntry(
            RecipeType<T> type, RecipeInputInventory inventory, ServerWorld world) {
        if (type != RecipeType.CRAFTING) return Optional.empty();

        Ingredient ingredient = Ingredient.ofStacks(inventory.getHeldStacks().stream());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(1, ingredient);

        ItemStack itemStack = CraftingHelper.getResultingItem(inventory, world);

        // Create a new recipe
        RawShapedRecipe rawShapedRecipe = new RawShapedRecipe(
                inventory.getWidth(), inventory.getHeight(),
                ingredients, Optional.empty()
        );
        @SuppressWarnings("unchecked")
        T recipe = (T) new ShapedRecipe(
                "misc", CraftingRecipeCategory.MISC,
                rawShapedRecipe,
                itemStack
        );

        return Optional.of(new RecipeEntry<>(RECIPE_ID, recipe));
    }

    /**
     * Get the item resulting from the recipe
     * @param inventory the inventory of crafting
     * @param world the world that is on the server
     * @return the item result of the recipe
     */
    private static ItemStack getResultingItem(RecipeInputInventory inventory, ServerWorld world) {
        RecipesState state = RecipesState.getServerState(world.getServer());

        // Create input from inventory
        RecipeInput input = RecipeInput.fromItemStacks(inventory.getHeldStacks());
        RecipeResult result = state.tryGetResult(input);

        if (result == null) {
            // Generate the new result
            Pair<RecipeResult, Boolean> pair = CraftingHelper.generateRecipeResult(input, world);
            result = pair.getLeft();
            // Save the item if it should be saved
            if (pair.getRight()) {
                state.addItem(input, result);
            }
        }

        return new ItemStack(Registries.ITEM.get(result.itemId()), result.count());
    }

    /**
     * Generate the result of the recipe
     * @param input the input of the recipe
     * @param world the world of the recipe
     * @return a pair of the recipe's result and a boolean to determine whether it should be saved
     */
    private static Pair<RecipeResult, Boolean> generateRecipeResult(RecipeInput input, ServerWorld world) {
        int[] inputs = ArrayUtils.toPrimitive(input.input().toArray(new Integer[0]));
        return CURRENT_GENERATOR.generate(inputs, world.getEnabledFeatures());
    }
}
