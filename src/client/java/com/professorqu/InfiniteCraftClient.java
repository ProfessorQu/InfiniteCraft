package com.professorqu;

import com.professorqu.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class InfiniteCraftClient implements ClientModInitializer {
	public static final int GRID_COLUMNS = 6, GRID_ROWS = 8,
							MAX_SLOTS = GRID_COLUMNS * GRID_ROWS;

	public static long seedHash;

	@Override
	public void onInitializeClient() {
		ModScreenHandlers.registerScreenHandlers();

		ClientLifecycleEvents.CLIENT_STARTED.register(Recipes::initialize);
		ClientPlayConnectionEvents.DISCONNECT.register((clientPlayNetworkHandler, client1) -> Recipes.save());
		ClientPlayNetworking.registerGlobalReceiver(InfiniteCraft.SEEDHASH_PACKET_ID, ((client, handler, buf, responseSender) -> {
			seedHash = buf.readLong();
			Recipes.load();
		}));
	}
}