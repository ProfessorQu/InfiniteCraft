package com.professorqu;

import net.fabricmc.api.ModInitializer;

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
    }
}