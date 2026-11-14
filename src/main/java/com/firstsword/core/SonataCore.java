package com.firstsword.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import com.firstsword.SwordCore;

/**
 * 奏鸣核心：给初锋加一个组合技。
 *
 * 快速连按「左键 → 右键 → 左键」，在瞬闪斩的基础上多出三波沿冲刺方向的横向彩虹落雷。
 * 触发逻辑在 {@link SonataWaves}（服务端）和 {@code client.SonataKeyHandler}（客户端输入）。
 *
 * 另外瞬闪斩和组合技都会给自身加速 buff，组合技再多两条。
 */
public final class SonataCore implements SwordCore
{
    public static final SonataCore INSTANCE = new SonataCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "sonata_core");

    /** buff 持续时间（tick），8 秒 */
    private static final int BUFF_TICKS = 160;

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.sonata");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.sonata.desc");
    }

    /** 瞬闪斩 / 组合技共用：速度 II + 急迫 II */
    @Override
    public void onBlinkCast(ServerPlayer player, ItemStack sword)
    {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, BUFF_TICKS, 1));
    }

    /** 组合技额外：跳跃提升 II + 生命恢复 II（在瞬闪斩的两条之外） */
    public static void applyComboBonus(ServerPlayer player)
    {
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, BUFF_TICKS, 1));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_TICKS, 1));
    }

    private SonataCore()
    {
    }
}
