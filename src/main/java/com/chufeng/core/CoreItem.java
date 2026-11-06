package com.chufeng.core;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.chufeng.FirstSwordItem;
import com.chufeng.SwordCore;
import com.chufeng.SwordCores;

/**
 * 通用核心物品。
 *
 * 副手拿着初锋、主手拿着它右键，就把构造时指定的词条打到剑上。
 * 附魔和自定义名字都不会动，只是给剑加一个组件（外加词条自己想做的一次性改动）。
 */
public class CoreItem extends Item
{
    private final SwordCore core;

    public CoreItem(Properties properties, SwordCore core)
    {
        super(properties);
        this.core = core;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack coreStack = player.getItemInHand(hand);

        InteractionHand other = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;

        ItemStack sword = player.getItemInHand(other);

        if (!(sword.getItem() instanceof FirstSwordItem))
        {
            if (!level.isClientSide())
            {
                player.displayClientMessage(
                        Component.translatable("message.chufeng_core.need_sword"), true);
            }

            return InteractionResultHolder.fail(coreStack);
        }

        if (!level.isClientSide())
        {
            if (SwordCores.attach(sword, this.core.id()))
            {
                if (!player.getAbilities().instabuild)
                {
                    coreStack.shrink(1);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

                player.displayClientMessage(Component.translatable(
                        "message.chufeng_core.applied", this.core.displayName()), false);
            }
            else
            {
                player.displayClientMessage(
                        Component.translatable("message.chufeng_core.already"), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(coreStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(this.core.description().copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.chufeng_core.core.howto")
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
