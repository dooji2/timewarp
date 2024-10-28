package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import com.dooji.timewarp.mixin.InGameHudAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private static boolean isRenderingCustom = false;

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    public void moveHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI") && !isRenderingCustom) {
            ci.cancel();

            isRenderingCustom = true;

            context.getMatrices().translate(0, 4, 0);

            ((InGameHudAccessor) (Object) this).invokeRenderStatusBars(context);
            renderCustomArmor(context, player, x, y);

            isRenderingCustom = false;
        }
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void moveArmorBar(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    private void renderCustomArmor(DrawContext context, PlayerEntity player, int x, int y) {
        int armorLevel = player.getArmor();
        if (armorLevel > 0) {
            RenderSystem.enableBlend();

            int adjustedY = y;

            int gap = 10;
            int adjustedX = x + 90 + gap;

            InGameHudAccessor hud = (InGameHudAccessor) MinecraftClient.getInstance().inGameHud;

            for (int n = 0; n < 10; ++n) {
                int armorX = adjustedX + n * 8;
                if (n * 2 + 1 < armorLevel) {
                    context.drawGuiTexture(hud.getFullArmorTexture(), armorX, adjustedY, 9, 9);
                } else if (n * 2 + 1 == armorLevel) {
                    context.drawGuiTexture(hud.getHalfArmorTexture(), armorX, adjustedY, 9, 9);
                } else {
                    context.drawGuiTexture(hud.getEmptyArmorTexture(), armorX, adjustedY, 9, 9);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"), cancellable = true)
    private void adjustAirBubblePosition(DrawContext context, CallbackInfo ci) {
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (playerEntity == null) return;

        int maxAir = playerEntity.getMaxAir();
        int air = Math.min(playerEntity.getAir(), maxAir);

        if ((playerEntity.isSubmergedIn(FluidTags.WATER) || air < maxAir) && Timewarp.isRetroShiftActive(playerEntity) && Timewarp.getRetroSetting(playerEntity, "oldGUI")) {
            ci.cancel();
            renderCustomAirBubbles(context, playerEntity, maxAir, air);
        }
    }

    private void renderCustomAirBubbles(DrawContext context, PlayerEntity player, int maxAir, int air) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int airBubbleX = screenWidth / 2 + 91;
        int airBubbleY = player.getArmor() > 0 ? screenHeight - 49 : screenHeight - 39;

        int fullBubbles = MathHelper.ceil((double) (air - 2) * 10.0 / (double) maxAir);
        int totalBubbles = MathHelper.ceil((double) air * 10.0 / (double) maxAir) - fullBubbles;

        InGameHudAccessor hud = (InGameHudAccessor) MinecraftClient.getInstance().inGameHud;

        RenderSystem.enableBlend();

        for (int i = 0; i < fullBubbles + totalBubbles; ++i) {
            int bubbleX = airBubbleX - i * 8 - 9;

            if (i < fullBubbles) {
                context.drawGuiTexture(hud.getAirTexture(), bubbleX, airBubbleY, 9, 9);
            } else {
                context.drawGuiTexture(hud.getAirBurstingTexture(), bubbleX, airBubbleY, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void hideHungerBar(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if (Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    public void hideMountHealth(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"), cancellable = true)
    public void hideJumpBar(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    public void hideXpLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void hideXpBar(DrawContext context, int x, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "oldGUI")) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void renderCustomText(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player != null && Timewarp.isRetroShiftActive(player) && Timewarp.getRetroSetting(player, "versionText")) {
            TextRenderer textRenderer = client.textRenderer;

            String versionNumber = "1.21";
            context.drawTextWithShadow(textRenderer, Text.translatable("message.timewarp.version_text", versionNumber), 2, 2, 0xFFFFFF);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null && Timewarp.isRetroShiftActive(player) && !Timewarp.getInstance().isPlayerInArea(player)) {
            int remainingTime = Timewarp.getShiftRemainingTime(player) / 20;
            Text remainingText = Text.translatable("message.timewarp.time_left", remainingTime);

            TextRenderer textRenderer = client.textRenderer;
            int screenWidth = client.getWindow().getScaledWidth();
            int textWidth = textRenderer.getWidth(remainingText);
            int x = (screenWidth / 2) - (textWidth / 2);
            int y = 20;

            context.drawTextWithShadow(textRenderer, Text.of(remainingText), x, y, 0xFFFFFF);

            Item objectiveItem = Timewarp.getInstance().getObjectiveItem(player);
            int objectiveAmount = Timewarp.getInstance().getObjectiveAmount(player);

            if (objectiveItem != null) {
                y += 12;

                ItemStack itemStack = new ItemStack(objectiveItem);

                int iconWidth = 16;
                String amountText = " x" + objectiveAmount;
                int amountTextWidth = textRenderer.getWidth(amountText);
                int totalWidth = iconWidth + amountTextWidth + 2;

                int centeredX = (screenWidth / 2) - (totalWidth / 2);

                context.drawItemWithoutEntity(itemStack, centeredX, y);
                context.drawTextWithShadow(textRenderer, amountText, centeredX + iconWidth + 2, y + 4, 0xFFFFFF);

                y += 20;
                Text objectiveText = Text.translatable("message.timewarp.objective_text");
                textWidth = textRenderer.getWidth(objectiveText);
                x = (screenWidth / 2) - (textWidth / 2);
                context.drawTextWithShadow(textRenderer, Text.of(objectiveText), x, y, 0xAAAAAA);
            }
        }
    }
}