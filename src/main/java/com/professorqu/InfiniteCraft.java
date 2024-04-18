package com.professorqu;

import com.professorqu.helpers.CraftingHelper;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfiniteCraft implements ModInitializer {
    public static final String MOD_ID = "infinite-craft";
    public static final String MOD_NAME = "Infinite Craft";
    public static final String RECIPES_FILE = "recipes";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Loaded: {}", MOD_NAME);

        ServerLifecycleEvents.SERVER_STARTED.register(CraftingHelper::createGenerator);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CraftingHelper.destroyGenerator());
    }
}