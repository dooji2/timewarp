package com.dooji.timewarp.mixin;

import net.minecraft.entity.player.PlayerEntity;
import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void preventHungerLoss(PlayerEntity player, CallbackInfo ci) {
        if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0F);
            ci.cancel();
        }
    }
}