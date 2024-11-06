package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import com.dooji.timewarp.items.TimewarpAxe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class TimewarpAxeInteractionMixin {
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void onLeftClick(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = client.player;
        if (player == null) return;

        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.getItem() instanceof TimewarpAxe) {
            BlockPos[] selections = TimewarpAxe.getSelection(player);
            
            if (selections[0] == null) {
                selections[0] = pos;
                player.sendMessage(Text.translatable("message.timewarp.first_corner_set", pos.getX(), pos.getY(), pos.getZ()), true);
            } else {
                selections[1] = pos;
                player.sendMessage(Text.translatable("message.timewarp.second_corner_set", pos.getX(), pos.getY(), pos.getZ()), true);
            }

            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void onRightClick(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player == null || hand != Hand.MAIN_HAND) return;

        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.getItem() instanceof TimewarpAxe) {
            BlockPos pos = hitResult.getBlockPos();
            BlockPos[] selections = TimewarpAxe.getSelection(player);

            if (selections[0] == null) {
                selections[0] = pos;
                player.sendMessage(Text.translatable("message.timewarp.first_corner_set", pos.getX(), pos.getY(), pos.getZ()), true);
            } else {
                selections[1] = pos;
                player.sendMessage(Text.translatable("message.timewarp.second_corner_set", pos.getX(), pos.getY(), pos.getZ()), true);
            }

            if (selections[0] != null && selections[1] != null) {
                if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
                    Timewarp.getInstance().storeCornerSelection(player.getUuid(), selections[0], selections[1]);
                } else {
                    Timewarp.getInstance().handleCornerSelection(player, selections[0], selections[1]);
                }
            }

            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}