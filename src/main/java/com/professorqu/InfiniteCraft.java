package com.professorqu;

import com.professorqu.block.ModBlocks;
import com.professorqu.generate.ItemGenerator;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfiniteCraft implements ModInitializer {
	public static final String MOD_ID = "infinite-craft";
	public static final String MOD_NAME = "Infinite Craft";

	public static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier SEEDHASH_PACKET_ID = new Identifier(MOD_ID, "seed_hash");

	@Override
	public void onInitialize() {
		ItemGenerator.loadNames();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> ItemGenerator.setSeed(server.getOverworld().getSeed()));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeLong(Long.hashCode(server.getOverworld().getSeed()));
			ServerPlayNetworking.send(handler.player, InfiniteCraft.SEEDHASH_PACKET_ID, buf);
		});

		ModBlocks.registerModBlocks();

		LOGGER.info("Done loading: {}", MOD_NAME);
	}
}