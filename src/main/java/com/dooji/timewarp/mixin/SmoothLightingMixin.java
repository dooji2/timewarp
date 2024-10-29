package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class SmoothLightingMixin {

    private Boolean previousSmoothLighting;

    @Inject(method = "tick", at = @At("HEAD"))
    private void manageSmoothLighting(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;

        if (client.player != null && Timewarp.isRetroShiftActive(client.player) && Timewarp.getRetroSetting(client.player, "noSmoothLighting")) {
            if (previousSmoothLighting == null) {
                SimpleOption<Boolean> aoOption = ((GameOptionsAccessor) options).getAo();
                previousSmoothLighting = aoOption.getValue();
                aoOption.setValue(false);
            }
        } else if (previousSmoothLighting != null) {
            ((GameOptionsAccessor) options).getAo().setValue(previousSmoothLighting);
            previousSmoothLighting = null;
        }
    }
}