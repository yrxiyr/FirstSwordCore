package com.firstsword.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;

/**
 * 摧灭核心：这个存档里第一次触发不死图腾时获得，每个玩家只有一次。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class TotemDrop
{
    private static final String KEY = "firstsword_core:devastation_dropped";

    @SubscribeEvent
    public static void onUseTotem(LivingUseTotemEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(KEY))
        {
            return;
        }

        data.putBoolean(KEY, true);

        ItemStack core = new ItemStack(FirstSwordCore.DEVASTATION_CORE.get());

        if (!player.addItem(core))
        {
            player.drop(core, false);
        }

        player.displayClientMessage(
                Component.translatable("message.firstsword_core.drop.devastation"), false);
        player.playNotifySound(SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private TotemDrop()
    {
    }
}
