package com.firstsword.core;

import net.minecraft.nbt.CompoundTag;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 玩家身上的一次性标记（罪 / 汇聚 / 摧灭 / 破袭）。
 *
 * 这些标记存在玩家的持久化数据里，死亡重生时不一定能带过去——
 * 带不过去就会出现「同一个存档死了之后又能拿一次核心」。
 * 所以在克隆事件里新旧玩家之间自己抄一遍，和平台自己的复制互不冲突。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class PlayerFlags
{
    private static final String[] KEYS = {
            "firstsword_core:sin_dropped",
            "firstsword_core:convergence_dropped",
            "firstsword_core:devastation_dropped",
            "firstsword_core:breach_granted",
            "firstsword_core:breach_charges",
            "firstsword_core:echo_granted",
            "firstsword_core:sonata_granted"
    };

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event)
    {
        CompoundTag from = event.getOriginal().getPersistentData();
        CompoundTag to = event.getEntity().getPersistentData();

        for (String key : KEYS)
        {
            if (from.contains(key))
            {
                to.put(key, from.get(key).copy());
            }
        }
    }

    private PlayerFlags()
    {
    }
}
