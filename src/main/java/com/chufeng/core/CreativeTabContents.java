package com.chufeng.core;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * 把核心放进创造模式的「材料」页。
 *
 * 物品注册了不等于会出现在创造栏里，必须显式往某个标签页里塞。
 * 放在原版「材料」页而不是自建标签页，是为了整合包作者找起来方便。
 */
@EventBusSubscriber(modid = ChuFengCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CreativeTabContents
{
    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ChuFengCore.SIN.get());
            event.accept(ChuFengCore.METEOR_CORE.get());
            event.accept(ChuFengCore.ANNIHILATION_CORE.get());
            event.accept(ChuFengCore.QINGHUO_CORE.get());
        }
    }
}
