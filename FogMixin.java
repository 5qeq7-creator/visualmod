package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

// Сигнатура applyFog проверена по Yarn 1.21.11:
// applyFog(ByteBuffer, int, Vector4f, float x6)
@Mixin(FogRenderer.class)
public class FogMixin {
    @Inject(method = "applyFog", at = @At("HEAD"))
    private void visualmod$color(ByteBuffer buf, int pos, Vector4f color,
                                 float envStart, float envEnd, float rdStart, float rdEnd,
                                 float skyEnd, float cloudEnd, CallbackInfo ci) {
        if (VisualConfig.fogEnabled) {
            color.set(VisualConfig.fogR, VisualConfig.fogG, VisualConfig.fogB, 1f);
        } else if (VisualConfig.worldTint) {
            color.set(VisualConfig.worldTintR, VisualConfig.worldTintG, VisualConfig.worldTintB, 1f);
        } else if (VisualConfig.shaderMode > 0) {
            float[] t = VisualConfig.shaderFog();
            float k = 0.8f * VisualConfig.shaderIntensity;
            color.set(color.x * (1 - k) + t[0] * k, color.y * (1 - k) + t[1] * k, color.z * (1 - k) + t[2] * k, 1f);
        }
    }

    @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float visualmod$envStart(float v) {
        return VisualConfig.fogEnabled ? VisualConfig.fogStart : v;
    }

    @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float visualmod$envEnd(float v) {
        if (VisualConfig.fogEnabled) return VisualConfig.fogEnd;
        return VisualConfig.shaderMode > 0 ? v * (1f - 0.35f * VisualConfig.shaderIntensity) : v;
    }

    @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float visualmod$rdStart(float v) {
        return VisualConfig.fogEnabled ? VisualConfig.fogStart : v;
    }

    @ModifyVariable(method = "applyFog", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float visualmod$rdEnd(float v) {
        if (VisualConfig.fogEnabled) return VisualConfig.fogEnd;
        return VisualConfig.shaderMode > 0 ? v * (1f - 0.35f * VisualConfig.shaderIntensity) : v;
    }
}
