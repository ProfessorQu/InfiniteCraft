package com.professorqu;

import com.professorqu.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;

public class InfiniteCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModScreenHandlers.registerScreenHandlers();
	}
}