package com.firstsword.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 顷火核心留下的灼烧：每秒 8 点，持续 7 秒。
 *
 * 没有做成自定义 MobEffect，而是自己记一张表——伤害要记在玩家头上，
 * 这样被打死的怪仍然会掉「罪」，用原版效果做不到这一点。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class SinBurn
{
    /** 持续时间（tick），7 秒 */
    private static final int DURATION = 140;

    /** 每这么多 tick 结算一次伤害 */
    private static final int INTERVAL = 20;

    /** 每跳伤害 */
    private static final float DAMAGE = 8.0F;

    private static final List<Burn> ACTIVE = new ArrayList<>();

    public static void apply(ServerLevel level, Player owner, LivingEntity target)
    {
        // 同一只怪只保留一条，再次命中是刷新时间而不是叠加
        ACTIVE.removeIf(burn -> burn.target == target);
        ACTIVE.add(new Burn(level, owner, target, DURATION));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        if (ACTIVE.isEmpty())
        {
            return;
        }

        Iterator<Burn> iterator = ACTIVE.iterator();

        while (iterator.hasNext())
        {
            Burn burn = iterator.next();

            if (!burn.target.isAlive()
                    || burn.target.level() != burn.level
                    || --burn.ticksLeft <= 0)
            {
                iterator.remove();
                continue;
            }

            burn.level.sendParticles(ParticleTypes.FLAME,
                    burn.target.getX(),
                    burn.target.getY() + burn.target.getBbHeight() * 0.5D,
                    burn.target.getZ(),
                    2, 0.25D, 0.35D, 0.25D, 0.01D);

            if (burn.ticksLeft % INTERVAL != 0)
            {
                continue;
            }

            DamageSource source = burn.owner.isRemoved()
                    ? burn.level.damageSources().magic()
                    : burn.level.damageSources().playerAttack(burn.owner);

            burn.target.hurt(source, DAMAGE);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        ACTIVE.clear();
    }

    private static final class Burn
    {
        final ServerLevel level;
        final Player owner;
        final LivingEntity target;
        int ticksLeft;

        Burn(ServerLevel level, Player owner, LivingEntity target, int ticksLeft)
        {
            this.level = level;
            this.owner = owner;
            this.target = target;
            this.ticksLeft = ticksLeft;
        }
    }

    private SinBurn()
    {
    }
}
