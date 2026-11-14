package com.firstsword.core.client;

import com.firstsword.core.FirstSwordCore;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 客户端渲染器注册 */
@EventBusSubscriber(modid = FirstSwordCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup
{
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(FirstSwordCore.GRAVITY_FIELD.get(), GravityFieldRenderer::new);
    }
}
