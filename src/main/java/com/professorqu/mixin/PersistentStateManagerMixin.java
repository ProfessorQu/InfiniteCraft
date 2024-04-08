package com.professorqu.mixin;

import com.professorqu.InfiniteCraft;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.function.Function;

@Mixin(PersistentStateManager.class)
public abstract class PersistentStateManagerMixin {
    @Inject(method = "getFile", at = @At("HEAD"))
    private void getFile(String id, CallbackInfoReturnable<File> cir) {
        InfiniteCraft.LOGGER.info("Getting file with id: {}", id);
    }

    @Inject(method = "readFromFile", at = @At("HEAD"))
    private <T extends PersistentState> void readFromFile(Function<NbtCompound, T> readFunction, DataFixTypes dataFixTypes, String id, CallbackInfoReturnable<@Nullable T> cir) {
        InfiniteCraft.LOGGER.info("Read from file with id: {}", id);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    private void readNbt(String id, DataFixTypes dataFixTypes, int currentSaveVersion, CallbackInfoReturnable<NbtCompound> cir) {
        InfiniteCraft.LOGGER.info("Getting nbt with id: {}", id);
    }
}
