package com.firstsword.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.firstsword.BlinkHitEvent;

/**
 * 回响核心：这个存档里第一次用瞬闪斩一刀刮到 3 个及以上目标时获得，每个玩家一次。
 *
 * 注意用的是主模组公开的 {@link BlinkHitEvent}，不是 SwordCore 的钩子——
 * 想拿词条的时候剑上当然还没有这个词条。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class EchoUnlock
{
    private static final String KEY = "firstsword_core:echo_granted";

    /** 一刀至少刮到几个 */
    private static final int NEEDED = 3;

    /** 这些命中算「同一刀」的时间窗口（tick） */
    private static final long WINDOW_TICKS = 6L;

    /** {本轮开始 tick, 本轮命中数} */
    private static final Map<UUID, long[]> WINDOWS = new HashMap<>();

    @SubscribeEvent
    public static void onBlinkHit(BlinkHitEvent event)
    {
        ServerPlayer player = event.player();

        if (player.getPersistentData().getBoolean(KEY))
        {
            return;
        }

        long now = event.level().getGameTime();
        long[] window = WINDOWS.computeIfAbsent(player.getUUID(), uuid -> new long[]{-1000L, 0L});

        if (now - window[0] > WINDOW_TICKS)
        {
            window[0] = now;
            window[1] = 0L;
        }

        window[1]++;

        if (window[1] < NEEDED)
        {
            return;
        }

        player.getPersistentData().putBoolean(KEY, true);
        WINDOWS.remove(player.getUUID());

        ItemStack core = new ItemStack(FirstSwordCore.ECHO_CORE.get());

        if (!player.addItem(core))
        {
            player.drop(core, false);
        }

        player.displayClientMessage(
                Component.translatable("message.firstsword_core.drop.echo"), false);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        WINDOWS.clear();
    }

    private EchoUnlock()
    {
    }
}
