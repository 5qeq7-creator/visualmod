package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// LayerRenderState#setGlint(Glint) найден в Yarn 1.21.11
@Mixin(targets = "net.minecraft.client.render.item.ItemRenderState$LayerRenderState")
public class GlintMixin {
    @ModifyVariable(method = "setGlint", at = @At("HEAD"), argsOnly = true)
    private ItemRenderState.Glint visualmod$forceGlint(ItemRenderState.Glint g) {
        return (VisualConfig.handGlow && VisualConfig.inHand) ? ItemRenderState.Glint.STANDARD : g;
    }
}
