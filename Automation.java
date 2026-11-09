package com.example.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class Automation {
    static int eatCooldown = 0, clickTicks = 0, prevSlot = -1;

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.interactionManager == null) return;

        if (mc.currentScreen != null) {
            if (prevSlot != -1) {
                p.getInventory().setSelectedSlot(prevSlot);
                prevSlot = -1;
            }
            return;
        }

        // ---- AutoEat ----
        if (eatCooldown > 0) eatCooldown--;
        if (VisualConfig.autoEat) {
            boolean eating = prevSlot != -1;
            if (eating && p.isUsingItem()) {
                mc.options.useKey.setPressed(true);
            } else if (eating) {
                p.getInventory().setSelectedSlot(prevSlot);
                prevSlot = -1;
                mc.options.useKey.setPressed(false);
            } else {
                int food = p.getHungerManager().getFoodLevel();
                if (eatCooldown == 0 && food < 20 && food <= VisualConfig.hungerThreshold) {
                    for (int i = 0; i < 9; i++) {
                        if (p.getInventory().getStack(i).contains(DataComponentTypes.FOOD)) {
                            prevSlot = p.getInventory().getSelectedSlot();
                            p.getInventory().setSelectedSlot(i);
                            mc.options.useKey.setPressed(true);
                            eatCooldown = VisualConfig.autoEatDelaySec * 20;
                            break;
                        }
                    }
                }
            }
        }

        // ---- AutoClicker ----
        if (VisualConfig.autoClicker && !p.isUsingItem()) {
            if (++clickTicks >= VisualConfig.clickIntervalSec * 20) {
                if (mc.crosshairTarget instanceof EntityHitResult hit) {
                    mc.interactionManager.attackEntity(p, hit.getEntity());
                    p.swingHand(Hand.MAIN_HAND);
                }
                clickTicks = 0;
            }
        } else {
            clickTicks = 0;
        }
    }
}
