package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class MinecartEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void onMinecartTick(CallbackInfo ci) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;

        if (minecart.hasPassengers() && minecart.getFirstPassenger() instanceof PlayerEntity player) {

            if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldMinecart")) {
                Vec3d velocity = minecart.getVelocity();

                if (velocity.x != 0 || velocity.z != 0) {
                    float targetYaw = (float) (Math.atan2(velocity.z, velocity.x) * 180 / Math.PI) - 90;

                    float currentYaw = player.getYaw();
                    float smoothYaw = MathHelper.lerp(0.2f, currentYaw, targetYaw);

                    player.setYaw(smoothYaw);
                }
            }
        }
    }
}