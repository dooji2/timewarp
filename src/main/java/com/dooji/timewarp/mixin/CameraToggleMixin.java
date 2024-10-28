package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class CameraToggleMixin {

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setPerspective(Lnet/minecraft/client/option/Perspective;)V"), cancellable = true)
    private void limitCameraToggle(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        PlayerEntity player = client.player;

        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noFrontView")) {
            Perspective currentPerspective = client.options.getPerspective();

            if (currentPerspective == Perspective.FIRST_PERSON) {
                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            } else if (currentPerspective == Perspective.THIRD_PERSON_BACK) {
                client.options.setPerspective(Perspective.FIRST_PERSON);
            }

            if (currentPerspective.isFirstPerson() != client.options.getPerspective().isFirstPerson()) {
                client.gameRenderer.onCameraEntitySet(
                        client.options.getPerspective().isFirstPerson() ? client.getCameraEntity() : null
                );
            }

            client.worldRenderer.scheduleTerrainUpdate();
            ci.cancel();
        }
    }
}