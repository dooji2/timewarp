package com.dooji.timewarp.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
    @Accessor("ARMOR_FULL_TEXTURE")
    Identifier getFullArmorTexture();

    @Accessor("ARMOR_HALF_TEXTURE")
    Identifier getHalfArmorTexture();

    @Accessor("ARMOR_EMPTY_TEXTURE")
    Identifier getEmptyArmorTexture();

    @Accessor("AIR_TEXTURE")
    Identifier getAirTexture();

    @Accessor("AIR_BURSTING_TEXTURE")
    Identifier getAirBurstingTexture();
}