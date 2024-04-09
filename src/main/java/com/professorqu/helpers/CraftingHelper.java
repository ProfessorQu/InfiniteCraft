package com.professorqu.helpers;

import com.professorqu.InfiniteCraft;
import com.professorqu.saving.RecipeInput;
import com.professorqu.saving.RecipesState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


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

    /**
     * Check if the recipe is a valid vanilla recipe
     * @return  return true if the recipe is a valid vanilla recipe
     */
    public boolean validVanillaRecipe() {
        return !this.resultInventory.isEmpty();
    }

    /**
     * Get the item that is crafted from the crafting screen
     * @return  the item that is crafted from the crafting screen
     */
    public @NotNull ItemStack getCraftedItem() {
        MinecraftServer server = world.getServer();
        if (server == null) throw new RuntimeException("Server wasn't available");

        RecipeInput input = new RecipeInput(RecipeInput.convertToItemIds(this.craftingInventory.getHeldStacks()));
        RecipesState state = RecipesState.getServerState(server);

        Item item = state.tryGetItem(input);
        if (item == null) {
            item = this.generateEnabledItem();
            state.addItem(input, Item.getRawId(item));
        }

        return new ItemStack(item);
    }

    private @NotNull Item generateEnabledItem() {
        Item item;
        do {
            item = this.generateItem();
        } while (!item.isEnabled(world.getEnabledFeatures()));

        return item;
    }

    private @NotNull Item generateItem() {
        int randIndex = InfiniteCraft.randomInt(Registries.ITEM.size());
        return Registries.ITEM.get(randIndex);
    }

    /**
     * Set the result stack of the crafting screen
     * @param itemStack the item stack to set as the result
     */
    public void setResultStack(@NotNull ItemStack itemStack) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;

        resultInventory.setStack(0, itemStack);
        handler.setPreviousTrackedSlot(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }
}
