package com.example.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** Свой звук при критическом ударе (условия крита как в ванили). */
public class CritSound {
    public static final String[] NAMES = {"Cat meow", "Level up", "XP orb", "Ping", "Allay", "Villager"};

    public static void onAttack(PlayerEntity player, Entity target) {
        if (!VisualConfig.critSound) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (player != mc.player) return;

        boolean crit = player.getAttackCooldownProgress(0.5f) > 0.9f
            && player.fallDistance > 0.0
            && !player.isOnGround()
            && !player.isClimbing()
            && !player.isTouchingWater()
            && !player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !player.hasVehicle()
            && target instanceof LivingEntity
            && !player.isSprinting();
        if (!crit) return;

        mc.getSoundManager().play(PositionedSoundInstance.master(
            pick(VisualConfig.critSoundType), VisualConfig.critSoundPitch, VisualConfig.critSoundVolume));
    }

    private static SoundEvent pick(int type) {
        switch (type) {
            case 1: return SoundEvents.ENTITY_PLAYER_LEVELUP;
            case 2: return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case 3: return SoundEvents.ENTITY_ARROW_HIT_PLAYER;
            case 4: return SoundEvents.ENTITY_ALLAY_ITEM_GIVEN;
            case 5: return SoundEvents.ENTITY_VILLAGER_YES;
            default: return SoundEvents.ENTITY_CAT_AMBIENT;
        }
    }
}
