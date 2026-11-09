package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HandMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void visualmod$handOn(CallbackInfo ci, @Local(argsOnly = true) MatrixStack matrices) {
        VisualConfig.inHand = true;
        if (!VisualConfig.handEnabled) return;
        matrices.translate(VisualConfig.handX, VisualConfig.handY, VisualConfig.handZ);
        matrices.scale(VisualConfig.handScale, VisualConfig.handScale, VisualConfig.handScale);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void visualmod$handOff(CallbackInfo ci) {
        VisualConfig.inHand = false;
    }
}
