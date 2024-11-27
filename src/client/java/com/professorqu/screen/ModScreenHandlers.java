package com.professorqu.screen;

import com.professorqu.InfiniteCraft;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ModScreenHandlers {
    public static void registerScreenHandlers() {
        InfiniteCraft.LOGGER.info("Registering Screen Handlers for: {}", InfiniteCraft.MOD_NAME);

        HandledScreens.register(InfiniteCraft.COMBINING_SCREEN_HANDLER, CombiningScreen::new);
    }
}
