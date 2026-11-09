package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityCullMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void visualmod$cull(Entity entity, Frustum frustum, double x, double y, double z,
                                CallbackInfoReturnable<Boolean> cir) {
        if (!VisualConfig.entityCull) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || entity == mc.player) return;
        double max = VisualConfig.entityCullDistance;
        if (entity.squaredDistanceTo(mc.player) > max * max) {
            cir.setReturnValue(false);
        }
    }
}
