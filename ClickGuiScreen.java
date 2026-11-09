package com.example.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ClickGuiScreen extends Screen {
    static final int W = 130, HH = 20, RH = 18, SH = 16, PICKER_H = 84;
    static final int ACCENT = 0xFFB066FF, ROW = 0xFF1A1A20, HOVER = 0xFF26262E, HEADER = 0xFF1E1E26;

    static class Slider {
        String name;
        float min, max;
        boolean integer;
        String[] labels;
        Supplier<Float> get;
        Consumer<Float> set;

        Slider(String n, float min, float max, boolean integer, Supplier<Float> g, Consumer<Float> s) {
            name = n;
            this.min = min;
            this.max = max;
            this.integer = integer;
            get = g;
            set = s;
        }

        Slider labels(String... l) {
            labels = l;
            return this;
        }
    }

    static class ColorPick {
        float[] hsb = new float[3];
        Supplier<float[]> getRgb;
        Consumer<float[]> setRgb;

        ColorPick(Supplier<float[]> g, Consumer<float[]> s) {
            getRgb = g;
            setRgb = s;
            float[] c = g.get();
            java.awt.Color.RGBtoHSB((int) (c[0] * 255), (int) (c[1] * 255), (int) (c[2] * 255), hsb);
        }

        void apply() {
            int c = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            setRgb.accept(new float[]{(c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f});
        }
    }

    static class Module {
        String name;
        BooleanSupplier enabled;
        Consumer<Boolean> setEnabled;
        IntSupplier color;
        List<Slider> sliders = new ArrayList<>();
        ColorPick picker;
        boolean open;

        Module(String n, BooleanSupplier e, Consumer<Boolean> s, IntSupplier c, Slider... sl) {
            name = n;
            enabled = e;
            setEnabled = s;
            color = c;
            sliders.addAll(Arrays.asList(sl));
        }
    }

    static class Panel {
        String title;
        int x, y;
        List<Module> mods = new ArrayList<>();

        Panel(String t) {
            title = t;
        }
    }

    final List<Panel> panels = new ArrayList<>();
    Slider dragging;
    ColorPick activePick;
    boolean activeHue;
    boolean wasL, wasR, wasKey = true;

    static int rgb(float r, float g, float b) {
        return 0xFF000000 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    public ClickGuiScreen() {
        super(Text.literal("VisualMod"));
        Panel visual = new Panel("Visual");
        Panel world = new Panel("World");
        Panel shaders = new Panel("Shaders");
        Panel particles = new Panel("Particles");
        Panel combat = new Panel("Combat");
        Panel pve = new Panel("PvE");
        Panel misc = new Panel("Player & FPS");
        Panel hud = new Panel("HUD");
        panels.addAll(Arrays.asList(visual, world, shaders, particles, combat, hud, pve, misc));

        // ================= Visual =================
        Module sky = new Module("Sky", () -> VisualConfig.skyEnabled, v -> VisualConfig.skyEnabled = v,
            () -> rgb(VisualConfig.skyR, VisualConfig.skyG, VisualConfig.skyB));
        sky.picker = new ColorPick(
            () -> new float[]{VisualConfig.skyR, VisualConfig.skyG, VisualConfig.skyB},
            c -> { VisualConfig.skyR = c[0]; VisualConfig.skyG = c[1]; VisualConfig.skyB = c[2]; });
        visual.mods.add(sky);

        Module fog = new Module("Fog", () -> VisualConfig.fogEnabled, v -> VisualConfig.fogEnabled = v,
            () -> rgb(VisualConfig.fogR, VisualConfig.fogG, VisualConfig.fogB),
            new Slider("Start", 0, 256, true, () -> VisualConfig.fogStart, v -> VisualConfig.fogStart = v),
            new Slider("End", 16, 512, true, () -> VisualConfig.fogEnd, v -> VisualConfig.fogEnd = v));
        fog.picker = new ColorPick(
            () -> new float[]{VisualConfig.fogR, VisualConfig.fogG, VisualConfig.fogB},
            c -> { VisualConfig.fogR = c[0]; VisualConfig.fogG = c[1]; VisualConfig.fogB = c[2]; });
        visual.mods.add(fog);

        visual.mods.add(new Module("Custom Hand", () -> VisualConfig.handEnabled, v -> VisualConfig.handEnabled = v, null,
            new Slider("X", -1, 1, false, () -> VisualConfig.handX, v -> VisualConfig.handX = v),
            new Slider("Y", -1, 1, false, () -> VisualConfig.handY, v -> VisualConfig.handY = v),
            new Slider("Z", -1, 1, false, () -> VisualConfig.handZ, v -> VisualConfig.handZ = v),
            new Slider("Scale", 0.3f, 2, false, () -> VisualConfig.handScale, v -> VisualConfig.handScale = v)));
        visual.mods.add(new Module("Hand Glow", () -> VisualConfig.handGlow, v -> VisualConfig.handGlow = v, null));

        Module glint = new Module("Glint", () -> VisualConfig.glintEnabled, v -> VisualConfig.glintEnabled = v,
            () -> rgb(VisualConfig.glintR, VisualConfig.glintG, VisualConfig.glintB),
            new Slider("Brightness", 0.3f, 2f, false, () -> VisualConfig.glintBrightness, v -> VisualConfig.glintBrightness = v));
        glint.picker = new ColorPick(
            () -> new float[]{VisualConfig.glintR, VisualConfig.glintG, VisualConfig.glintB},
            c -> { VisualConfig.glintR = c[0]; VisualConfig.glintG = c[1]; VisualConfig.glintB = c[2]; });
        visual.mods.add(glint);
        visual.mods.add(new Module("Glint Rainbow", () -> VisualConfig.glintRainbow, v -> VisualConfig.glintRainbow = v, null,
            new Slider("Speed", 1, 20, true, () -> (float) VisualConfig.rainbowSpeed, v -> VisualConfig.rainbowSpeed = v.intValue())));
        visual.mods.add(new Module("Glint Pulse", () -> VisualConfig.glintPulse, v -> VisualConfig.glintPulse = v, null));

        // ================= World (вид мира) =================
        world.mods.add(new Module("World Time", () -> VisualConfig.worldTime, v -> VisualConfig.worldTime = v, null,
            new Slider("Mode", 0, 4, true, () -> (float) VisualConfig.worldTimeMode, v -> VisualConfig.worldTimeMode = v.intValue())
                .labels("Day", "Morning", "Evening", "Night", "Custom"),
            new Slider("Custom time", 0, 24000, true, () -> (float) VisualConfig.worldTimeCustom, v -> VisualConfig.worldTimeCustom = v.intValue())));

        Module tint = new Module("World Tint", () -> VisualConfig.worldTint, v -> VisualConfig.worldTint = v,
            () -> rgb(VisualConfig.worldTintR, VisualConfig.worldTintG, VisualConfig.worldTintB));
        tint.picker = new ColorPick(
            () -> new float[]{VisualConfig.worldTintR, VisualConfig.worldTintG, VisualConfig.worldTintB},
            c -> { VisualConfig.worldTintR = c[0]; VisualConfig.worldTintG = c[1]; VisualConfig.worldTintB = c[2]; });
        world.mods.add(tint);

        // ================= Shaders (погода и яркость) =================
        shaders.mods.add(new Module("Snow", () -> VisualConfig.shaderMode == 1, v -> VisualConfig.shaderMode = v ? 1 : 0, null,
            new Slider("Intensity", 0.2f, 1f, false, () -> VisualConfig.shaderIntensity, v -> VisualConfig.shaderIntensity = v)));
        shaders.mods.add(new Module("Rain", () -> VisualConfig.shaderMode == 2, v -> VisualConfig.shaderMode = v ? 2 : 0, null,
            new Slider("Intensity", 0.2f, 1f, false, () -> VisualConfig.shaderIntensity, v -> VisualConfig.shaderIntensity = v)));
        shaders.mods.add(new Module("Bright", () -> VisualConfig.brightEnabled, v -> VisualConfig.brightEnabled = v, null,
            new Slider("Brightness", 1f, 12f, false, () -> VisualConfig.brightness, v -> VisualConfig.brightness = v)));

        // ================= Particles =================
        Module wp = new Module("World Particles", () -> VisualConfig.ambientParticles, v -> VisualConfig.ambientParticles = v,
            () -> rgb(VisualConfig.wpR, VisualConfig.wpG, VisualConfig.wpB),
            new Slider("Shape", 0, 14, true, () -> (float) VisualConfig.wpShape, v -> VisualConfig.wpShape = v.intValue())
                .labels("Dot", "Cube", "Tetra", "Octa", "Pyramid", "Line", "Cross", "Ring", "Star8", "Spiral",
                    "Petal pink", "Petal color", "Dollar", "Star", "Heart"),
            new Slider("Size", 0.3f, 3f, false, () -> VisualConfig.wpSize, v -> VisualConfig.wpSize = v),
            new Slider("Fall speed", 0.01f, 0.4f, false, () -> VisualConfig.wpFallSpeed, v -> VisualConfig.wpFallSpeed = v),
            new Slider("Radius", 3, 20, true, () -> (float) VisualConfig.wpRadius, v -> VisualConfig.wpRadius = v.intValue()),
            new Slider("Max count", 5, 80, true, () -> (float) VisualConfig.wpCount, v -> VisualConfig.wpCount = v.intValue()),
            new Slider("Per sec", 1, 40, true, () -> (float) VisualConfig.ambientPerSecond, v -> VisualConfig.ambientPerSecond = v.intValue()),
            new Slider("Fade in", 0, 40, true, () -> (float) VisualConfig.wpFadeIn, v -> VisualConfig.wpFadeIn = v.intValue()),
            new Slider("Fade out", 1, 60, true, () -> (float) VisualConfig.wpFadeOut, v -> VisualConfig.wpFadeOut = v.intValue()),
            new Slider("Land speed", 0, 1, false, () -> VisualConfig.wpLandSpeed, v -> VisualConfig.wpLandSpeed = v),
            new Slider("Ground time", 0, 100, true, () -> (float) VisualConfig.wpGroundTicks, v -> VisualConfig.wpGroundTicks = v.intValue()),
            new Slider("Petal life (s)", 2, 20, true, () -> (float) VisualConfig.wpPetalLife, v -> VisualConfig.wpPetalLife = v.intValue()));
        wp.picker = new ColorPick(
            () -> new float[]{VisualConfig.wpR, VisualConfig.wpG, VisualConfig.wpB},
            c -> { VisualConfig.wpR = c[0]; VisualConfig.wpG = c[1]; VisualConfig.wpB = c[2]; });
        particles.mods.add(wp);
        particles.mods.add(new Module("Particles Rainbow", () -> VisualConfig.wpRainbow, v -> VisualConfig.wpRainbow = v, null,
            new Slider("Speed", 1, 20, true, () -> (float) VisualConfig.rainbowSpeed, v -> VisualConfig.rainbowSpeed = v.intValue())));
        particles.mods.add(new Module("Particle Limit", () -> VisualConfig.particlesLimit, v -> VisualConfig.particlesLimit = v, null,
            new Slider("Distance", 8, 64, true, () -> (float) VisualConfig.maxParticleDistance, v -> VisualConfig.maxParticleDistance = v.intValue())));

        // ================= Combat =================
        combat.mods.add(new Module("Crit Sound", () -> VisualConfig.critSound, v -> VisualConfig.critSound = v, null,
            new Slider("Sound", 0, 5, true, () -> (float) VisualConfig.critSoundType, v -> VisualConfig.critSoundType = v.intValue())
                .labels(CritSound.NAMES),
            new Slider("Volume", 0.1f, 1f, false, () -> VisualConfig.critSoundVolume, v -> VisualConfig.critSoundVolume = v),
            new Slider("Pitch", 0.5f, 2f, false, () -> VisualConfig.critSoundPitch, v -> VisualConfig.critSoundPitch = v)));
        combat.mods.add(new Module("Armor Tint", () -> VisualConfig.armorTint, v -> VisualConfig.armorTint = v, null,
            new Slider("Strength", 0.1f, 1f, false, () -> VisualConfig.armorTintStrength, v -> VisualConfig.armorTintStrength = v)));

        // ================= HUD =================
        Module th = new Module("Target HUD", () -> VisualConfig.targetHud, v -> VisualConfig.targetHud = v,
            () -> rgb(VisualConfig.thR, VisualConfig.thG, VisualConfig.thB),
            new Slider("Hold (s)", 1, 15, true, () -> (float) VisualConfig.thHold, v -> VisualConfig.thHold = v.intValue()));
        th.picker = new ColorPick(
            () -> new float[]{VisualConfig.thR, VisualConfig.thG, VisualConfig.thB},
            c -> { VisualConfig.thR = c[0]; VisualConfig.thG = c[1]; VisualConfig.thB = c[2]; });
        hud.mods.add(th);

        // ================= PvE =================
        pve.mods.add(new Module("AutoEat", () -> VisualConfig.autoEat, v -> VisualConfig.autoEat = v, null,
            new Slider("Delay (s)", 1, 10, true, () -> (float) VisualConfig.autoEatDelaySec, v -> VisualConfig.autoEatDelaySec = v.intValue()),
            new Slider("Hunger <=", 1, 19, true, () -> (float) VisualConfig.hungerThreshold, v -> VisualConfig.hungerThreshold = v.intValue())));
        pve.mods.add(new Module("AutoClicker", () -> VisualConfig.autoClicker, v -> VisualConfig.autoClicker = v, null,
            new Slider("Interval (s)", 0.1f, 5, false, () -> VisualConfig.clickIntervalSec, v -> VisualConfig.clickIntervalSec = v)));

        // ================= Player & FPS =================
        misc.mods.add(new Module("Player Scale", () -> VisualConfig.playerScaleEnabled, v -> VisualConfig.playerScaleEnabled = v, null,
            new Slider("X", 0.5f, 1.5f, false, () -> VisualConfig.scaleX, v -> VisualConfig.scaleX = v),
            new Slider("Y", 0.5f, 1.5f, false, () -> VisualConfig.scaleY, v -> VisualConfig.scaleY = v),
            new Slider("Z", 0.5f, 1.5f, false, () -> VisualConfig.scaleZ, v -> VisualConfig.scaleZ = v)));
        misc.mods.add(new Module("Entity Cull", () -> VisualConfig.entityCull, v -> VisualConfig.entityCull = v, null,
            new Slider("Distance", 16, 128, true, () -> (float) VisualConfig.entityCullDistance, v -> VisualConfig.entityCullDistance = v.intValue())));
    }

    static boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    static int panelHeight(Panel p) {
        int h = HH;
        for (Module m : p.mods) {
            h += RH;
            if (m.open) {
                if (m.picker != null) h += PICKER_H;
                h += m.sliders.size() * SH;
            }
        }
        return h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        VisualConfig.save();
        super.removed();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        long win = mc.getWindow().getHandle();

        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean lClick = l && !wasL, rClick = r && !wasR;
        wasL = l;
        wasR = r;
        if (!l) {
            dragging = null;
            activePick = null;
        }

        boolean key = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (key && !wasKey) {
            close();
            return;
        }
        wasKey = key;

        ctx.fill(0, 0, width, height, 0x80000000);

        // раскладка панелей по колонкам: сколько влезает по ширине экрана
        int cols = Math.max(1, (width - 10) / (W + 10));
        int[] colY = new int[cols];
        Arrays.fill(colY, 10);
        for (int i = 0; i < panels.size(); i++) {
            Panel p = panels.get(i);
            int c = i % cols;
            p.x = 10 + c * (W + 10);
            p.y = colY[c];
            colY[c] += panelHeight(p) + 8;
        }

        for (Panel p : panels) {
            int y = p.y;
            ctx.fill(p.x, y, p.x + W, y + HH, HEADER);
            ctx.fill(p.x, y, p.x + W, y + 2, ACCENT);
            ctx.drawText(tr, p.title, p.x + 8, y + 7, 0xFFFFFFFF, false);
            y += HH;

            for (Module m : p.mods) {
                boolean on = m.enabled.getAsBoolean();
                boolean hov = hit(mx, my, p.x, y, W, RH);
                ctx.fill(p.x, y, p.x + W, y + RH, hov ? HOVER : ROW);
                if (on) ctx.fill(p.x, y, p.x + 2, y + RH, ACCENT);
                ctx.drawText(tr, m.name, p.x + 8, y + 5, on ? 0xFFFFFFFF : 0xFF9A9AA5, false);
                if (m.color != null) ctx.fill(p.x + W - 14, y + 5, p.x + W - 6, y + 13, m.color.getAsInt());
                if (hov && lClick) m.setEnabled.accept(!on);
                if (hov && rClick) m.open = !m.open;
                y += RH;

                if (!m.open) continue;

                if (m.picker != null) {
                    ColorPick cp = m.picker;
                    int sx = p.x + 5, sy = y + 4, sw = 120, sqh = 60, hy = y + 68;
                    float h = cp.hsb[0];

                    ctx.fill(p.x, y, p.x + W, y + PICKER_H, ROW);
                    for (int i = 0; i < sw; i += 2) {
                        int top = 0xFF000000 | java.awt.Color.HSBtoRGB(h, i / (float) (sw - 1), 1f);
                        ctx.fillGradient(sx + i, sy, sx + i + 2, sy + sqh, top, 0xFF000000);
                    }
                    for (int i = 0; i < sw; i += 2) {
                        ctx.fill(sx + i, hy, sx + i + 2, hy + 10,
                            0xFF000000 | java.awt.Color.HSBtoRGB(i / (float) (sw - 1), 1f, 1f));
                    }

                    int mkx = sx + (int) (cp.hsb[1] * (sw - 1));
                    int mky = sy + (int) ((1 - cp.hsb[2]) * (sqh - 1));
                    ctx.fill(mkx - 2, mky - 2, mkx + 3, mky + 3, 0xFFFFFFFF);
                    ctx.fill(mkx - 1, mky - 1, mkx + 2, mky + 2,
                        0xFF000000 | java.awt.Color.HSBtoRGB(cp.hsb[0], cp.hsb[1], cp.hsb[2]));
                    int hx = sx + (int) (cp.hsb[0] * (sw - 1));
                    ctx.fill(hx - 1, hy - 1, hx + 2, hy + 11, 0xFFFFFFFF);

                    if (lClick && hit(mx, my, sx, sy, sw, sqh)) { activePick = cp; activeHue = false; }
                    if (lClick && hit(mx, my, sx, hy, sw, 10)) { activePick = cp; activeHue = true; }
                    if (l && activePick == cp) {
                        float t = Math.max(0, Math.min(1, (mx - sx) / (float) (sw - 1)));
                        if (activeHue) {
                            cp.hsb[0] = t;
                        } else {
                            cp.hsb[1] = t;
                            cp.hsb[2] = 1 - Math.max(0, Math.min(1, (my - sy) / (float) (sqh - 1)));
                        }
                        cp.apply();
                    }
                    y += PICKER_H;
                }

                for (Slider s : m.sliders) {
                    boolean sh = hit(mx, my, p.x, y, W, SH);
                    ctx.fill(p.x, y, p.x + W, y + SH, ROW);
                    float v = s.get.get();
                    float t = (v - s.min) / (s.max - s.min);
                    ctx.fill(p.x + 4, y + SH - 4, p.x + 4 + (int) ((W - 8) * Math.max(0, Math.min(1, t))), y + SH - 2, ACCENT);
                    String val;
                    if (s.labels != null) {
                        int li = Math.max(0, Math.min(s.labels.length - 1, Math.round(v)));
                        val = s.labels[li];
                    } else {
                        val = s.integer ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.2f", v);
                    }
                    ctx.drawText(tr, s.name + ": " + val, p.x + 6, y + 3, 0xFFCFCFD6, false);
                    if (sh && lClick) dragging = s;
                    if (dragging == s && l) {
                        float nt = Math.max(0, Math.min(1, (mx - (p.x + 4)) / (float) (W - 8)));
                        float nv = s.min + nt * (s.max - s.min);
                        s.set.accept(s.integer ? (float) Math.round(nv) : nv);
                    }
                    y += SH;
                }
            }
        }
    }
}
