package com.dooji.timewarp.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import com.dooji.timewarp.Timewarp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void preventFoodStacking(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PlayerInventory inventory = (PlayerInventory)(Object) this;
        PlayerEntity player = inventory.player;

        if (Timewarp.isRetroShiftActive(player) && !Timewarp.getRetroSetting(player, "allowStacking") && stack.get(DataComponentTypes.FOOD) != null) {
            int remaining = stack.getCount();

            for (int i = 0; i < remaining; i++) {
                ItemStack singleItem = stack.copy();
                singleItem.setCount(1);

                int emptySlot = findEmptySlotForFood(inventory, singleItem);
                if (emptySlot != -1) {
                    inventory.setStack(emptySlot, singleItem);
                } else {
                    player.dropItem(singleItem, false);
                }
            }
            stack.setCount(0);
            cir.setReturnValue(true);
        }
    }

    @Unique
    private int findEmptySlotForFood(PlayerInventory inventory, ItemStack originalStack) {
        int originalSlot = inventory.getSlotWithStack(originalStack);

        if (originalSlot != -1) {
            if (originalSlot > 0 && inventory.getStack(originalSlot - 1).isEmpty()) {
                return originalSlot - 1;
            }
            if (originalSlot < inventory.main.size() - 1 && inventory.getStack(originalSlot + 1).isEmpty()) {
                return originalSlot + 1;
            }
        }

        return inventory.getEmptySlot();
    }
}