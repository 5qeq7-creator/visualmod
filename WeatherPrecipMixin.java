package com.example.visual.mixin;

import com.example.visual.VisualConfig;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// WeatherRendering.getPrecipitationAt(World, BlockPos) -> Biome.Precipitation найден в Yarn 1.21.11.
// Только клиентская картинка: везде идёт снег или дождь, независимо от биома.
@Mixin(WeatherRendering.class)
public class WeatherPrecipMixin {
    @Inject(method = "getPrecipitationAt", at = @At("RETURN"), cancellable = true)
    private void visualmod$precip(World world, BlockPos pos, CallbackInfoReturnable<Biome.Precipitation> cir) {
        if (VisualConfig.shaderMode == 1) cir.setReturnValue(Biome.Precipitation.SNOW);
        else if (VisualConfig.shaderMode == 2) cir.setReturnValue(Biome.Precipitation.RAIN);
    }
}
