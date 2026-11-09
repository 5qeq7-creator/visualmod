package com.example.visual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class VisualConfig {
    // --- Небо ---
    public static boolean skyEnabled = false;
    public static float skyR = 0.5f, skyG = 0.2f, skyB = 0.9f;

    // --- Туман ---
    public static boolean fogEnabled = false;
    public static float fogStart = 20f, fogEnd = 120f;
    public static float fogR = 0.5f, fogG = 0.2f, fogB = 0.9f;

    // --- Частицы ---
    public static boolean particlesLimit = true;
    public static int maxParticleDistance = 24;
    public static boolean ambientParticles = false;
    public static int ambientPerSecond = 10;  // сколько частиц мира появляется в секунду

    // --- Рука ---
    public static boolean handEnabled = false;
    public static float handX = 0.1f, handY = -0.05f, handZ = 0f, handScale = 1f;
    public static boolean handGlow = false;

    // --- Глинт (цвет зачарования) ---
    public static boolean glintEnabled = false;
    public static float glintR = 0.6f, glintG = 0.25f, glintB = 1.0f;
    public static float glintBrightness = 1.0f;
    public static boolean glintRainbow = false;
    public static int rainbowSpeed = 5;
    public static boolean glintPulse = false;

    // --- Игрок ---
    public static boolean playerScaleEnabled = false;
    public static float scaleX = 1f, scaleY = 1f, scaleZ = 1f;

    // --- FPS ---
    public static boolean entityCull = true;
    public static int entityCullDistance = 48;

    // --- PvE ---
    public static boolean autoEat = false;
    public static int autoEatDelaySec = 2;
    public static int hungerThreshold = 14;
    public static boolean autoClicker = false;
    public static float clickIntervalSec = 1.0f;

    // --- Мир: время суток и оттенок ---
    public static boolean worldTime = false;
    public static int worldTimeMode = 0;      // 0 день, 1 утро, 2 вечер, 3 ночь, 4 своё
    public static int worldTimeCustom = 6000; // 0..24000
    public static boolean worldTint = false;
    public static float worldTintR = 0.3f, worldTintG = 0.4f, worldTintB = 0.8f;

    // --- Частицы мира (геометрические) ---
    public static int wpShape = 1;            // 0..14
    public static float wpR = 0.6f, wpG = 0.25f, wpB = 1.0f;
    public static boolean wpRainbow = false;
    public static float wpSize = 1.0f;
    public static float wpFallSpeed = 0.05f;  // блоков за тик
    public static int wpRadius = 12;          // 3..20
    public static int wpCount = 30;           // максимум частиц одновременно
    public static int wpFadeIn = 10;          // тиков
    public static int wpFadeOut = 20;         // тиков
    public static float wpLandSpeed = 0.0f;   // 0 = замирает при приземлении, 1 = скользит
    public static int wpGroundTicks = 20;     // сколько тиков лежит на земле
    public static int wpPetalLife = 8;        // сколько секунд живут лепестки (фигуры Petal)

    // --- Звук крита ---
    public static boolean critSound = false;
    public static int critSoundType = 0;
    public static float critSoundVolume = 1.0f;
    public static float critSoundPitch = 1.2f;

    // --- Оттенок брони по прочности ---
    public static boolean armorTint = false;
    public static float armorTintStrength = 0.4f;

    // --- Target HUD ---
    public static boolean targetHud = false;
    public static int thX = -1, thY = -1;     // -1 = по умолчанию (под прицелом)
    public static float thR = 0.95f, thG = 0.6f, thB = 0.9f; // цвет полосы здоровья (розовый)
    public static int thHold = 4;             // сколько секунд держать цель после последнего удара

    // --- Shaders (погода и яркость) ---
    public static int shaderMode = 0;          // 0 выкл, 1 снег, 2 дождь
    public static float shaderIntensity = 1.0f; // 0.2..1
    public static boolean brightEnabled = false;
    public static float brightness = 4.0f;     // 1 = как в ванили (макс. ползунка), больше = ярче

    // служебное, не сохраняется
    public static transient boolean inHand = false;

    /** Цвет неба для пресета: снег = светло-голубой, дождь = серый. */
    public static float[] shaderSky() {
        return shaderMode == 1 ? new float[]{0.80f, 0.87f, 0.96f} : new float[]{0.45f, 0.48f, 0.53f};
    }

    /** Цвет тумана для пресета: снег = белёсый, дождь = серый. */
    public static float[] shaderFog() {
        return shaderMode == 1 ? new float[]{0.88f, 0.92f, 0.98f} : new float[]{0.52f, 0.54f, 0.58f};
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("visualmod.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Field fl : VisualConfig.class.getDeclaredFields()) {
            int m = fl.getModifiers();
            if (!Modifier.isStatic(m) || Modifier.isTransient(m) || Modifier.isFinal(m)) continue;
            String v = p.getProperty(fl.getName());
            if (v == null) continue;
            try {
                Class<?> t = fl.getType();
                if (t == boolean.class) fl.setBoolean(null, Boolean.parseBoolean(v));
                else if (t == int.class) fl.setInt(null, Integer.parseInt(v));
                else if (t == float.class) fl.setFloat(null, Float.parseFloat(v));
            } catch (Exception ignored) {
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Field fl : VisualConfig.class.getDeclaredFields()) {
            int m = fl.getModifiers();
            if (!Modifier.isStatic(m) || Modifier.isTransient(m) || Modifier.isFinal(m)) continue;
            try {
                p.setProperty(fl.getName(), String.valueOf(fl.get(null)));
            } catch (Exception ignored) {
            }
        }
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "VisualMod settings");
        } catch (IOException ignored) {
        }
    }
}
