package com.professorqu.helpers;

import com.professorqu.InfiniteCraft;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;

public class RecipesManager {
    private static final Map<RecipeInput, Integer> RECIPES = new HashMap<>();

    public static PersistentState.Type<RecipesState> getPersistentStateType() {
        return new PersistentState.Type<>(RecipesManager::createState, RecipesManager::stateFromNbt, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    }

    private static RecipesState createState() {
        return new RecipesState(RECIPES);
    }

    private static RecipesState stateFromNbt(NbtCompound nbt) {
        return RecipesManager.createState().readNbt(nbt);
    }

    public static void loadRecipes(MinecraftServer server) {
        InfiniteCraft.LOGGER.info("Loading...");
        PersistentStateManager manager = server.getOverworld().getChunkManager().getPersistentStateManager();
        RecipesState state = manager.getOrCreate(RecipesManager.getPersistentStateType(), "recipes");
        Map<RecipeInput, Integer> recipes = state.getRecipes();
        RECIPES.putAll(recipes);

        InfiniteCraft.LOGGER.info("RECIPES length: " + RECIPES.size());
        InfiniteCraft.LOGGER.info("RECIPES: " + RECIPES);
    }

    public static void saveRecipes(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getChunkManager().getPersistentStateManager();
        RecipesState state = new RecipesState(new HashMap<>(RECIPES));
        manager.set("recipes", state);

        manager.save();
        RECIPES.clear();
    }

    static Item getItem(RecipeInput input) {
        Integer itemId = RECIPES.get(input);
        if (itemId == null) return null;

        return Registries.ITEM.get(itemId);
    }

    static void addItem(RecipeInput input, Item item) {
        RECIPES.put(input, Registries.ITEM.getRawId(item));
    }
}
