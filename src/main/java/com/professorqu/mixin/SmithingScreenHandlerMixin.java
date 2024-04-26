package com.professorqu.mixin;

import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin {
    @Shadow @Final private List<RecipeEntry<SmithingRecipe>> recipes;

    @Inject(method = "getForgingSlotsManager", at = @At("HEAD"), cancellable = true)
    private void getForgingSlotsManager(CallbackInfoReturnable<ForgingSlotsManager> cir) {
        cir.setReturnValue(ForgingSlotsManager.create()
                .input(0, 8, 48, stack -> this.recipes.stream().anyMatch(recipe -> recipe.value().testTemplate(stack)))
                .input(1, 26, 48, stack -> true)
                .input(2, 44, 48, stack -> true)
                .output(3, 98, 48).build());
        cir.cancel();
    }
}
