package com.example.visual;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Частицы мира.
 * Фигуры 0..9: объёмные фигуры из цветных точек-пыли.
 * Фигуры 10..11: настоящие лепестки (ванильные листья с физикой падения): розовые или любого цвета.
 * Фигуры 12..14: доллар, звезда, сердце (плоские значки из точек, всегда повёрнуты к тебе).
 */
public class WorldParticles {
    static class P {
        double x, y, z, dx, dz;
        int age;
        int landedAge = -1;
        float spin;
    }

    static final List<P> list = new ArrayList<>();
    static double acc;
    static int tickCount;

    /** 0 точка, 1 куб, 2 тетраэдр, 3 октаэдр, 4 пирамида, 5 линия, 6 крест, 7 кольцо, 8 звезда, 9 спираль */
    static final float[][][] SHAPES = new float[10][][];

    static final String[][] GLYPHS = {
        // доллар
        {"..#..",
         ".###.",
         "#.#.#",
         "#.#..",
         ".###.",
         "..#.#",
         "#.#.#",
         ".###.",
         "..#.."},
        // звезда
        {"...#...",
         "...#...",
         "..###..",
         "#######",
         "..###..",
         ".##.##.",
         ".#...#."},
        // сердце
        {".##.##.",
         "#######",
         "#######",
         ".#####.",
         "..###..",
         "...#..."}
    };

    static {
        SHAPES[0] = new float[][]{{0, 0, 0}};

        float[][] cube = new float[8][];
        int c = 0;
        for (int x = -1; x <= 1; x += 2)
            for (int y = -1; y <= 1; y += 2)
                for (int z = -1; z <= 1; z += 2)
                    cube[c++] = new float[]{x, y, z};
        SHAPES[1] = cube;

        SHAPES[2] = new float[][]{{1, 1, 1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, 1}};
        SHAPES[3] = new float[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        SHAPES[4] = new float[][]{{1, -1, 1}, {1, -1, -1}, {-1, -1, 1}, {-1, -1, -1}, {0, 1, 0}};

        float[][] line = new float[5][];
        for (int i = 0; i < 5; i++) line[i] = new float[]{0, -1f + i * 0.5f, 0};
        SHAPES[5] = line;

        SHAPES[6] = new float[][]{{0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

        float[][] ring = new float[8][];
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            ring[i] = new float[]{(float) Math.cos(a), 0, (float) Math.sin(a)};
        }
        SHAPES[7] = ring;

        float[][] star = new float[8][];
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            float rr = (i % 2 == 0) ? 1f : 0.45f;
            star[i] = new float[]{(float) (Math.cos(a) * rr), 0, (float) (Math.sin(a) * rr)};
        }
        SHAPES[8] = star;

        float[][] helix = new float[8][];
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 2;
            helix[i] = new float[]{(float) (Math.cos(a) * 0.8), -1f + i * 0.28f, (float) (Math.sin(a) * 0.8)};
        }
        SHAPES[9] = helix;
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity pl = mc.player;
        ClientWorld w = mc.world;
        if (!VisualConfig.ambientParticles || pl == null || w == null) {
            if (!list.isEmpty()) list.clear();
            return;
        }
        tickCount++;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double radius = Math.max(3, Math.min(20, VisualConfig.wpRadius));
        int max = Math.max(1, VisualConfig.wpCount);
        int shape = Math.max(0, Math.min(14, VisualConfig.wpShape));
        boolean petal = shape == 10 || shape == 11;

        // скорость появления; для лепестков ограничиваем так, чтобы одновременно жило не больше "Max count"
        double rate = VisualConfig.ambientPerSecond;
        if (petal) rate = Math.min(rate, max / (double) Math.max(1, VisualConfig.wpPetalLife));
        acc += rate / 20.0;

        while (acc >= 1) {
            acc -= 1;
            if (petal) {
                spawnPetal(mc, pl, r, radius, shape);
            } else if (list.size() < max) {
                P p = new P();
                double a = r.nextDouble() * Math.PI * 2;
                double d = Math.sqrt(r.nextDouble()) * radius;
                p.x = pl.getX() + Math.cos(a) * d;
                p.z = pl.getZ() + Math.sin(a) * d;
                p.y = pl.getY() + 3 + r.nextDouble() * 7;
                double da = r.nextDouble() * Math.PI * 2;
                p.dx = Math.cos(da);
                p.dz = Math.sin(da);
                p.spin = r.nextFloat() * 6.28f;
                list.add(p);
            }
        }
        if (acc > 5) acc = 0;

        if (petal) {
            if (!list.isEmpty()) list.clear();
            return;
        }

        int budget = 500; // защита FPS: максимум частиц пыли за тик
        int idx = 0;
        Iterator<P> it = list.iterator();
        while (it.hasNext()) {
            P p = it.next();
            idx++;
            p.age++;
            p.spin += 0.12f;

            double hx = p.x - pl.getX(), hz = p.z - pl.getZ();
            double dist = Math.sqrt(hx * hx + hz * hz);
            if (dist > radius + 1 || p.age > 1200 || p.y < w.getBottomY()) {
                it.remove();
                continue;
            }

            if (p.landedAge < 0) {
                p.y -= VisualConfig.wpFallSpeed;
                BlockPos bp = BlockPos.ofFloored(p.x, p.y - 0.05, p.z);
                BlockState st = w.getBlockState(bp);
                if (!st.getCollisionShape(w, bp).isEmpty()) {
                    p.landedAge = 0;
                    p.y = bp.getY() + 1.0;
                }
            } else {
                p.landedAge++;
                p.x += p.dx * VisualConfig.wpLandSpeed * 0.05;
                p.z += p.dz * VisualConfig.wpLandSpeed * 0.05;
                if (p.landedAge > VisualConfig.wpGroundTicks + VisualConfig.wpFadeOut) {
                    it.remove();
                    continue;
                }
            }

            // появление / растворение
            float fade = Math.min(1f, p.age / (float) Math.max(1, VisualConfig.wpFadeIn));
            if (p.landedAge > VisualConfig.wpGroundTicks) {
                fade *= 1f - (p.landedAge - VisualConfig.wpGroundTicks) / (float) Math.max(1, VisualConfig.wpFadeOut);
            }
            fade *= (float) Math.max(0, Math.min(1, (radius - dist) / 3.0));
            if (fade <= 0.02f) continue;

            // рисуем раз в 2 тика (частица живёт 3 тика), это вдвое дешевле
            if (((tickCount + idx) & 1) != 0) continue;
            if (budget <= 0) continue;
            budget -= emit(mc, pl, p, idx, fade, dist, shape);
        }
    }

