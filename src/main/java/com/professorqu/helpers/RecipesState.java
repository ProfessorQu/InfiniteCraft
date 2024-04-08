package com.professorqu.helpers;

import net.minecraft.nbt.*;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipesState extends PersistentState {
    private final Map<RecipeInput, Integer> recipes;

    public RecipesState(Map<RecipeInput, Integer> recipes) {
        this.recipes = recipes;
    }

    public RecipesState readNbt(NbtCompound nbt) {
        NbtList list = nbt.getList("Recipes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound nbtCompound = list.getCompound(i);
            NbtList nbtInput = nbtCompound.getList("Input", NbtElement.INT_ARRAY_TYPE);
            int result = nbtCompound.getInt("Result");

            List<Integer> input = new ArrayList<>();
            for (int j = 0; j < nbtInput.size(); j++) {
                input.add(nbtInput.getInt(j));
            }

            this.recipes.put(new RecipeInput(input), result);
        }

        return this;
    }

    private NbtList getNbtRecipes() {
        NbtList nbtList = new NbtList();

        for (RecipeInput input : this.recipes.keySet()) {
            NbtCompound compound = new NbtCompound();

            compound.putIntArray("Input", input.getInputStacks());
            compound.putInt("Result", this.recipes.get(input));

            nbtList.add(compound);
        }

        return nbtList;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("Recipes", this.getNbtRecipes());
        return nbt;
    }

    public Map<RecipeInput, Integer> getRecipes() {
        return this.recipes;
    }

    @Override
    public void save(File file) {
        this.setDirty(true);
        super.save(file);
    }
}
