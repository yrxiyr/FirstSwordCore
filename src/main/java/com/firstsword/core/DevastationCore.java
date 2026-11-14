package com.firstsword.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import com.firstsword.SwordCore;

/**
 * 摧灭核心：流星群里多出中型流星，同时整片法阵扩大。
 *
 * 中型流星：5~6 颗，爆炸半径 3 格、36 点伤害，和小流星一样散开，
 * 但排在大流星之前落下。
 *
 * 法阵范围 ×1.5，小流星数量也按面积 ×2.25，保证落点密度不变。
 */
public final class DevastationCore implements SwordCore
{
    public static final DevastationCore INSTANCE = new DevastationCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "devastation_core");

    /** 散布范围放大倍数 */
    public static final double FIELD_SCALE = 1.5D;

    /** 密度不变时数量要按面积放大，也就是倍数的平方 */
    public static final double COUNT_SCALE = FIELD_SCALE * FIELD_SCALE;

    /** 中型流星数量范围 */
    public static final int MEDIUM_MIN = 5;
    public static final int MEDIUM_MAX = 6;

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.devastation");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.devastation.desc");
    }

    @Override
    public double meteorScatterRadius(double base)
    {
        return base * FIELD_SCALE;
    }

    @Override
    public double meteorFieldRadius(double base)
    {
        return base * FIELD_SCALE;
    }

    @Override
    public int smallMeteorCount(int base)
    {
        return Math.round(base * (float) COUNT_SCALE);
    }

    @Override
    public int mediumMeteorCount(RandomSource random)
    {
        return MEDIUM_MIN + random.nextInt(MEDIUM_MAX - MEDIUM_MIN + 1);
    }
}
