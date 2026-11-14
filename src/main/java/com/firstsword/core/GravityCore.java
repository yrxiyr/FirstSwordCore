package com.firstsword.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.firstsword.SwordCore;

/**
 * 牵星核心：大流星落地前，落点先展开一个引力场，把周围的怪拽向圆心。
 *
 * 被引力场拽住的怪，吃大流星的伤害 ×1.4——先聚起来，再一发给大的。
 */
public final class GravityCore implements SwordCore
{
    public static final GravityCore INSTANCE = new GravityCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "gravity_core");

    /** 引力场半径（格） */
    public static final float RADIUS = 8.0F;

    /** 被拽住的目标吃流星的伤害倍率 */
    public static final float DAMAGE_MULTIPLIER = 1.4F;

    /** 现在开着的引力场，用来判定「谁被拽住了」 */
    private static final List<Active> ACTIVE = new ArrayList<>();

    private record Active(ResourceKey<Level> dimension, double x, double y, double z,
                          double radius, long expiresAt)
    {
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.gravity");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.gravity.desc");
    }

    @Override
    public void onBigMeteorIncoming(ServerLevel level, double x, double y, double z, Player owner)
    {
        GravityField.open(level, x, y, z, RADIUS);
        ACTIVE.add(new Active(level.dimension(), x, y, z, RADIUS,
                level.getGameTime() + GravityField.LIFETIME));
    }

    @Override
    public float meteorDamage(float base, LivingEntity target)
    {
        long now = target.level().getGameTime();

        ACTIVE.removeIf(field -> now > field.expiresAt());

        for (Active field : ACTIVE)
        {
            if (!target.level().dimension().equals(field.dimension()))
            {
                continue;
            }

            double dy = target.getY() - field.y();

            if (dy < -6.0D || dy > 6.0D)
            {
                continue;
            }

            double dx = target.getX() - field.x();
            double dz = target.getZ() - field.z();

            if (dx * dx + dz * dz <= field.radius() * field.radius())
            {
                return base * DAMAGE_MULTIPLIER;
            }
        }

        return base;
    }

    /** 服务器停了就把残留的场清掉，免得下次进游戏还留着旧坐标 */
    @EventBusSubscriber(modid = FirstSwordCore.MODID)
    public static final class Cleanup
    {
        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event)
        {
            ACTIVE.clear();
        }

        private Cleanup()
        {
        }
    }

    private GravityCore()
    {
    }
}
