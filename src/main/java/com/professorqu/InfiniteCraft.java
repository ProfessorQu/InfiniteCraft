package com.professorqu;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class InfiniteCraft implements ModInitializer {
	public static final String MOD_ID = "infinite-craft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Random RNG = new Random();

	@Override
	public void onInitialize() {
		LOGGER.info("Loaded: " + MOD_ID);
	}
}