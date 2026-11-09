package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Режим Bright: подменяем значение яркости (gamma) выше обычного максимума.
@Mixin(SimpleOption.class)
public class GammaMixin {
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void visualmod$gamma(CallbackInfoReturnable<Object> cir) {
        if (!VisualConfig.brightEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;
        if ((Object) this == mc.options.getGamma()) {
            cir.setReturnValue(Double.valueOf(VisualConfig.brightness));
        }
    }
}
