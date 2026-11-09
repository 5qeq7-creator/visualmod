package com.example.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VisualClient implements ClientModInitializer {
    private boolean was;

    @Override
    public void onInitializeClient() {
        VisualConfig.load();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            CritSound.onAttack(player, entity);
            TargetHud.onAttack(entity);
            return ActionResult.PASS;
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.of("visualmod", "target_hud"),
            (ctx, tickCounter) -> TargetHud.render(ctx));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Automation.tick(mc);
            GlintRecolor.tick(mc);
            WorldParticles.tick(mc);

            boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !was && mc.currentScreen == null && mc.player != null) {
                mc.setScreen(new ClickGuiScreen());
            }
            was = down;
        });
    }
}
