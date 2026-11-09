package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Включает "осадки" на твоём экране, даже если на сервере ясно (только клиентский мир).
@Mixin(World.class)
public class RainGradientMixin {
    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
    private void visualmod$rain(float delta, CallbackInfoReturnable<Float> cir) {
        if (VisualConfig.shaderMode == 0) return;
        World self = (World) (Object) this;
        if (!self.isClient()) return;
        cir.setReturnValue(Math.max(cir.getReturnValue(), VisualConfig.shaderIntensity));
    }
}
