package com.professorqu;

import net.fabricmc.api.ModInitializer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class InfiniteCraft implements ModInitializer {
    public static final String MOD_ID = "infinite-craft";
    public static final String MOD_NAME = "Infinite Craft";
    public static final String RECIPES_FILE = "recipes";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Random RNG = new Random();

    @Override
    public void onInitialize() {
        LOGGER.info("Loaded: {}", MOD_NAME);
    }

    /**
     * Generate a random integer
     * @param maximum   the maximum value
     * @return          an integer in the bound [0, maximum>
     */
    public static int randomInt(int maximum) {
        return RNG.nextInt(maximum);
    }
}