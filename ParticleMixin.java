package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void visualmod$limit(ParticleEffect effect, double x, double y, double z,
                                 double vx, double vy, double vz, CallbackInfoReturnable<Particle> cir) {
        if (!VisualConfig.particlesLimit) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        double max = VisualConfig.maxParticleDistance;
        if (VisualConfig.ambientParticles) max = Math.max(max, VisualConfig.wpRadius + 3);
        if (mc.player.squaredDistanceTo(x, y, z) > max * max) {
            cir.setReturnValue(null);
        }
    }
}
