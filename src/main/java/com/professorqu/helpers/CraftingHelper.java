package com.professorqu.helpers;

import com.professorqu.InfiniteCraft;
import com.professorqu.generate.Generator;
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
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;


public class CraftingHelper {
    private static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");
    private static Generator CURRENT_GENERATOR;

    public static void createGenerator(MinecraftServer server) {
        InfiniteCraft.LOGGER.info("Created new Generator");
        CURRENT_GENERATOR = new Generator(server);
    }

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
        ItemStack itemStack = CraftingHelper.getCraftedItem(inventory, world);

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

    private static ItemStack getCraftedItem(RecipeInputInventory inventory, ServerWorld world) {
        MinecraftServer server = world.getServer();

        RecipeInput input = RecipeInput.fromItemStacks(inventory.getHeldStacks());
        RecipesState state = RecipesState.getServerState(server);

        RecipeResult result = state.tryGetResult(input);
        if (result == null) {
            result = CraftingHelper.generateRecipeResult(input, world);
            state.addItem(input, result);
        }

        return new ItemStack(Registries.ITEM.get(result.getItemId()), result.getCount());
    }

    private static RecipeResult generateRecipeResult(RecipeInput input, ServerWorld world) {
        return CURRENT_GENERATOR.generate(
                ArrayUtils.toPrimitive(input.input().toArray(new Integer[0])),
                world.getEnabledFeatures()
        );
    }
}
