package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// ClientWorld#getSkyColor(Vec3d, float) -> int (ARGB), проверено для Yarn 1.21.4/1.21.5
@Mixin(ClientWorld.class)
public class SkyMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void visualmod$sky(Vec3d cameraPos, float tickProgress, CallbackInfoReturnable<Integer> cir) {
        float fr, fg, fb;
        if (VisualConfig.skyEnabled) {
            fr = VisualConfig.skyR; fg = VisualConfig.skyG; fb = VisualConfig.skyB;
        } else if (VisualConfig.worldTint) {
            fr = VisualConfig.worldTintR; fg = VisualConfig.worldTintG; fb = VisualConfig.worldTintB;
        } else if (VisualConfig.shaderMode > 0) {
            // пресет Snow/Rain: смешиваем ванильный цвет неба с серым/белёсым
            float[] t = VisualConfig.shaderSky();
            float k = 0.8f * VisualConfig.shaderIntensity;
            int o = cir.getReturnValue();
            fr = (o >> 16 & 255) / 255f * (1 - k) + t[0] * k;
            fg = (o >> 8 & 255) / 255f * (1 - k) + t[1] * k;
            fb = (o & 255) / 255f * (1 - k) + t[2] * k;
        } else {
            return;
        }
        int r = (int) (fr * 255), g = (int) (fg * 255), b = (int) (fb * 255);
        cir.setReturnValue(0xFF000000 | r << 16 | g << 8 | b);
    }
}
