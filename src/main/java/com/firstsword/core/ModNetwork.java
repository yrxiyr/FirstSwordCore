package com.firstsword.core;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.firstsword.BlinkStrike;
import com.firstsword.FirstSwordItem;
import com.firstsword.SwordCores;

/** 附属模组自己的网络：目前只有奏鸣核心的组合技请求 */
@EventBusSubscriber(modid = FirstSwordCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork
{
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        event.registrar("1").playToServer(
                SonataPayload.TYPE,
                SonataPayload.STREAM_CODEC,
                ModNetwork::handleSonata);
    }

    private static void handleSonata(SonataPayload payload, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            if (!(context.player() instanceof ServerPlayer player))
            {
                return;
            }

            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();

            ItemStack sword = main.getItem() instanceof FirstSwordItem ? main
                    : off.getItem() instanceof FirstSwordItem ? off
                    : ItemStack.EMPTY;

            // 服务端校验：剑上得有奏鸣核心，手上得拿着初锋
            if (sword.isEmpty() || !SwordCores.has(sword, SonataCore.ID))
            {
                return;
            }

            int max = FirstSwordItem.blinkCharges(sword);
            int remaining = BlinkStrike.getRemainingCooldown(
                    player, player.serverLevel().getGameTime(), max);

            if (remaining > 0)
            {
                player.displayClientMessage(Component.translatable(
                        "message.firstsword.blink_cooldown",
                        Mth.ceil(remaining / 20.0F)), true);
                return;
            }

            // 组合技是「瞬闪斩 + 周身彩雷」，所以要像正常瞬闪斩一样消耗一层
            if (!BlinkStrike.cast(player.serverLevel(), player, sword))
            {
                return;
            }

            // 冲刺方向：和瞬闪斩一致，只取水平分量
            Vec3 look = player.getLookAngle();
            Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

            if (horizontal.lengthSqr() < 1.0E-6D)
            {
                horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
            }

            // 三波彩雷，沿冲刺方向铺满 15×7×7 的长方体
            SonataWaves.cast(player.serverLevel(), player, sword, horizontal.normalize());

            // 组合技额外附赠跳跃提升 II + 生命恢复 II
            SonataCore.applyComboBonus(player);
        });
    }

    private ModNetwork()
    {
    }
}
