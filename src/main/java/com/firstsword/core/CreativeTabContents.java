package com.firstsword.core;

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
@EventBusSubscriber(modid = FirstSwordCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CreativeTabContents
{
    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(FirstSwordCore.SIN.get());
            event.accept(FirstSwordCore.METEOR_CORE.get());
            event.accept(FirstSwordCore.ANNIHILATION_CORE.get());
            event.accept(FirstSwordCore.QINGHUO_CORE.get());
        }
    }
}
