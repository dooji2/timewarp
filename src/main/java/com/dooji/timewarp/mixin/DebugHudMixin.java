package com.dooji.timewarp.mixin;

import com.dooji.timewarp.Timewarp;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

@Mixin(InGameHud.class)
public class DebugHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void renderDebugInfo(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || !Timewarp.getInstance().DEBUG_MODE) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int padding = 5;
        int yOffset = padding;

        PlayerEntity player = client.player;

        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("timewarp");
        String modVersion = modContainer.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");

        Map.Entry<String, Integer>[] debugLines = new Map.Entry[]{
                new AbstractMap.SimpleEntry<>("timewarp v" + modVersion, 0xFFFFFF),
                new AbstractMap.SimpleEntry<>("minecraft " + SharedConstants.getGameVersion().getName(), 0xFFFFFF),
                new AbstractMap.SimpleEntry<>("fabric " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion(), 0xFFFFFF),
                new AbstractMap.SimpleEntry<>("isTimewarpActive: " + Timewarp.getInstance().isRetroShiftActive(player), Timewarp.getInstance().isRetroShiftActive(player) ? Formatting.GREEN.getColorValue() : Formatting.RED.getColorValue()),
                new AbstractMap.SimpleEntry<>("hasObjective: " + Timewarp.getInstance().objectives.containsKey(player), Timewarp.getInstance().objectives.containsKey(player) ? Formatting.GREEN.getColorValue() : Formatting.RED.getColorValue()),
                new AbstractMap.SimpleEntry<>("timeUntilSave: " + (Timewarp.getInstance().SAVE_INTERVAL - (client.world.getTimeOfDay() % Timewarp.getInstance().SAVE_INTERVAL)) / 20 + "s", 0xFFFFFF),
                new AbstractMap.SimpleEntry<>("timeUntilShift: " + Timewarp.getInstance().playerShiftTimers.getOrDefault(player, 0) / 20 + "s", 0xFFFFFF),
                new AbstractMap.SimpleEntry<>("remainingShiftTime: " + Timewarp.getInstance().getShiftRemainingTime(player) / 20 + "s", 0xFFFFFF)
        };

        for (Map.Entry<String, Integer> entry : debugLines) {
            String line = entry.getKey();
            int color = entry.getValue();
            int textWidth = client.textRenderer.getWidth(line);
            int xOffset = screenWidth - textWidth - padding;
            context.drawTextWithShadow(client.textRenderer, line, xOffset, yOffset, color);
            yOffset += 10;
        }

        for (Map.Entry<String, Boolean> featureEntry : Timewarp.getInstance().retroTimeShiftSettings.getOrDefault(player, Map.of()).entrySet()) {
            String featureLine = featureEntry.getKey() + ": " + featureEntry.getValue();
            int textWidth = client.textRenderer.getWidth(featureLine);
            int xOffset = screenWidth - textWidth - padding;
            int color = featureEntry.getValue() ? Formatting.GREEN.getColorValue() : Formatting.RED.getColorValue();
            context.drawTextWithShadow(client.textRenderer, featureLine, xOffset, yOffset, color);
            yOffset += 10;
        }
    }
}