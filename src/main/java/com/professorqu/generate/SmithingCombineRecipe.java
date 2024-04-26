package com.professorqu.generate;

import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Stream;

public class SmithingCombineRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingCombineRecipe(ItemStack baseStack, ItemStack additionStack) {
        this.template = Ingredient.EMPTY;

        this.base = Ingredient.ofStacks(baseStack);
        this.addition = Ingredient.ofStacks(additionStack);

        this.result = new ItemStack(baseStack.getItem());

        Multimap<EntityAttribute, EntityAttributeModifier> baseAttributes = getAttributes(baseStack);
        Multimap<EntityAttribute, EntityAttributeModifier> additionAttributes = getAttributes(additionStack);

        for (Map.Entry<EntityAttribute, EntityAttributeModifier> baseAttribute : baseAttributes.entries()) {
            Optional<EntityAttributeModifier> modifier = additionAttributes.get(baseAttribute.getKey()).stream().findFirst();

            if (modifier.isPresent()) {
                Generator.addAttribute(
                        this.result,
                        baseAttribute.getKey(),
                        new EntityAttributeModifier(
                                baseAttribute.getValue().toString(),
                                baseAttribute.getKey().clamp(baseAttribute.getValue().getValue() + modifier.get().getValue()),
                                EntityAttributeModifier.Operation.ADDITION
                        )
                );
            } else {
                Generator.addAttribute(this.result, baseAttribute.getKey(), baseAttribute.getValue());
            }
        }

        for (Map.Entry<EntityAttribute, EntityAttributeModifier> additionAttribute : additionAttributes.entries()) {
            if (!baseAttributes.containsKey(additionAttribute.getKey())) {
                Generator.addAttribute(this.result, additionAttribute.getKey(), additionAttribute.getValue());
            }
        }
    }

    private static Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack stack) {
        return stack.getAttributeModifiers(Generator.getPossibleEquipmentSlots(stack).get(0));
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.template.test(inventory.getStack(0)) && this.base.test(inventory.getStack(1)) && this.addition.test(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return this.result.copy();
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return this.result;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isEmpty() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }
}
