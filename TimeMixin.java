package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// ClientWorld.Properties#getTimeOfDay() найден в Yarn 1.21.11.
// Меняет время суток только у тебя на экране, сервер об этом не знает.
@Mixin(ClientWorld.Properties.class)
public class TimeMixin {
    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void visualmod$time(CallbackInfoReturnable<Long> cir) {
        if (!VisualConfig.worldTime) return;
        long t;
        switch (VisualConfig.worldTimeMode) {
            case 1: t = 23000L; break;  // утро (рассвет)
            case 2: t = 12000L; break;  // вечер (закат)
            case 3: t = 18000L; break;  // ночь
            case 4: t = Math.max(0, Math.min(24000, VisualConfig.worldTimeCustom)); break;
            default: t = 6000L;         // день
        }
        cir.setReturnValue(t);
    }
}
