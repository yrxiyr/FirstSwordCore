package com.firstsword.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.firstsword.SwordCore;

/**
 * 森罗核心：彩虹雷不再受地形限制。
 *
 * <ul>
 *   <li>无视方块：不再贴地表生成，而是直接落在玩家当前高度——下界、洞穴、室内都能落雷</li>
 *   <li>落点半径 ×1.4（30 → 42 格）</li>
 * </ul>
 *
 * 只影响右键的彩虹雷，流星群和瞬闪斩不变。
 */
public final class SenraCore implements SwordCore
{
    public static final SenraCore INSTANCE = new SenraCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "senra_core");

    /** 落点半径倍数 */
    public static final float RADIUS_MULTIPLIER = 1.4F;

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.senra");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.senra.desc");
    }

    @Override
    public float lightningRadius(float base)
    {
        return base * RADIUS_MULTIPLIER;
    }

    @Override
    public boolean lightningThroughBlocks()
    {
        return true;
    }

    private SenraCore()
    {
    }
}
