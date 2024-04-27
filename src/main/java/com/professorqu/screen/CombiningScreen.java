package com.professorqu.screen;

import com.professorqu.InfiniteCraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CombiningScreen extends ForgingScreen<CombiningScreenHandler> {
    public CombiningScreen(CombiningScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, new Identifier(InfiniteCraft.MOD_ID, "textures/gui/combiner_gui.png"));
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {

    }
}
