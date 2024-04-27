package com.professorqu.screen;

import com.professorqu.InfiniteCraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<CombiningScreenHandler> COMBINING_SCREEN_HANDLER =
            ModScreenHandlers.register(CombiningScreenHandler::new);

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER,
                new Identifier(InfiniteCraft.MOD_ID, "combining"),
                new ScreenHandlerType<T>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    public static void registerScreenHandlers() {
        InfiniteCraft.LOGGER.info("Registering Screen Handlers for: {}", InfiniteCraft.MOD_NAME);
    }
}
