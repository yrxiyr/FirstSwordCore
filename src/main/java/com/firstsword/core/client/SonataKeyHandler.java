package com.firstsword.core.client;

import com.firstsword.FirstSwordItem;
import com.firstsword.core.FirstSwordCore;
import com.firstsword.core.SonataPayload;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 奏鸣核心的组合技输入：左键 → 右键 → 左键。
 *
 * 只在客户端识别——服务端的左键事件只有「真的砍到东西」才会发，
 * 对着空气挥剑也要能凑出连招，所以必须读原始按键状态。
 * 三个步骤之间最多隔 {@link #WINDOW_TICKS} tick，超时就从头开始。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID, value = Dist.CLIENT)
public class SonataKeyHandler
{
    /** 每一步之间允许的最大间隔（tick），10 tick = 0.5 秒 */
    private static final int WINDOW_TICKS = 10;

    private static boolean leftDown;
    private static boolean rightDown;

    /** 0 = 还没开始，1 = 已经按了左，2 = 已经按了左 + 右 */
    private static int stage;

    private static int ticksLeft;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft minecraft = Minecraft.getInstance();

        boolean left = minecraft.options.keyAttack.isDown();
        boolean right = minecraft.options.keyUse.isDown();

        boolean leftPress = left && !leftDown;
        boolean rightPress = right && !rightDown;

        leftDown = left;
        rightDown = right;

        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null)
        {
            stage = 0;
            return;
        }

        if (ticksLeft > 0 && --ticksLeft == 0)
        {
            stage = 0;
        }

        boolean holdingSword = minecraft.player.getMainHandItem().getItem() instanceof FirstSwordItem
                || minecraft.player.getOffhandItem().getItem() instanceof FirstSwordItem;

        if (!holdingSword)
        {
            stage = 0;
            return;
        }

        if (leftPress)
        {
            if (stage == 2)
            {
                // 左 → 右 → 左，凑齐
                PacketDistributor.sendToServer(new SonataPayload());
                stage = 0;
            }
            else
            {
                stage = 1;
                ticksLeft = WINDOW_TICKS;
            }
        }
        else if (rightPress)
        {
            if (stage == 1)
            {
                stage = 2;
                ticksLeft = WINDOW_TICKS;
            }
            else
            {
                stage = 0;
            }
        }
    }

    private SonataKeyHandler()
    {
    }
}
