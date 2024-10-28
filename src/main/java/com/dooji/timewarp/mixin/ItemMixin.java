package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void instantConsumeOnRightClick(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack heldItem = player.getStackInHand(hand);

        if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI") && heldItem.get(DataComponentTypes.FOOD) != null) {
            FoodComponent foodComponent = heldItem.get(DataComponentTypes.FOOD);

            if (foodComponent != null) {
                player.heal(foodComponent.nutrition());
                heldItem.decrement(1);
                player.getItemCooldownManager().set((Item) (Object) this, 10);

                cir.setReturnValue(TypedActionResult.success(player.getStackInHand(hand), world.isClient()));
            }
        }
    }
}