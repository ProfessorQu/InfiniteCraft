package com.professorqu;

import com.professorqu.block.ModBlocks;
import com.professorqu.generate.ItemGenerator;
import com.professorqu.screen.CombiningScreen;
import com.professorqu.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfiniteCraft implements ModInitializer {
    public static final String MOD_ID = "infinite-craft";
    public static final String MOD_NAME = "Infinite Craft";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> ItemGenerator.setSeed(server.getOverworld().getSeed()));

        ModBlocks.registerModBlocks();
        ModScreenHandlers.registerScreenHandlers();

        HandledScreens.register(ModScreenHandlers.COMBINING_SCREEN_HANDLER, CombiningScreen::new);

        LOGGER.info("Loaded: {}", MOD_NAME);
    }
}