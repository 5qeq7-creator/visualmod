package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// scale(PlayerEntityRenderState, MatrixStack) проверен по Yarn 1.21.11
@Mixin(PlayerEntityRenderer.class)
public class PlayerScaleMixin {
    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("RETURN"))
    private void visualmod$scale(PlayerEntityRenderState state, MatrixStack matrices, CallbackInfo ci) {
        if (!VisualConfig.playerScaleEnabled) return;
        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        if (me == null) return;
        // "это я" определяем по позиции
        if (Math.abs(state.x - me.getX()) < 0.5
            && Math.abs(state.y - me.getY()) < 0.5
            && Math.abs(state.z - me.getZ()) < 0.5) {
            matrices.scale(VisualConfig.scaleX, VisualConfig.scaleY, VisualConfig.scaleZ);
        }
    }
}
