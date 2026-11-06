package com.firstsword.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 击杀掉落：「罪」和「汇聚核心」都是 1% 概率，并且每个玩家各只掉一次。
 *
 * 技能伤害走的是 playerAttack，所以雷、流星、瞬闪斩打死的怪同样参与判定。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class CoreDrops
{
    /** 罪：1% */
    private static final float SIN_CHANCE = 0.01F;

    /** 汇聚核心：1% */
    private static final float CONVERGENCE_CHANCE = 0.01F;

    private static final String SIN_KEY = "firstsword_core:sin_dropped";
    private static final String CONVERGENCE_KEY = "firstsword_core:convergence_dropped";

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity))
        {
            return;
        }

        // 玩家之间互杀不算
        if (entity instanceof Player)
        {
            return;
        }

        if (!(entity.level() instanceof ServerLevel level))
        {
            return;
        }

        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        CompoundTag data = player.getPersistentData();

        roll(player, level, entity, data, SIN_KEY, SIN_CHANCE,
                FirstSwordCore.SIN.get(), "message.firstsword_core.drop.sin");

        roll(player, level, entity, data, CONVERGENCE_KEY, CONVERGENCE_CHANCE,
                FirstSwordCore.METEOR_CORE.get(), "message.firstsword_core.drop.convergence");
    }

    /** 掷一次概率；中了就标记成「已经给过」，以后不再掉 */
    private static void roll(ServerPlayer player, ServerLevel level, LivingEntity entity,
                             CompoundTag data, String key, float chance, Item item, String messageKey)
    {
        if (data.getBoolean(key) || level.random.nextFloat() >= chance)
        {
            return;
        }

        data.putBoolean(key, true);

        entity.spawnAtLocation(new ItemStack(item));

        player.displayClientMessage(Component.translatable(messageKey), false);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private CoreDrops()
    {
    }
}
