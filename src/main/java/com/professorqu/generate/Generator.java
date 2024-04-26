package com.professorqu.generate;

import com.professorqu.InfiniteCraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class Generator {
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

        Generator.seed = seed;
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

        ItemStack itemStack = Generator.generateItemStack(inventory, world);

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
        RNG.setSeed(inventory.hashCode() + Generator.seed);

        ItemStack result = Generator.getRandomItemStack();
        while (!result.getItem().isEnabled(world.getEnabledFeatures())) {
            result = Generator.getRandomItemStack();
        }

        return result;
    }

    /**
     * Create a random new ItemStack
     * @return a random ItemStack
     */
    private static ItemStack getRandomItemStack() {
        Item item = getRandomItem();
        int count = Generator.getRandomCount(item.getMaxCount());

        ItemStack stack = new ItemStack(item, count);

        if (item.getMaxCount() > 1) return stack;

        addRandomEnchantments(stack);
        addRandomAttributes(stack);

        return stack;
    }

    /**
     * Get a random item id
     * @return an item id
     */
    private static Item getRandomItem() {
        return Registries.ITEM.get(RNG.nextInt(Registries.ITEM.size()));
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
        for (ClampedEntityAttribute attribute : Generator.PLAYER_ATTRIBUTES) {
            if (RNG.nextFloat() > attributeChance) continue;

            double value = Math.pow(RNG.nextGaussian(attribute.getMinValue(), 1), 2);
            value = Math.ceil(value * 10) / 10;
            var modifier = new EntityAttributeModifier(
                    attribute.getTranslationKey(),
                    attribute.clamp(value),
                    EntityAttributeModifier.Operation.ADDITION
            );

            addAttribute(stack, attribute, modifier);
        }
    }

    /**
     * Generate a smithing recipe
     * @param inventory the inventory
     * @return a smithing recipe
     * @param <C> extends Inventory
     * @param <T> extends Recipe<C>
     */
    public static<C extends Inventory, T extends Recipe<C>> List<RecipeEntry<T>> generateSmithingRecipe(
            Inventory inventory
    ) {
        @SuppressWarnings("unchecked")
        T recipe = (T) new SmithingCombineRecipe(
                inventory.getStack(1),
                inventory.getStack(2)
        );

        return List.of(new RecipeEntry<>(RECIPE_ID, recipe));
    }

    /**
     * Add an attribute to a stack
     * @param stack the stack to add the attribute to
     * @param attribute the attribute to add
     * @param modifier the modifier to add
     */
    public static void addAttribute(ItemStack stack, EntityAttribute attribute, EntityAttributeModifier modifier) {
        for (EquipmentSlot slot : getPossibleEquipmentSlots(stack)) {
            stack.addAttributeModifier(attribute, modifier, slot);
        }
    }

    /**
     * Get all possible equipment slots
     * @param stack the stack to get the slots for
     * @return a list of slots for the given stack
     */
    public static List<EquipmentSlot> getPossibleEquipmentSlots(ItemStack stack) {
        List<EquipmentSlot> equipmentSlots = new ArrayList<>();
        if (stack.getItem() instanceof ArmorItem armorItem) {
            equipmentSlots.add(armorItem.getSlotType());
        } else {
            equipmentSlots.add(EquipmentSlot.MAINHAND);
            equipmentSlots.add(EquipmentSlot.OFFHAND);
        }

        return equipmentSlots;
    }
}
