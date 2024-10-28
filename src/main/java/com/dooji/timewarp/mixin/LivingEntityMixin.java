package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void modifyDrops(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldAnimalBehavior")) {
                LivingEntity entity = (LivingEntity) (Object) this;

                if (entity instanceof CowEntity || entity instanceof SheepEntity || entity instanceof ChickenEntity) {
                    ci.cancel();
                    dropFilteredLoot(entity);
                }
            }
        }
    }

    @Unique
    private void dropFilteredLoot(LivingEntity entity) {
        Random random = entity.getWorld().random;

        if (entity instanceof CowEntity) {
            int leatherCount = random.nextInt(3);
            if (leatherCount > 0) {
                entity.dropStack(new ItemStack(Items.LEATHER, leatherCount));
            }
        }

        if (entity instanceof SheepEntity sheep) {
            if (!sheep.isSheared()) {
                int woolCount = 1 + random.nextInt(3);
                for (int i = 0; i < woolCount; i++) {
                    entity.dropStack(new ItemStack(Items.WHITE_WOOL));
                }
            }
        }

        if (entity instanceof ChickenEntity) {
            int featherCount = 1 + random.nextInt(3);
            entity.dropStack(new ItemStack(Items.FEATHER, featherCount));
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void shearOnHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof SheepEntity sheep && !sheep.isSheared() && entity.getWorld() instanceof ServerWorld serverWorld) {
            if (source.getAttacker() instanceof PlayerEntity player && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldAnimalBehavior")) {
                sheep.setSheared(true);

                DyeColor color = sheep.getColor();
                ItemStack woolStack = switch (color) {
                    case WHITE -> new ItemStack(Items.WHITE_WOOL);
                    case ORANGE -> new ItemStack(Items.ORANGE_WOOL);
                    case MAGENTA -> new ItemStack(Items.MAGENTA_WOOL);
                    case LIGHT_BLUE -> new ItemStack(Items.LIGHT_BLUE_WOOL);
                    case YELLOW -> new ItemStack(Items.YELLOW_WOOL);
                    case LIME -> new ItemStack(Items.LIME_WOOL);
                    case PINK -> new ItemStack(Items.PINK_WOOL);
                    case GRAY -> new ItemStack(Items.GRAY_WOOL);
                    case LIGHT_GRAY -> new ItemStack(Items.LIGHT_GRAY_WOOL);
                    case CYAN -> new ItemStack(Items.CYAN_WOOL);
                    case PURPLE -> new ItemStack(Items.PURPLE_WOOL);
                    case BLUE -> new ItemStack(Items.BLUE_WOOL);
                    case BROWN -> new ItemStack(Items.BROWN_WOOL);
                    case GREEN -> new ItemStack(Items.GREEN_WOOL);
                    case RED -> new ItemStack(Items.RED_WOOL);
                    case BLACK -> new ItemStack(Items.BLACK_WOOL);
                };

                int woolCount = 1 + serverWorld.random.nextInt(3);
                for (int i = 0; i < woolCount; i++) {
                    sheep.dropStack(woolStack.copy());
                }

                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void preventSprinting(boolean sprinting, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player && Timewarp.isRetroShiftActive(player) && !Timewarp.getRetroSetting(player, "allowSprinting")) {
            ci.cancel();
        }
    }
}