package com.firstsword.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 破袭核心：每个玩家第一次进入下界时获得，只有一次。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class NetherGrant
{
    private static final String KEY = "firstsword_core:breach_granted";

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        if (!Level.NETHER.equals(event.getTo()))
        {
            return;
        }

        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(KEY))
        {
            return;
        }

        data.putBoolean(KEY, true);

        ItemStack core = new ItemStack(FirstSwordCore.BREACH_CORE.get());

        if (!player.addItem(core))
        {
            player.drop(core, false);
        }

        player.displayClientMessage(
                Component.translatable("message.firstsword_core.drop.breach"), false);
        player.playNotifySound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private NetherGrant()
    {
    }
}
