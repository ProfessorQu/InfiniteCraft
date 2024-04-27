package com.professorqu.block;

import com.professorqu.InfiniteCraft;
import com.professorqu.block.custom.CombinerBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    private static final Identifier COMBINER_ID = new Identifier(InfiniteCraft.MOD_ID, "combiner");
    public static final Block COMBINER_BLOCK = registerBlock(
            new CombinerBlock(FabricBlockSettings.copyOf(Blocks.SMITHING_TABLE)));

    private static Block registerBlock(Block block) {
        registerBlockItem(block);
        return Registry.register(Registries.BLOCK, COMBINER_ID, block);
    }

    private static void registerBlockItem(Block block) {
        Registry.register(Registries.ITEM, COMBINER_ID,
                new BlockItem(block, new FabricItemSettings()));
    }

    private static void addEntries(FabricItemGroupEntries entries) {
        entries.add(new ItemStack(COMBINER_BLOCK));
    }

    public static void registerModBlocks() {
        InfiniteCraft.LOGGER.info("Registering ModBlocks for: {}", InfiniteCraft.MOD_NAME);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ModBlocks::addEntries);

        Registry.register(Registries.BLOCK_TYPE, COMBINER_ID, CombinerBlock.CODEC);
    }
}
