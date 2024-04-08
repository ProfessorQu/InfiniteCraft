package com.professorqu;

import com.professorqu.helpers.CraftingHelper;
import com.professorqu.helpers.RecipesManager;
import com.professorqu.helpers.RecipesState;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.world.PersistentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class InfiniteCraft implements ModInitializer {
    public static final String MOD_ID = "infinite-craft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Random RNG = new Random();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(RecipesManager::loadRecipes);
        ServerLifecycleEvents.SERVER_STOPPED.register(RecipesManager::saveRecipes);

        LOGGER.info("Loaded: " + MOD_ID);
    }

    public static int randomInt(int bound) {
        return RNG.nextInt(bound);
    }
}