package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "setPose", at = @At("HEAD"), cancellable = true)
    private void preventCrouchingPose(EntityPose pose, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof PlayerEntity player) {
            if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noSneaking") && pose == EntityPose.CROUCHING) {
                ci.cancel();
            }
        }
    }
}