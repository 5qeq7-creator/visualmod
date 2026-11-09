package com.example.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.UUID;

/**
 * Target HUD: плашка с головой, ником, здоровьем, предметами (правая рука, левая рука, броня) и полосой здоровья.
 * В открытом чате (T) плашку можно перетащить мышкой.
 */
public class TargetHud {
    static final int W = 168, H = 54;

    static PlayerEntity target;
    static long lastSeen;
    static UUID lastId;
    static float shown = -1f;
    static boolean dragging, wasDown;
    static int dragDX, dragDY;

    public static void onAttack(Entity e) {
        if (e instanceof PlayerEntity p) {
            target = p;
            lastSeen = System.currentTimeMillis();
        }
    }

    public static void render(DrawContext ctx) {
        if (!VisualConfig.targetHud) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        boolean chat = mc.currentScreen instanceof ChatScreen;
        if (mc.currentScreen != null && !chat) return;

        long now = System.currentTimeMillis();
        if (mc.targetedEntity instanceof PlayerEntity pe && pe != mc.player) {
            target = pe;
            lastSeen = now;
        }
        if (target != null && (target.isRemoved() || now - lastSeen > VisualConfig.thHold * 1000L)) {
            target = null;
        }
        PlayerEntity p = target;
        if (p == null && chat) p = mc.player; // в чате показываем предпросмотр на себе
        if (p == null) {
            shown = -1f;
            return;
        }

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int x = VisualConfig.thX >= 0 ? VisualConfig.thX : sw / 2 - W / 2;
        int y = VisualConfig.thY >= 0 ? VisualConfig.thY : sh / 2 + 40;
        x = Math.max(0, Math.min(sw - W, x));
        y = Math.max(0, Math.min(sh - H, y));

        // перетаскивание в открытом чате
        if (chat) {
            long win = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            double mx = mc.mouse.getX() * sw / (double) mc.getWindow().getWidth();
            double my = mc.mouse.getY() * sh / (double) mc.getWindow().getHeight();
            if (down && !wasDown && mx >= x && mx < x + W && my >= y && my < y + H) {
                dragging = true;
                dragDX = (int) (mx - x);
                dragDY = (int) (my - y);
            }
            if (dragging && down) {
                x = Math.max(0, Math.min(sw - W, (int) mx - dragDX));
                y = Math.max(0, Math.min(sh - H, (int) my - dragDY));
                VisualConfig.thX = x;
                VisualConfig.thY = y;
            }
            if (dragging && !down) {
                dragging = false;
                VisualConfig.save();
            }
            wasDown = down;
        } else {
            dragging = false;
            wasDown = false;
        }

        TextRenderer tr = mc.textRenderer;

        // здоровье: число показываем сразу точным значением, полоса плавно догоняет
        float hp = p.getHealth();
        float max = Math.max(1f, p.getMaxHealth());
        float abs = p.getAbsorptionAmount();
        UUID id = p.getUuid();
        if (shown < 0f || !id.equals(lastId)) shown = hp;
        lastId = id;
        shown += (hp - shown) * 0.2f;

        // фон
        round(ctx, x, y, W, H, 0xE6101014);
        if (chat) {
            ctx.fill(x, y, x + W, y + 1, 0xFFB066FF);
            ctx.fill(x, y + H - 1, x + W, y + H, 0xFFB066FF);
        }

        // голова
        ClientPlayNetworkHandler nh = mc.getNetworkHandler();
        PlayerListEntry entry = nh != null ? nh.getPlayerListEntry(id) : null;
        if (entry != null) {
            var tex = entry.getSkinTextures();
            PlayerSkinDrawer.draw(ctx, tex, x + 8, y + 9, 36);
        }

        // ник
        ctx.drawText(tr, p.getName().getString(), x + 52, y + 6, 0xFFFFFFFF, true);

        // здоровье числом справа: сердце + число (+ золотые сердца)
        String hs = fmt(hp);
        int rx = x + W - 8 - tr.getWidth(hs);
        ctx.drawText(tr, hs, rx, y + 6, 0xFFFFFFFF, true);
        if (abs > 0.01f) {
            String as = "+" + fmt(abs);
            int ax = rx - 3 - tr.getWidth(as);
            ctx.drawText(tr, as, ax, y + 6, 0xFFFFD24A, true);
            rx = ax;
        }
        heart(ctx, rx - 10, y + 6, 0xFFE03030);

        // предметы: правая рука, левая рука, шлем, нагрудник, поножи, ботинки
        ItemStack[] items = {
            p.getMainHandStack(),
            p.getOffHandStack(),
            p.getEquippedStack(EquipmentSlot.HEAD),
            p.getEquippedStack(EquipmentSlot.CHEST),
            p.getEquippedStack(EquipmentSlot.LEGS),
            p.getEquippedStack(EquipmentSlot.FEET)
        };
        for (int i = 0; i < items.length; i++) {
            int ix = x + 52 + i * 17, iy = y + 20;
            ctx.fill(ix, iy, ix + 16, iy + 16, 0x28FFFFFF);
            if (!items[i].isEmpty()) {
                ctx.drawItem(items[i], ix, iy);
                ctx.drawStackOverlay(tr, items[i], ix, iy);
            }
        }

        // полоса здоровья
        int bx = x + 52, by = y + 42, bw = W - 52 - 8, bh = 6;
        ctx.fill(bx, by, bx + bw, by + bh, 0xFF2A2A30);
        int fill = (int) (bw * Math.max(0f, Math.min(1f, shown / max)));
        int color = 0xFF000000
            | (int) (VisualConfig.thR * 255) << 16
            | (int) (VisualConfig.thG * 255) << 8
            | (int) (VisualConfig.thB * 255);
        ctx.fill(bx, by, bx + fill, by + bh, color);
        if (abs > 0.01f) {
            int af = (int) (bw * Math.max(0f, Math.min(1f, abs / max)));
            ctx.fill(bx, by, bx + af, by + 2, 0xFFFFD24A);
        }
    }

    static String fmt(float v) {
        return String.format(Locale.ROOT, v == Math.floor(v) ? "%.0f" : "%.1f", v);
    }

    static void round(DrawContext ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x + 2, y, x + w - 2, y + h, c);
        ctx.fill(x, y + 2, x + w, y + h - 2, c);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, c);
    }

    static void heart(DrawContext ctx, int x, int y, int color) {
        String[] rows = {".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."};
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (rows[r].charAt(c) == '#') ctx.fill(x + c, y + r, x + c + 1, y + r + 1, color);
            }
        }
    }
}
