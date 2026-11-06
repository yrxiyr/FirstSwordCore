package com.chufeng.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.chufeng.SwordCore;

/**
 * 顷火核心：被流星打中的怪会烧起来（每秒 8 点，7 秒），
 * 同时给玩家 12 秒抗火和 12 秒伤害吸收（8 点 = 4 颗金心）。
 */
public final class QinghuoCore implements SwordCore
{
    public static final QinghuoCore INSTANCE = new QinghuoCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ChuFengCore.MODID, "qinghuo_core");

    /** 抗火 / 伤害吸收的持续时间（tick），12 秒 */
    public static final int BUFF_DURATION = 240;

    /** 伤害吸收等级：1 = 8 点（4 颗金心），0 只有 4 点 */
    public static final int ABSORPTION_AMPLIFIER = 1;

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.chufeng_core.qinghuo");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.chufeng_core.qinghuo.desc");
    }

    @Override
    public void onMeteorHit(ServerLevel level, Player owner, LivingEntity target)
    {
        SinBurn.apply(level, owner, target);

        owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BUFF_DURATION, 0));
        owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, BUFF_DURATION, ABSORPTION_AMPLIFIER));
    }
}
