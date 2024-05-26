package com.professorqu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.professorqu.screen.slot.RecipeResultSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Recipes {
    public static final String SEED_SEPARATOR = "::";
    public static final String RECIPES_SEPARATOR = "#";
    public static final String RECIPE_SEPARATOR = "->";
    public static final String INGREDIENT_SEPARATOR = " ";

    private static final List<RecipeEntry<?>> SAVED_RECIPES = new ArrayList<>();
    private static final Inventory CURRENT_RECIPE = new SimpleInventory(9);

    @Nullable
    private static RecipeEntry<?> lastRecipe;

    private static File recipesFile;

    public static void initialize(MinecraftClient client) {
        recipesFile = new File(client.runDirectory, "recipes.txt");
        try {
            recipesFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reset() {
        SAVED_RECIPES.clear();
        CURRENT_RECIPE.clear();
        lastRecipe = null;
    }

    public static void save() {
        try {
            List<String> recipes = new ArrayList<>();
            for (RecipeEntry<?> recipeEntry : SAVED_RECIPES) {
                Recipe<?> recipe = recipeEntry.value();
                ItemStack resultStack = recipe.getResult(null);
                NbtCompound compound = new NbtCompound();
                resultStack.writeNbt(compound);
                String result = compound.asString();

                String ingredients = recipe.getIngredients()
                        .stream().map(Recipes::ingredientToString)
                        .collect(Collectors.joining(INGREDIENT_SEPARATOR));

                String recipeString = ingredients + RECIPE_SEPARATOR + result;
                recipes.add(recipeString);
            }

            String recipesString = InfiniteCraftClient.seedHash + SEED_SEPARATOR + String.join(RECIPES_SEPARATOR, recipes);

            BufferedReader bufferedReader = new BufferedReader(new FileReader(recipesFile));
            StringBuilder buffer = new StringBuilder();

            boolean replaced = false;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (Long.parseLong(line.split(SEED_SEPARATOR)[0]) == InfiniteCraftClient.seedHash) {
                    buffer.append(recipesString);
                    replaced = true;
                } else {
                    buffer.append(line);
                }
                buffer.append('\n');
            }

            if (!replaced) {
                buffer.append(recipesString);
                buffer.append('\n');
            }

            FileOutputStream fileOutputStream = new FileOutputStream(recipesFile);
            fileOutputStream.write(buffer.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Recipes.reset();
        }
    }

    public static void load() {
        Recipes.reset();

        try {
            Scanner scanner = new Scanner(recipesFile);
            long seedHash;
            String recipesString;
            if (!scanner.hasNextLine())
                return;

            do {
                String line = scanner.nextLine();
                String[] strings = line.split(SEED_SEPARATOR);
                seedHash = Long.parseLong(strings[0]);
                if (strings.length > 1)
                    recipesString = strings[1];
                else
                    recipesString = null;
            } while (scanner.hasNextLine() && seedHash != InfiniteCraftClient.seedHash);

            if (seedHash != InfiniteCraftClient.seedHash || recipesString == null || recipesString.isEmpty())
                return;

            String[] recipes = recipesString.split(RECIPES_SEPARATOR);

            for (String recipeString : recipes) {
                String[] recipe = recipeString.split(RECIPE_SEPARATOR);
                ItemStack result = ItemStack.fromNbt(StringNbtReader.parse(recipe[1]));
                String ingredientsString = recipe[0];
                List<Ingredient> ingredientsList = Arrays.stream(ingredientsString.split(INGREDIENT_SEPARATOR))
                        .map(Recipes::stringToIngredient).toList();
                DefaultedList<Ingredient> ingredients = DefaultedList.of();
                ingredients.addAll(ingredientsList);

                // Create a new recipe
                RawShapedRecipe rawShapedRecipe = new RawShapedRecipe(
                        3, 3, ingredients, Optional.empty()
                );
                SAVED_RECIPES.add(new RecipeEntry<>(InfiniteCraft.RECIPE_ID, new ShapedRecipe(
                        "infinite",
                        CraftingRecipeCategory.MISC,
                        rawShapedRecipe,
                        result
                )));
            }
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setLastRecipe(@Nullable RecipeEntry<ShapedRecipe> recipeEntry) {
        lastRecipe = recipeEntry;
    }

    public static void saveLastRecipe() {
        if (lastRecipe == null) return;

        boolean notSaved = true;
        for (RecipeEntry<?> recipeEntry : SAVED_RECIPES) {
            if (entriesMatch(recipeEntry, lastRecipe)) {
                notSaved = false;
                break;
            }
        }

        if (notSaved)
            SAVED_RECIPES.add(lastRecipe);
    }

    public static void removeRecipe(RecipeEntry<?> entry) {
        SAVED_RECIPES.removeIf(loopEntry -> entriesMatch(entry, loopEntry));
    }

    public static void refresh(CraftingScreenHandler handler) {
        for (int i = 0; i < InfiniteCraftClient.MAX_SLOTS; i++) {
            RecipeResultSlot slot = (RecipeResultSlot) handler.getSlot(46 + i);
            if (i < Recipes.getRecipeEntriesSize()) {
                slot.setRecipeEntry(Recipes.getRecipeEntry(i));
                slot.setEnabled(true);
            } else {
                slot.clearRecipeEntry();
                slot.setEnabled(false);
            }
        }
    }

    public static RecipeEntry<?> getRecipeEntry(int index) {
        return SAVED_RECIPES.get(index);
    }

    public static int getRecipeEntriesSize() {
        return SAVED_RECIPES.size();
    }

    public static void setCurrentRecipe(RecipeEntry<?> recipe) {
        int slotIndex = 0;
        List<ItemStack> itemStacks = recipe.value().getIngredients().stream()
                .map(Recipes::ingredientToStack).toList();
        for (ItemStack itemStack : itemStacks) {
            CURRENT_RECIPE.setStack(slotIndex++, itemStack);
        }
    }

    private static String ingredientToString(Ingredient ingredient) {
        return String.valueOf(Item.getRawId(ingredientToStack(ingredient).getItem()));
    }

    private static Ingredient stringToIngredient(String string) {
        Item item = Item.byRawId(Integer.parseInt(string));
        ItemStack stack = item == Items.AIR ? new ItemStack(Items.STICK, 65) : new ItemStack(item);
        return Ingredient.ofStacks(stack);
    }

    private static ItemStack ingredientToStack(Ingredient ingredient) {
        ItemStack itemStack = ingredient.getMatchingStacks()[0];
        return itemStack.getCount() == 65 ? ItemStack.EMPTY : itemStack;
    }

    private static boolean entriesMatch(RecipeEntry<?> recipeEntry1, RecipeEntry<?> recipeEntry2) {
        DefaultedList<Ingredient> ingredients1 = recipeEntry1.value().getIngredients();
        DefaultedList<Ingredient> ingredients2 = recipeEntry2.value().getIngredients();

        if (ingredients1.size() != ingredients2.size())
            return false;

        for (int i = 0; i < ingredients1.size(); i++) {
            if (ingredients1.get(i).getMatchingStacks()[0].getItem() != ingredients2.get(i).getMatchingStacks()[0].getItem())
                return false;
        }

        return true;
    }

    public static Inventory getCurrentRecipe() {
        return CURRENT_RECIPE;
    }
}
