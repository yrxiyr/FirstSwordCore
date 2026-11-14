package com.firstsword.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import com.firstsword.FirstSwordItem;
import com.firstsword.RainbowLightning;

/**
 * 奏鸣核心的彩雷：分三批释放。
 *
 * 冲刺本身只有 2 tick，一批雷 0.4 秒就没了，玩家反应过来时早就散干净了。
 * 所以拆成三波、每波间隔 6 tick，并且每一波都以**玩家当时的位置**为中心重新铺一遍，
 * 保证不管冲刺后有没有转身，眼前都有雷。
 *
 * 三波共用一份去重集合：同一只怪整轮只吃一次雷伤，多出来的只是视觉。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class SonataWaves
{
    /** 一共几波 */
    private static final int WAVES = 3;

    /** 波与波之间隔多少 tick */
    private static final int INTERVAL = 6;

    /** 每波铺几道 */
    private static final int BOLTS_PER_WAVE = 20;

    /** 长方体尺寸：沿冲刺方向 15 长、7 宽、7 高 */
    private static final double BOX_LENGTH = 15.0D;
    private static final double BOX_WIDTH = 7.0D;
    private static final double BOX_HEIGHT = 7.0D;

    private static final List<Wave> PENDING = new ArrayList<>();

    public static void cast(ServerLevel level, ServerPlayer player, ItemStack sword, Vec3 dir)
    {
        Set<Entity> damaged = new HashSet<>();
        float damage = FirstSwordItem.lightningDamage(sword);

        spawnWave(level, player, dir, damage, damaged);

        for (int i = 1; i < WAVES; i++)
        {
            PENDING.add(new Wave(level, player, dir, damage, damaged,
                    level.getGameTime() + (long) i * INTERVAL));
        }
    }

    /** 以玩家当前位置为中心，在沿 dir 的长方体里随机铺一批横向彩雷 */
    private static void spawnWave(ServerLevel level, ServerPlayer player, Vec3 dir,
                                  float damage, Set<Entity> damaged)
    {
        RandomSource random = level.random;

        double halfLength = BOX_LENGTH * 0.5D;
        double halfWidth = BOX_WIDTH * 0.5D;

        Vec3 center = player.position();
        double baseY = player.getY() + BOX_HEIGHT * 0.5D - 1.0D;

        for (int i = 0; i < BOLTS_PER_WAVE; i++)
        {
            double along = (random.nextDouble() * 2.0D - 1.0D) * halfLength;
            double lateral = (random.nextDouble() * 2.0D - 1.0D) * halfWidth;
            double height = (random.nextDouble() * 2.0D - 1.0D) * (BOX_HEIGHT * 0.5D);

            double x = center.x + dir.x * along - dir.z * lateral;
            double z = center.z + dir.z * along + dir.x * lateral;
            double y = baseY + height;

            // 每道雷自己的朝向也随机，横七竖八才像炸开
            double boltAngle = random.nextDouble() * Math.PI * 2.0D;

            RainbowLightning.spawnHorizontalBolt(
                    level, player, x, y, z,
                    (float) Math.cos(boltAngle), (float) Math.sin(boltAngle),
                    damage, damaged);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        if (PENDING.isEmpty())
        {
            return;
        }

        List<Wave> due = new ArrayList<>();

        Iterator<Wave> iterator = PENDING.iterator();

        while (iterator.hasNext())
        {
            Wave wave = iterator.next();

            if (wave.level.getGameTime() >= wave.at)
            {
                iterator.remove();
                due.add(wave);
            }
        }

        for (Wave wave : due)
        {
            if (!wave.player.isAlive() || wave.player.hasDisconnected())
            {
                continue;
            }

            spawnWave(wave.level, wave.player, wave.dir, wave.damage, wave.damaged);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        PENDING.clear();
    }

    private static final class Wave
    {
        final ServerLevel level;
        final ServerPlayer player;
        final Vec3 dir;
        final float damage;
        final Set<Entity> damaged;
        final long at;

        Wave(ServerLevel level, ServerPlayer player, Vec3 dir, float damage,
             Set<Entity> damaged, long at)
        {
            this.level = level;
            this.player = player;
            this.dir = dir;
            this.damage = damage;
            this.damaged = damaged;
            this.at = at;
        }
    }

    private SonataWaves()
    {
    }
}
