package com.firstsword.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import com.firstsword.SwordCore;

/**
 * 破袭核心：流星键不带任何蓄力时，不召唤流星群，改成给自己上状态。
 *
 * 抗性提升 II + 力量 II 各 12 秒，同时把「爆裂」充能补满到 3 层。
 * 之后每次用初锋左键砍中生物消耗一层，在目标身上炸一次（不破坏地形）。
 *
 * 点按和流星群共用同一把剑，但走各自的冷却：点按 12 秒，流星群还是原来的 32 秒。
 */
public final class BreachCore implements SwordCore
{
    public static final BreachCore INSTANCE = new BreachCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "breach_core");

    /** buff 持续时间：12 秒 */
    public static final int BUFF_TICKS = 240;

    /** 抗性提升 II、力量 II，amplifier 1 就是 II 级 */
    public static final int BUFF_AMPLIFIER = 1;

    /** 爆裂最多存 3 层 */
    public static final int MAX_CHARGES = 3;

    /** 点按的独立冷却，和 buff 一样长，正好接上 */
    public static final int TAP_COOLDOWN = 240;

    /** 爆裂斩的作用半径：1.6 格，大约是 3×3 的范围 */
    public static final double BLAST_RADIUS = 1.6D;

    /** 爆裂斩固定伤害，不吃距离衰减，范围内的怪各吃这么多 */
    public static final float BLAST_DAMAGE = 12.0F;

    static final String KEY_CHARGES = "firstsword_core:breach_charges";

    /** 点按冷却，按玩家记。这是跨存档的运行时状态，下线重进就清了，够用 */
    private static final Map<UUID, Long> TAP_READY = new HashMap<>();

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.breach");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.breach.desc");
    }

    @Override
    public boolean onMeteorTap(ServerLevel level, Player player, ItemStack sword)
    {
        if (!(player instanceof ServerPlayer serverPlayer))
        {
            return false;
        }

        long now = level.getGameTime();
        Long ready = TAP_READY.get(serverPlayer.getUUID());

        // 接了这次点按，但还在冷却里：既不召唤流星，也不重复给 buff
        if (ready != null && now < ready)
        {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.firstsword_core.breach.cooldown"), true);

            return true;
        }

        TAP_READY.put(serverPlayer.getUUID(), now + TAP_COOLDOWN);

        serverPlayer.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, BUFF_TICKS, BUFF_AMPLIFIER, false, true, true));
        serverPlayer.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, BUFF_TICKS, BUFF_AMPLIFIER, false, true, true));

        serverPlayer.getPersistentData().putInt(KEY_CHARGES, MAX_CHARGES);

        serverPlayer.displayClientMessage(
                Component.translatable("message.firstsword_core.breach.tap"), true);
        serverPlayer.playNotifySound(SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.0F);

        return true;
    }

    /** 当前存了几层爆裂 */
    public static int charges(Player player)
    {
        return Math.max(0, Math.min(MAX_CHARGES, player.getPersistentData().getInt(KEY_CHARGES)));
    }

    static void setCharges(Player player, int value)
    {
        CompoundTag data = player.getPersistentData();
        data.putInt(KEY_CHARGES, Math.max(0, Math.min(MAX_CHARGES, value)));
    }

    @EventBusSubscriber(modid = FirstSwordCore.MODID)
    public static final class Cleanup
    {
        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event)
        {
            TAP_READY.clear();
        }

        private Cleanup()
        {
        }
    }

    private BreachCore()
    {
    }
}
