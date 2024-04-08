package com.professorqu.helpers;

import com.professorqu.InfiniteCraft;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;


public class CraftingHelper {
    private final ScreenHandler handler;
    private final World world;
    private final PlayerEntity player;
    private final RecipeInputInventory craftingInventory;
    private final CraftingResultInventory resultInventory;

    public CraftingHelper(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory) {
        this.handler = handler;
        this.world = world;
        this.player = player;
        this.craftingInventory = craftingInventory;
        this.resultInventory = resultInventory;
    }

    public boolean emptyRecipe() {
        return this.craftingInventory.isEmpty();
    }

    public boolean validVanillaRecipe() {
        return !this.resultInventory.isEmpty();
    }

    public ItemStack getCraftedItem() {
        RecipeInput input = new RecipeInput(RecipeInput.convertItemStacksToIntegers(this.craftingInventory.getHeldStacks()));
        Item item = RecipesManager.getItem(input);
        if (item == null) {
            item = generateItem();
            RecipesManager.addItem(input, item);
        }

        return new ItemStack(item);
    }

    private Item generateItem() {
        Item item;
        do {
            int randIndex = InfiniteCraft.randomInt(Registries.ITEM.size());
            item = Registries.ITEM.get(randIndex);
        } while (!item.isEnabled(world.getEnabledFeatures()));

        return item;
    }

    public void setResultStack(ItemStack itemStack) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;

        resultInventory.setStack(0, itemStack);
        handler.setPreviousTrackedSlot(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }
}