    /** Лепестки: ванильные листья, у них своя физика (покачивание, вращение, лежат на земле). */
    private static void spawnPetal(MinecraftClient mc, ClientPlayerEntity pl, ThreadLocalRandom r, double radius, int shape) {
        double a = r.nextDouble() * Math.PI * 2;
        double d = Math.sqrt(r.nextDouble()) * radius;
        double x = pl.getX() + Math.cos(a) * d;
        double z = pl.getZ() + Math.sin(a) * d;
        double y = pl.getY() + 3 + r.nextDouble() * 5;

        ParticleEffect eff;
        if (shape == 10) {
            eff = ParticleTypes.CHERRY_LEAVES;
        } else {
            float cr, cg, cb;
            if (VisualConfig.wpRainbow) {
                int c = java.awt.Color.HSBtoRGB(r.nextFloat(), 0.9f, 1f);
                cr = (c >> 16 & 255) / 255f;
                cg = (c >> 8 & 255) / 255f;
                cb = (c & 255) / 255f;
            } else {
                cr = VisualConfig.wpR;
                cg = VisualConfig.wpG;
                cb = VisualConfig.wpB;
            }
            eff = TintedParticleEffect.create(ParticleTypes.TINTED_LEAVES, cr, cg, cb);
        }
        Particle part = mc.particleManager.addParticle(eff, x, y, z, 0, 0, 0);
        if (part != null) {
            part.scale(VisualConfig.wpSize);
            part.setMaxAge(Math.max(20, VisualConfig.wpPetalLife * 20));
        }
    }

    private static int emit(MinecraftClient mc, ClientPlayerEntity pl, P p, int idx, float fade, double dist, int shape) {
        if (dist > 12) shape = 0; // вдали рисуем одной точкой

        int rgb;
        if (VisualConfig.wpRainbow) {
            float hue = (p.age * VisualConfig.rainbowSpeed * 0.01f + idx * 0.07f) % 1f;
            rgb = java.awt.Color.HSBtoRGB(hue, 1f, 1f) & 0xFFFFFF;
        } else {
            rgb = ((int) (VisualConfig.wpR * 255) << 16) | ((int) (VisualConfig.wpG * 255) << 8) | (int) (VisualConfig.wpB * 255);
        }

        float dot = Math.max(0.05f, Math.min(4f, 0.45f * VisualConfig.wpSize * fade + 0.05f));
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int count = 0;

        if (shape >= 12) {
            // плоский значок, повёрнутый к игроку, с лёгким покачиванием
            String[] g = GLYPHS[shape - 12];
            double dirX = pl.getX() - p.x, dirZ = pl.getZ() - p.z;
            double a = Math.atan2(dirZ, dirX) + 0.35 * Math.sin(p.spin);
            double rx = Math.sin(a), rz = -Math.cos(a);
            double cell = 0.11 * VisualConfig.wpSize;
            int h = g.length, wd = g[0].length();
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < wd; col++) {
                    if (g[row].charAt(col) != '#') continue;
                    if (fade < 0.6f && rnd.nextFloat() > fade / 0.6f) continue; // растворение: точки пропадают
                    double u = (col - (wd - 1) / 2.0) * cell;
                    double v = ((h - 1) / 2.0 - row) * cell;
                    Particle part = mc.particleManager.addParticle(new DustParticleEffect(rgb, dot),
                        p.x + rx * u, p.y + v + h * cell / 2.0, p.z + rz * u, 0, 0, 0);
                    if (part != null) part.setMaxAge(3);
                    count++;
                }
            }
            return count;
        }

        shape = Math.max(0, Math.min(9, shape));
        float e = 0.25f * VisualConfig.wpSize;
        double cos = Math.cos(p.spin), sin = Math.sin(p.spin);
        for (float[] o : SHAPES[shape]) {
            if (fade < 0.6f && rnd.nextFloat() > fade / 0.6f) continue; // растворение
            double ox = (o[0] * cos - o[2] * sin) * e;
            double oz = (o[0] * sin + o[2] * cos) * e;
            double oy = o[1] * e;
            Particle part = mc.particleManager.addParticle(
                new DustParticleEffect(rgb, dot), p.x + ox, p.y + oy + e, p.z + oz, 0, 0, 0);
            if (part != null) part.setMaxAge(3);
            count++;
        }
        return count;
    }
}
