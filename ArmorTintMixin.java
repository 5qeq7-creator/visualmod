package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// EquipmentRenderer.getDyeColor(Layer, int) и render(...) найдены в Yarn 1.21.11.
// Оттенок брони: тёмно-зелёный (целая) -> тёмно-красный (почти сломана), для своей и чужой брони.
@Mixin(EquipmentRenderer.class)
public class ArmorTintMixin {
    private static ItemStack visualmod$current;

    @Inject(method = "render", at = @At("HEAD"))
    private void visualmod$begin(CallbackInfo ci, @Local(argsOnly = true) ItemStack stack) {
        visualmod$current = stack;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void visualmod$end(CallbackInfo ci) {
        visualmod$current = null;
    }

    @Inject(method = "getDyeColor", at = @At("RETURN"), cancellable = true)
    private static void visualmod$tint(CallbackInfoReturnable<Integer> cir) {
        ItemStack s = visualmod$current;
        if (!VisualConfig.armorTint || s == null || !s.isDamageable()) return;

        float f = 1f - (float) s.getDamage() / Math.max(1, s.getMaxDamage()); // 1 = целая, 0 = сломана
        f = Math.max(0f, Math.min(1f, f));
        float hue = (f * 120f) / 360f; // 0 = красный ... 120 = зелёный
        int c = java.awt.Color.HSBtoRGB(hue, 1f, 0.55f); // тёмный оттенок

        float k = Math.max(0f, Math.min(1f, VisualConfig.armorTintStrength));
        int r = (int) ((1f - k) * 255 + k * (c >> 16 & 255));
        int g = (int) ((1f - k) * 255 + k * (c >> 8 & 255));
        int b = (int) ((1f - k) * 255 + k * (c & 255));
        cir.setReturnValue(0xFF000000 | r << 16 | g << 8 | b);
    }
}
