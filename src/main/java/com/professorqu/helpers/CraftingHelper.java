package com.professorqu.helpers;

import com.professorqu.generate.Generator;
import com.professorqu.saving.RecipeResult;
import com.professorqu.saving.RecipeInput;
import com.professorqu.saving.RecipesState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
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

    public boolean validVanillaRecipe() {
        return !this.resultInventory.isEmpty();
    }

    /**
     * Get the item that is crafted from the crafting screen
     * @return the item that is crafted from the crafting screen
     */
    public @NotNull ItemStack getCraftedItem() {
        MinecraftServer server = world.getServer();
        if (server == null) throw new RuntimeException("Server wasn't available");

        RecipeInput input = RecipeInput.fromItemStacks(this.craftingInventory.getHeldStacks());
        RecipesState state = RecipesState.getServerState(server);

        RecipeResult result = state.tryGetResult(input);
        if (result == null) {
            result = this.generateItem(input);
            state.addItem(input, result);
        }

        return new ItemStack(Registries.ITEM.get(result.getItemId()), result.getCount());
    }

    private @NotNull RecipeResult generateItem(RecipeInput input) {
        return Generator.generate(ArrayUtils.toPrimitive(input.input().toArray(new Integer[0])));
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
