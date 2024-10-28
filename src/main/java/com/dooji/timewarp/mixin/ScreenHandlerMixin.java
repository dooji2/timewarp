package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (Timewarp.isRetroShiftActive(player) && !Timewarp.getRetroSetting(player, "allowStacking")) {
            if (slotIndex == -999 || slotIndex == -1) {
                return;
            }

            Slot slot = ((ScreenHandler) (Object) this).getSlot(slotIndex);
            if (slot != null && slot.hasStack()) {
                ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
                ItemStack clickedStack = slot.getStack();

                boolean cursorIsFood = cursorStack.get(DataComponentTypes.FOOD) != null;
                boolean clickedIsFood = clickedStack.get(DataComponentTypes.FOOD) != null;

                if (cursorIsFood && clickedIsFood && cursorStack.isOf(clickedStack.getItem())) {
                    ci.cancel();
                }
            }
        }
    }
}