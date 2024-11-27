package com.professorqu;

import com.professorqu.block.ModBlocks;
import com.professorqu.generate.ItemGenerator;
import com.professorqu.screen.CombiningScreenHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfiniteCraft implements ModInitializer {
	public static final String MOD_ID = "infinite-craft";
	public static final String MOD_NAME = "Infinite Craft";

	public static final Identifier RECIPE_ID = new Identifier(InfiniteCraft.MOD_ID, "recipe");

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier SEEDHASH_PACKET_ID = new Identifier(MOD_ID, "seed_hash");

	public static final ScreenHandlerType<CombiningScreenHandler> COMBINING_SCREEN_HANDLER =
			InfiniteCraft.register(CombiningScreenHandler::new);

	private static <T extends ScreenHandler> ScreenHandlerType<T> register(ScreenHandlerType.Factory<T> factory) {
		return Registry.register(Registries.SCREEN_HANDLER,
				new Identifier(InfiniteCraft.MOD_ID, "combining"),
				new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
	}

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

	public static boolean canHavePotionEffects(Item item) {
		return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.TIPPED_ARROW;
	}
}