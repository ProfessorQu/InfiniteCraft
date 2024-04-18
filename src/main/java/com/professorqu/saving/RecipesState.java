package com.professorqu.saving;

import com.professorqu.InfiniteCraft;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipesState extends PersistentState {
    private final Map<RecipeInput, RecipeResult> recipes = new HashMap<>();

    private static final Type<RecipesState> TYPE = new Type<>(
            RecipesState::new,
            RecipesState::createFromNbt,
            null
    );

    private static final String RECIPES_KEY = "Recipes";
    private static final String INPUT_KEY = "Input";
    private static final String RESULT_KEY = "Result";
    private static final String COUNT_KEY = "Count";

    private static RecipesState createFromNbt(NbtCompound nbt) {
        RecipesState state = new RecipesState();
        NbtList recipesList = nbt.getList(RECIPES_KEY, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < recipesList.size(); i++) {
            NbtCompound recipeNbt = recipesList.getCompound(i);

            int[] inputArray = recipeNbt.getIntArray(INPUT_KEY);
            List<Integer> inputList = Arrays.stream(inputArray).boxed().toList();

            int itemId = recipeNbt.getInt(RESULT_KEY);
            int count = recipeNbt.getInt(COUNT_KEY);

            state.addItem(new RecipeInput(inputList), new RecipeResult(itemId, count));
        }

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();

        for (RecipeInput input : this.recipes.keySet()) {
            NbtCompound compound = new NbtCompound();

            compound.putIntArray(INPUT_KEY, input.input());
            compound.putInt(RESULT_KEY, this.recipes.get(input).getItemId());
            compound.putInt(COUNT_KEY, this.recipes.get(input).getCount());

            nbtList.add(compound);
        }

        nbt.put(RECIPES_KEY, nbtList);
        return nbt;
    }

    /**
     * Get the server state for the given server
     * @param server the server to get the state for
     * @return the server state of the server
     */
    public static RecipesState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        RecipesState state = manager.getOrCreate(TYPE, InfiniteCraft.RECIPES_FILE);
        state.markDirty();
        return state;
    }

    /**
     * Get the resulting item from the given getInput
     * @param input the getInput to get the result from
     * @return the resulting item
     */
    public @Nullable RecipeResult tryGetResult(RecipeInput input) {
        return this.recipes.get(input);
    }

    /**
     * Add an item to the list of stored recipes
     * @param input the input of the recipe
     * @param result the result of the recipe
     */
    public void addItem(RecipeInput input, RecipeResult result) {
        this.recipes.put(input, result);
    }
}
