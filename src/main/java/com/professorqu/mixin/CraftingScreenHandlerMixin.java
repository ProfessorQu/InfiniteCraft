package com.professorqu.mixin;

import com.professorqu.helpers.CraftingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
	@Inject(method = "updateResult", at = @At("TAIL"))
	private static void updateResult(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
		CraftingHelper helper = new CraftingHelper(handler, world, player, craftingInventory, resultInventory);

		if (helper.emptyRecipe()) return;
		if (helper.validVanillaRecipe()) return;

		ItemStack itemStack = helper.getCraftedItem();
		helper.setResultStack(itemStack);
	}
}