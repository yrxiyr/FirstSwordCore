package com.firstsword.core;

import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.firstsword.SwordCores;

/**
 * 更强的初锋。
 *
 * 主模组本身不改数值，强化全部由这里的「核心」词条提供——
 * 不装这个模组，初锋的平衡和 1.6.0 完全一致。
 */
@Mod(FirstSwordCore.MODID)
public class FirstSwordCore
{
    public static final String MODID = "firstsword_core";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    /** 罪：任意生物因玩家死亡时掉落，做核心的材料 */
    public static final DeferredItem<Item> SIN = ITEMS.registerItem(
            "sin",
            props -> new Item(props));

    /** 汇聚核心：用初锋累计打出 2500 点伤害后获得 */
    public static final DeferredItem<CoreItem> METEOR_CORE = ITEMS.registerItem(
            "meteor_core",
            props -> new CoreItem(props.stacksTo(16), MeteorCore.INSTANCE));

    /** 毁杀核心：下界之星 + 3 钻石 + 3 铁锭合成 */
    public static final DeferredItem<CoreItem> ANNIHILATION_CORE = ITEMS.registerItem(
            "annihilation_core",
            props -> new CoreItem(props.stacksTo(16), AnnihilationCore.INSTANCE));

    /** 顷火核心：罪 + 恶魂之泪 + 黑曜石合成 */
    public static final DeferredItem<CoreItem> QINGHUO_CORE = ITEMS.registerItem(
            "qinghuo_core",
            props -> new CoreItem(props.stacksTo(16), QinghuoCore.INSTANCE));

    public FirstSwordCore(IEventBus modEventBus, ModContainer modContainer)
    {
        ITEMS.register(modEventBus);

        // 词条要在剑读取之前注册好，构造期就够了
        SwordCores.register(MeteorCore.INSTANCE);
        SwordCores.register(AnnihilationCore.INSTANCE);
        SwordCores.register(QinghuoCore.INSTANCE);
    }
}
