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
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public class CraftingHelper<C extends Inventory, T extends Recipe<C>> {
    private static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");

    private final RecipeType<T> type;
    private final RecipeInputInventory inventory;
    private final World world;
//    private final int seed;

    public CraftingHelper(RecipeType<T> type, C inventory, World world) {
        this.type = type;
        this.inventory = (RecipeInputInventory) inventory;
        this.world = world;

//        ServerWorld serverWorld = (ServerWorld) this.world;
//        this.seed = (int) serverWorld.getSeed();
    }

    /**
     * Generate a recipe entry for the inventory
     * @return  a recipe entry with a generated item
     */
    public Optional<RecipeEntry<T>> getRecipeEntry() {
        if (this.type != RecipeType.CRAFTING) return Optional.empty();

        Ingredient ingredient = Ingredient.ofStacks(inventory.getHeldStacks().stream());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(1, ingredient);
        ItemStack itemStack = this.getCraftedItem();

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

    private ItemStack getCraftedItem() {
        MinecraftServer server = world.getServer();
        if (server == null) return null;

        RecipeInput input = RecipeInput.fromItemStacks(this.inventory.getHeldStacks());
        RecipesState state = RecipesState.getServerState(server);

        RecipeResult result = state.tryGetResult(input);
        if (result == null) {
            result = this.generateResult(input);
            state.addItem(input, result);
        }

        return new ItemStack(Registries.ITEM.get(result.getItemId()), result.getCount());
    }

    private @NotNull RecipeResult generateResult(RecipeInput input) {
        return Generator.generate(
                ArrayUtils.toPrimitive(input.input().toArray(new Integer[0])),
//                this.seed,
                this.world.getEnabledFeatures()
        );
    }
}
