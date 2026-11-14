package com.firstsword.core;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端凑齐「左键 → 右键 → 左键」之后发给服务端的组合技请求。
 *
 * 按键在客户端识别（只有客户端才知道鼠标是不是真的按了），
 * 能不能触发、伤害算多少、消耗几层瞬闪斩，全由服务端自己校验。
 */
public record SonataPayload() implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SonataPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "sonata"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SonataPayload> STREAM_CODEC =
            StreamCodec.unit(new SonataPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
