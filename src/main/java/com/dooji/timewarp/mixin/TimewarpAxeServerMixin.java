package com.dooji.timewarp.mixin;

import com.dooji.timewarp.items.TimewarpAxe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class TimewarpAxeServerMixin {

    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    public void onServerBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player.getMainHandStack().getItem() instanceof TimewarpAxe) {
            cir.setReturnValue(false);
        }
    }
}