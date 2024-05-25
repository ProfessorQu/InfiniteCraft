package com.professorqu.screen;

import com.professorqu.InfiniteCraft;
import com.professorqu.block.ModBlocks;
import com.professorqu.generate.ItemChanger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.util.Identifier;

public class CombiningScreenHandler extends ForgingScreenHandler {
    private static final Identifier COMBINER_SCREEN_HANDLER_ID = new Identifier(InfiniteCraft.MOD_ID, "combining");

    public CombiningScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CombiningScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(Registries.SCREEN_HANDLER.get(COMBINER_SCREEN_HANDLER_ID), syncId, inventory, context);
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return true;
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        this.input.removeStack(0, 1);
        this.input.removeStack(1, 1);
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isOf(ModBlocks.COMBINER_BLOCK);
    }

    @Override
    public void updateResult() {
        ItemStack baseStack = this.input.getStack(0);
        ItemStack additionStack = this.input.getStack(1);

        if (additionStack.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
            return;
        }

        ItemStack result = new ItemStack(baseStack.getItem());

        boolean changed1 = ItemChanger.combineEnchantments(baseStack, additionStack, result);
        boolean changed2 = ItemChanger.combineAttributes(baseStack, additionStack, result);
        boolean changed3 = ItemChanger.combinePotionEffects(baseStack, additionStack, result);

        boolean changed4 = ItemChanger.combineNames(baseStack, additionStack, result);

        if (changed1 || changed2 || changed3 || changed4)
            this.output.setStack(0, result);
    }

    @Override
    protected ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.create().input(0, 28, 38, stack -> stack.getMaxCount() == 1).input(1, 72, 38, stack -> stack.getMaxCount() == 1).output(2, 116, 38).build();
    }
}
