package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    public void allowEatingWithFullHunger(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void preventSwimming(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noSwimming")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void disableSwimmingAnimation(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noSwimming")) {
            ci.cancel();
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void preventTradingWithVillagers(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noTrading")) {
            if (entity instanceof VillagerEntity) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void disableSprintingAndAttackCooldownInTimewarp(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        EntityAttributeInstance attackSpeed = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);

        if (Timewarp.getInstance().isRetroShiftActive(player) && !Timewarp.getRetroSetting(player, "allowSprinting")) {
            if (player.isSprinting()) {
                player.setSprinting(false);
            }
        }

        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldCombat") && attackSpeed != null) {
            attackSpeed.setBaseValue(1024);
        } else if (attackSpeed != null) {
            attackSpeed.setBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED.value().getDefaultValue());
        }
    }

    @Inject(method = "adjustMovementForSneaking", at = @At("HEAD"), cancellable = true)
    private void disableSneakingMovementAdjustment(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Timewarp.getInstance().isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "noSneaking")) {
            cir.setReturnValue(movement);
        }
    }
}