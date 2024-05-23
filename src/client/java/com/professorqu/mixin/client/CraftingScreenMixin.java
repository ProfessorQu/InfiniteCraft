package com.professorqu.mixin.client;

import com.professorqu.InfiniteCraft;
import com.professorqu.Recipes;
import com.professorqu.screen.slot.RecipeResultSlot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
    @Shadow @Final private RecipeBookWidget recipeBook;
    @Unique
    private static final Identifier SAVED_RECIPES_TEXTURE = new Identifier(InfiniteCraft.MOD_ID, "textures/gui/saved_recipes.png");
    @Unique
    private static final int SAVED_RECIPES_TEXTURE_WIDTH = 122;
    @Unique
    private static final int SAVED_RECIPES_TEXTURE_HEIGHT = 158;

    @Unique
    private static ButtonWidget addButton;

    @Unique
    private static final Identifier RECIPE_TEXTURE = new Identifier(InfiniteCraft.MOD_ID, "textures/gui/recipe.png");
    @Unique
    private static final int RECIPE_TEXTURE_SIZE = 68;

    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initInject(CallbackInfo ci) {
        addButton = this.addDrawableChild(ButtonWidget.builder(
                Text.of("+"),
                button -> {
                    Recipes.saveLastRecipe();
                    Recipes.refresh(handler);
                }
        ).dimensions(this.getRightEdge() - 24, this.getTopEdge() + 34, 16, 16).build());
    }

    @Inject(method = "onMouseClick", at = @At("TAIL"))
    private void onMouseClickInject(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (!(slot instanceof RecipeResultSlot recipeResultSlot)) {
            return;
        }

        Optional<RecipeEntry<?>> optionalRecipeEntry = recipeResultSlot.getOptionalRecipeEntry();
        if (optionalRecipeEntry.isEmpty()) return;

        RecipeEntry<?> recipeEntry = optionalRecipeEntry.get();

        switch (button) {
            case 0 -> Recipes.setCurrentRecipe(recipeEntry);
            case 1 -> Recipes.removeRecipe(recipeEntry);
        }

        Recipes.refresh(handler);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (addButton == null)
            return;

        addButton.setPosition(this.getRightEdge() - 24, this.getTopEdge() + 34);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        context.drawTexture(
                SAVED_RECIPES_TEXTURE,
                this.getRightEdge() + 2, this.getTopEdge(),
                0, 0,
                SAVED_RECIPES_TEXTURE_WIDTH,
                SAVED_RECIPES_TEXTURE_HEIGHT,
                SAVED_RECIPES_TEXTURE_WIDTH,
                SAVED_RECIPES_TEXTURE_HEIGHT
        );
        context.drawTexture(
                RECIPE_TEXTURE,
                this.getLeftEdge() - RECIPE_TEXTURE_SIZE - 2, this.getTopEdge(),
                0, 0,
                RECIPE_TEXTURE_SIZE,
                RECIPE_TEXTURE_SIZE,
                RECIPE_TEXTURE_SIZE,
                RECIPE_TEXTURE_SIZE
        );
    }

    @Unique
    private int getLeftEdge() {
//        return (this.width - this.backgroundWidth) / 2;
        return recipeBook.findLeftEdge(this.width, this.backgroundWidth);
    }

    @Unique
    private int getRightEdge() {
        return this.getLeftEdge() + this.backgroundWidth;
    }

    @Unique
    private int getTopEdge() {
        return (this.height - this.backgroundHeight) / 2;
    }
}
