package com.firstsword.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
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

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    /** 牵星核心的引力场，纯视觉 + 拉拽判定 */
    public static final DeferredHolder<EntityType<?>, EntityType<GravityField>> GRAVITY_FIELD =
            ENTITY_TYPES.register("gravity_field", () -> EntityType.Builder
                    .of(GravityField::new, MobCategory.MISC)
                    .noSave()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(16)
                    .updateInterval(20)
                    .build("gravity_field"));



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

    /** 摧灭核心：第一次触发不死图腾时获得 */
    public static final DeferredItem<CoreItem> DEVASTATION_CORE = ITEMS.registerItem(
            "devastation_core",
            props -> new CoreItem(props.stacksTo(16), DevastationCore.INSTANCE));

    /** 破袭核心：第一次进入下界时获得 */
    public static final DeferredItem<CoreItem> BREACH_CORE = ITEMS.registerItem(
            "breach_core",
            props -> new CoreItem(props.stacksTo(16), BreachCore.INSTANCE));

    /** 牵星核心：末影珍珠 + 恶魂之泪 + 下界之星合成 */
    public static final DeferredItem<CoreItem> GRAVITY_CORE = ITEMS.registerItem(
            "gravity_core",
            props -> new CoreItem(props.stacksTo(16), GravityCore.INSTANCE));

    /** 回响核心：第一次用瞬闪斩一刀刮到 3 个目标时获得 */
    public static final DeferredItem<CoreItem> ECHO_CORE = ITEMS.registerItem(
            "echo_core",
            props -> new CoreItem(props.stacksTo(16), EchoCore.INSTANCE));

    /** 奏鸣核心：组合技（左键→右键→左键） */
    public static final DeferredItem<CoreItem> SONATA_CORE = ITEMS.registerItem(
            "sonata_core",
            props -> new CoreItem(props.stacksTo(16), SonataCore.INSTANCE));

    /** 森罗核心：九种矿物合成，彩雷穿墙 + 维度无关 + 半径 ×1.4 */
    public static final DeferredItem<CoreItem> SENRA_CORE = ITEMS.registerItem(
            "senra_core",
            props -> new CoreItem(props.stacksTo(16), SenraCore.INSTANCE));

    public FirstSwordCore(IEventBus modEventBus, ModContainer modContainer)
    {
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        // 词条要在剑读取之前注册好，构造期就够了
        SwordCores.register(MeteorCore.INSTANCE);
        SwordCores.register(AnnihilationCore.INSTANCE);
        SwordCores.register(QinghuoCore.INSTANCE);
        SwordCores.register(DevastationCore.INSTANCE);
        SwordCores.register(BreachCore.INSTANCE);
        SwordCores.register(GravityCore.INSTANCE);
        SwordCores.register(EchoCore.INSTANCE);
        SwordCores.register(SonataCore.INSTANCE);
        SwordCores.register(SenraCore.INSTANCE);
    }
}
