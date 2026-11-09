package com.example.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** Перекрашивает текстуры глинта (свечения зачарования) на лету. */
public class GlintRecolor {
    static final Identifier[] IDS = {
        Identifier.ofVanilla("textures/misc/enchanted_glint_item.png"),
        Identifier.ofVanilla("textures/misc/enchanted_glint_entity.png")
    };
    static final NativeImage[] base = new NativeImage[2];
    static final NativeImageBackedTexture[] tex = new NativeImageBackedTexture[2];
    static boolean loaded, applied;
    static int ticks, lastKey = Integer.MIN_VALUE;

    public static void tick(MinecraftClient mc) {
        if (mc.world == null) return;
        ticks++;
        TextureManager tm = mc.getTextureManager();

        if (!VisualConfig.glintEnabled) {
            if (applied) {
                for (int i = 0; i < 2; i++) {
                    tm.destroyTexture(IDS[i]);
                    tex[i] = null;
                }
                applied = false;
                lastKey = Integer.MIN_VALUE;
            }
            return;
        }
        if (!loaded && !load(mc)) return;

        for (int i = 0; i < 2; i++) {
            if (tex[i] == null) {
                NativeImage copy = new NativeImage(base[i].getWidth(), base[i].getHeight(), false);
                copy.copyFrom(base[i]);
                tex[i] = new NativeImageBackedTexture(() -> "visualmod_glint", copy);
                lastKey = Integer.MIN_VALUE;
            }
            if (tm.getTexture(IDS[i]) != tex[i]) {
                tm.registerTexture(IDS[i], tex[i]);
                lastKey = Integer.MIN_VALUE;
            }
        }
        applied = true;

        boolean animated = VisualConfig.glintRainbow || VisualConfig.glintPulse;
        int key = animated
            ? ticks / 2
            : Objects.hash(VisualConfig.glintR, VisualConfig.glintG, VisualConfig.glintB, VisualConfig.glintBrightness);
        if (key == lastKey) return;
        lastKey = key;
        recolor(0);
        recolor(1);
    }

    static boolean load(MinecraftClient mc) {
        try {
            for (int i = 0; i < 2; i++) {
                try (InputStream in = mc.getResourceManager().open(IDS[i])) {
                    base[i] = NativeImage.read(in);
                }
            }
            loaded = true;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    static void recolor(int i) {
        NativeImage src = base[i];
        NativeImage dst = tex[i].getImage();
        if (dst == null) return;
        int w = src.getWidth(), h = src.getHeight();
        float time = ticks * VisualConfig.rainbowSpeed * 0.004f;
        float br = VisualConfig.glintBrightness;
        if (VisualConfig.glintPulse) br *= 0.65f + 0.35f * (float) Math.sin(ticks * 0.15);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = src.getColorArgb(x, y);
                int a = p >>> 24;
                float lum = Math.max(p >> 16 & 255, Math.max(p >> 8 & 255, p & 255)) / 255f;
                lum = Math.min(1f, lum * br);

                float r, g, b;
                if (VisualConfig.glintRainbow) {
                    int c = java.awt.Color.HSBtoRGB(((x + y) / (float) (w + h) + time) % 1f, 1f, 1f);
                    r = (c >> 16 & 255) / 255f;
                    g = (c >> 8 & 255) / 255f;
                    b = (c & 255) / 255f;
                } else {
                    r = VisualConfig.glintR;
                    g = VisualConfig.glintG;
                    b = VisualConfig.glintB;
                }
                dst.setColorArgb(x, y, a << 24
                    | (int) (r * lum * 255) << 16
                    | (int) (g * lum * 255) << 8
                    | (int) (b * lum * 255));
            }
        }
        tex[i].upload();
    }
}
