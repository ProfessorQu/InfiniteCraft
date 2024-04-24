package com.professorqu;

import com.professorqu.generate.Generator;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfiniteCraft implements ModInitializer {
    public static final String MOD_ID = "infinite-craft";
    public static final String MOD_NAME = "Infinite Craft";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Generator.setSeed(server.getOverworld().getSeed()));

        LOGGER.info("Loaded: {}", MOD_NAME);
    }
}