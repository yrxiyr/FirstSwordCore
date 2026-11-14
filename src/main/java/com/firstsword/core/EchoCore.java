package com.firstsword.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.firstsword.SwordCore;

/**
 * 回响核心：瞬闪斩可以存 3 次（冷却时长不变），并且每一刀都会回血。
 *
 * 每次冲刺最多回 6 点——按「同一次冲刺」算，不是按单只怪无限叠。
 */
public final class EchoCore implements SwordCore
{
    public static final EchoCore INSTANCE = new EchoCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "echo_core");

    /** 瞬闪斩最多存几次 */
    public static final int CHARGES = 3;

    /** 每命中一只怪回多少血 */
    public static final float HEAL_PER_HIT = 2.0F;

    /** 一次冲刺最多回多少血 */
    public static final float HEAL_CAP = 6.0F;

    /** 超过这么多 tick 没再命中，就算新的一次冲刺 */
    private static final long WINDOW_TICKS = 10L;

    /** 每个玩家当前这次冲刺已经回了多少血 */
    private static final Map<UUID, Window> WINDOWS = new HashMap<>();

    private static final class Window
    {
        long startTick;
        float healed;
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.echo");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.echo.desc");
    }

    @Override
    public int blinkCharges(int base)
    {
        return Math.max(base, CHARGES);
    }

    @Override
    public void onBlinkHit(ServerLevel level, Player owner, LivingEntity target)
    {
        long now = level.getGameTime();
        Window window = WINDOWS.get(owner.getUUID());

        if (window == null || now - window.startTick > WINDOW_TICKS)
        {
            window = new Window();
            window.startTick = now;
            WINDOWS.put(owner.getUUID(), window);
        }

        if (window.healed >= HEAL_CAP)
        {
            return;
        }

        float heal = Math.min(HEAL_PER_HIT, HEAL_CAP - window.healed);

        owner.heal(heal);
        window.healed += heal;

        level.sendParticles(ParticleTypes.HEART,
                owner.getX(), owner.getY() + owner.getBbHeight() * 0.6D, owner.getZ(),
                (int) heal, 0.3D, 0.3D, 0.3D, 0.0D);
    }

    @EventBusSubscriber(modid = FirstSwordCore.MODID)
    public static final class Cleanup
    {
        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event)
        {
            WINDOWS.clear();
        }

        private Cleanup()
        {
        }
    }

    private EchoCore()
    {
    }
}
