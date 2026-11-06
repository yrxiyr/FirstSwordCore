package com.chufeng.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.chufeng.SwordCore;

/**
 * 汇聚核心：流星群冷却 -10 秒，彩虹雷每道伤害 +6。
 *
 * id 仍然是 meteor_core——改 id 会让已经打在剑上的核心失效，改名只改了显示名。
 */
public final class MeteorCore implements SwordCore
{
    public static final MeteorCore INSTANCE = new MeteorCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(ChuFengCore.MODID, "meteor_core");

    /** 流星群冷却减免（tick），10 秒 */
    public static final int COOLDOWN_REDUCTION = 200;

    /** 彩虹雷每道伤害加成 */
    public static final float LIGHTNING_BONUS = 6.0F;

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.chufeng_core.meteor");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.chufeng_core.meteor.desc");
    }

    @Override
    public float lightningDamage(float base)
    {
        return base + LIGHTNING_BONUS;
    }

    @Override
    public int meteorCooldown(int base)
    {
        return Math.max(0, base - COOLDOWN_REDUCTION);
    }
}
