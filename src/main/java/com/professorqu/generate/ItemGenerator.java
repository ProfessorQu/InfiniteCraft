package com.professorqu.generate;

import com.google.common.collect.Iterables;
import com.professorqu.InfiniteCraft;
import com.professorqu.block.ModBlocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class ItemGenerator {
    public static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");

    private static int seed;

    private static final Random RNG = new Random();

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
     * @param <C> extends Inventory
     * @param <T> extends Recipe<C>
     */
    public static<C extends Inventory, T extends Recipe<C>> Optional<RecipeEntry<T>> generateCraftingRecipe(
            RecipeInputInventory inventory, ServerWorld world
    ) {
        Ingredient ingredient = Ingredient.ofStacks(inventory.getHeldStacks().stream());
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(1, ingredient);

        ItemStack itemStack = ItemGenerator.generateItemStack(inventory, world);

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
    private static ItemStack generateItemStack(RecipeInputInventory inventory, ServerWorld world) {
        RNG.setSeed(ItemGenerator.getHashFromInventory(inventory) + ItemGenerator.seed);
        return ItemGenerator.getRandomItemStack(world.getEnabledFeatures());
    }

    /**
     * Create a random new ItemStack
     * @return a random ItemStack
     */
    private static ItemStack getRandomItemStack(FeatureSet enabledFeatures) {
        Item item = getRandomItem(enabledFeatures);
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
    private static Item getRandomItem(FeatureSet enabledFeatures) {
        Item item = Registries.ITEM.get(RNG.nextInt(Registries.ITEM.size()));
        while (!item.isEnabled(enabledFeatures) || item == ModBlocks.COMBINER_BLOCK.asItem()) {
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
        transformList(inputList);
        return inputList.hashCode();
    }

    /**
     * Pad the list of item ids
     * @param ids the list of item ids of the recipe
     */
    private static void transformList(List<Integer> ids) {
        // Convert 2x2 grid to 3x3
        if (ids.size() == 4) {
            ids.add(2, 0);
        }

        while (ids.size() < 9) {
            ids.add(0);
        }

        // Move recipe up
        while (ids.get(0) == 0 && ids.get(1) == 0 && ids.get(2) == 0) {
            for (int i = 0; i < 9; i++) {
                ids.set(i, Iterables.get(ids, i + 3, 0));
            }
        }

        // Move recipe left
        while (ids.get(0) == 0 && ids.get(3) == 0 && ids.get(6) == 0) {
            for (int i = 0; i < 9; i += 3) {
                ids.set(i, ids.get(i + 1));
                ids.set(i + 1, ids.get(i + 2));
                ids.set(i + 2, 0);
            }
        }
    }
}
