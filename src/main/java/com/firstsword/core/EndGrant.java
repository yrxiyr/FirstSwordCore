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
 * 奏鸣核心：每个玩家第一次进入末地时获得，只有一次。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class EndGrant
{
    private static final String KEY = "firstsword_core:sonata_granted";

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        if (!Level.END.equals(event.getTo()))
        {
            return;
        }

        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(KEY))
        {
            return;
        }

        data.putBoolean(KEY, true);

        ItemStack core = new ItemStack(FirstSwordCore.SONATA_CORE.get());

        if (!player.addItem(core))
        {
            player.drop(core, false);
        }

        player.displayClientMessage(
                Component.translatable("message.firstsword_core.drop.sonata"), false);
        player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private EndGrant()
    {
    }
}
