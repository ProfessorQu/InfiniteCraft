package com.professorqu.generate;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemChanger {
    /**
     * Combine enchantments between input1 and input2
     * @param input1 the first input
     * @param input2 the second input
     * @param result the result to add all the enchantments to
     */
    public static boolean combineEnchantments(ItemStack input1, ItemStack input2, ItemStack result) {
        boolean changed = false;

        Map<Enchantment, Integer> baseEnchantments = EnchantmentHelper.fromNbt(input1.getEnchantments());
        Map<Enchantment, Integer> additionEnchantments = EnchantmentHelper.fromNbt(input2.getEnchantments());

        for (Map.Entry<Enchantment, Integer> baseEnchantment : baseEnchantments.entrySet()) {
            Integer level = additionEnchantments.get(baseEnchantment.getKey());
            if (level != null) {
                result.addEnchantment(baseEnchantment.getKey(), Math.min(baseEnchantment.getValue() + level, 255));

                changed = true;
            } else {
                result.addEnchantment(baseEnchantment.getKey(), baseEnchantment.getValue());
            }
        }

        for (Map.Entry<Enchantment, Integer> additionEnchantment : additionEnchantments.entrySet()) {
            if (!baseEnchantments.containsKey(additionEnchantment.getKey())) {
                result.addEnchantment(additionEnchantment.getKey(), additionEnchantment.getValue());

                changed = true;
            }
        }

        return changed;
    }

    public static void addAttributes(ItemStack result, ItemStack attributesStack) {
        ItemChanger.combineAttributes(result, attributesStack, result);
    }

    /**
     * Combine enchantments between input1 and input2
     * @param input1 the first input
     * @param input2 the second input
     * @param result the result to add all the enchantments to
     */
    public static boolean combineAttributes(ItemStack input1, ItemStack input2, ItemStack result) {
        boolean changed = false;

        Multimap<EntityAttribute, EntityAttributeModifier> baseAttributes = getAttributes(input1);
        Multimap<EntityAttribute, EntityAttributeModifier> additionAttributes = getAttributes(input2);

        for (Map.Entry<EntityAttribute, EntityAttributeModifier> baseAttribute : baseAttributes.entries()) {
            Optional<EntityAttributeModifier> modifier = additionAttributes.get(baseAttribute.getKey()).stream().findFirst();

            if (modifier.isPresent()) {
                ItemChanger.addAttribute(
                        result,
                        baseAttribute.getKey(),
                        new EntityAttributeModifier(
                                baseAttribute.getValue().toString(),
                                baseAttribute.getKey().clamp(baseAttribute.getValue().getValue() + modifier.get().getValue()),
                                EntityAttributeModifier.Operation.ADDITION
                        )
                );

                changed = true;
            } else {
                ItemChanger.addAttribute(result, baseAttribute.getKey(), baseAttribute.getValue());
            }
        }

        for (Map.Entry<EntityAttribute, EntityAttributeModifier> additionAttribute : additionAttributes.entries()) {
            if (!baseAttributes.containsKey(additionAttribute.getKey())) {
                ItemChanger.addAttribute(result, additionAttribute.getKey(), additionAttribute.getValue());

                changed = true;
            }
        }

        return changed;
    }

    /**
     * Get all attributes from a stack
     * @param stack the stack to get the attributes from
     * @return the attributes
     */
    private static Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack stack) {
        return stack.getAttributeModifiers(ItemChanger.getPossibleEquipmentSlots(stack).get(0));
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
