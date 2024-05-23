package com.professorqu.generate;

import com.google.common.collect.Iterables;
import com.professorqu.InfiniteCraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class ItemGenerator {
    private static final Random RNG = new Random();
    private static final Identifier COMBINER_ID = new Identifier(InfiniteCraft.MOD_ID, "combiner");
    private static int seed;

    private static final List<ClampedEntityAttribute> PLAYER_ATTRIBUTES = new ArrayList<>();
    static {
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_ARMOR);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_ATTACK_DAMAGE);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_ATTACK_SPEED);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_MAX_HEALTH);
        PLAYER_ATTRIBUTES.add((ClampedEntityAttribute) EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    private static final float enchantmentChance = 0.2f;
    private static final float attributeChance = 0.2f;

    /**
     * Convert seed from long to int
     * @param longSeed the seed as long
     */
    public static void setSeed(long longSeed) {
        int seed = (int) longSeed;
        while (Math.abs(seed) > 1_000_000) {
            seed /= 2;
        }

        ItemGenerator.seed = seed;
    }

    /**
     * Generate a crafting recipe
     * @param inventory the inventory where the recipe is
     * @param world the world of the server
     * @return an optional recipe entry
     * @param <T> extends Recipe<C>
     */
    public static<C extends Inventory, T extends Recipe<C>> RecipeEntry<T> generateCraftingRecipe(
            RecipeInputInventory inventory, ServerWorld world
    ) {
        Ingredient defaultIngredient = Ingredient.ofStacks(new ItemStack(Items.STICK, 65));
        DefaultedList<Ingredient> ingredients = DefaultedList.of();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = new ItemStack(inventory.getStack(i).getItem());
            Ingredient ingredient;
            if (stack.isEmpty()) {
                ingredient = defaultIngredient;
            } else {
                ingredient = Ingredient.ofStacks(stack);
            }

            ingredients.add(ingredient);
        }
        transformList(ingredients, defaultIngredient);

        ItemStack itemStack = ItemGenerator.generateItemStack(inventory, world);

        // Create a new recipe
        RawShapedRecipe rawShapedRecipe = new RawShapedRecipe(
                inventory.getWidth(), inventory.getHeight(),
                ingredients, Optional.empty()
        );
        @SuppressWarnings("unchecked")
        T recipe = (T) new ShapedRecipe(
                "infinite",
                CraftingRecipeCategory.MISC,
                rawShapedRecipe,
                itemStack
        );

        return new RecipeEntry<>(InfiniteCraft.RECIPE_ID, recipe);
    }

    /**
     * Get the item resulting from the recipe
     * @param inventory the inventory of crafting
     * @param world the world that is on the server
     * @return the item result of the recipe
     */
    private static ItemStack generateItemStack(RecipeInputInventory inventory, ServerWorld world) {
        RNG.setSeed(ItemGenerator.getHashFromInventory(inventory) + ItemGenerator.seed);
        return ItemGenerator.getRandomItemStack(world);
    }

    /**
     * Create a random new ItemStack
     * @return a random ItemStack
     */
    private static ItemStack getRandomItemStack(ServerWorld world) {
        Item item = getRandomItem(world);
        int count = ItemGenerator.getRandomCount(item.getMaxCount());

        ItemStack stack = new ItemStack(item, count);

        if (item.getMaxCount() > 1) return stack;

        ItemGenerator.addRandomEnchantments(stack);
        ItemGenerator.addRandomAttributes(stack);

        return stack;
    }

    /**
     * Get a random item id
     * @return an item id
     */
    private static Item getRandomItem(ServerWorld world) {
        Item item = Registries.ITEM.get(RNG.nextInt(Registries.ITEM.size()));
        boolean canGenerateEgg = world.getEnderDragonFight() != null && world.getEnderDragonFight().hasPreviouslyKilled();
        while (!item.isEnabled(world.getEnabledFeatures()) ||
                item == Registries.ITEM.get(COMBINER_ID) ||
                (item == Items.DRAGON_EGG && !canGenerateEgg)) {
            item = Registries.ITEM.get(RNG.nextInt(Registries.ITEM.size()));
        }

        return item;
    }

    /**
     * Get a random count for the given item
     * @param maxCount the maximum count for the item
     * @return a random count
     */
    private static int getRandomCount(int maxCount) {
        if (maxCount == 1) return 1;

        return (int) Math.max(Math.round(RNG.nextGaussian(0, 4)) % maxCount, 1);
    }

    /**
     * Add random enchantments to an item stack
     * @param stack the item stack to add enchantments to
     */
    private static void addRandomEnchantments(ItemStack stack) {
        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            if (enchantment.isCursed()
                    || !enchantment.target.isAcceptableItem(stack.getItem())
                    || RNG.nextFloat() > enchantmentChance)
                continue;

            stack.addEnchantment(enchantment, 1 + RNG.nextInt(enchantment.getMaxLevel()));
        }
    }

    /**
     * Add random attributes to an item stack
     * @param stack the item stack to add attributes to
     */
    private static void addRandomAttributes(ItemStack stack) {
        ItemStack attributesStack = new ItemStack(stack.getItem());

        for (ClampedEntityAttribute attribute : ItemGenerator.PLAYER_ATTRIBUTES) {
            if (RNG.nextFloat() > attributeChance) continue;

            double value = Math.pow(RNG.nextGaussian(attribute.getMinValue(), 1), 2);
            value = Math.ceil(value * 10) / 10;
            var modifier = new EntityAttributeModifier(
                    attribute.getTranslationKey(),
                    attribute.clamp(value),
                    EntityAttributeModifier.Operation.ADDITION
            );

            ItemChanger.addAttribute(attributesStack, attribute, modifier);
        }

        ItemChanger.addAttributes(stack, attributesStack);
    }

    /**
     * Get a hash from the given inventory
     * @param inventory the inventory to generate the hash from
     * @return the generated hash
     */
    private static int getHashFromInventory(RecipeInputInventory inventory) {
        List<Integer> inputList = new ArrayList<>(inventory.getHeldStacks().stream().map(ItemStack::getItem).map(Item::getRawId).toList());
        transformList(inputList, 0);
        return inputList.hashCode();
    }

    /**
     * Pad the list of item list
     * @param list the list of item list of the recipe
     * @param defaultElement the default element to add
     */
    private static<T> void transformList(List<T> list, T defaultElement) {
        // Convert 2x2 grid to 3x3
        if (list.size() == 4) {
            list.add(2, defaultElement);
        }

        while (list.size() < 9) {
            list.add(defaultElement);
        }

        // Move recipe up
        while (list.get(0) == defaultElement && list.get(1) == defaultElement && list.get(2) == defaultElement) {
            for (int i = 0; i < 9; i++) {
                list.set(i, Iterables.get(list, i + 3, defaultElement));
            }
        }

        // Move recipe left
        while (list.get(0) == defaultElement && list.get(3) == defaultElement && list.get(6) == defaultElement) {
            for (int i = 0; i < 9; i += 3) {
                list.set(i, list.get(i + 1));
                list.set(i + 1, list.get(i + 2));
                list.set(i + 2, defaultElement);
            }
        }
    }
}
