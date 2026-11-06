package com.firstsword.core;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import com.firstsword.SwordCore;

/**
 * 毁杀核心：攻击力 +4，瞬闪斩伤害 +6（路径和落点都加）。
 *
 * 攻击力走的是物品的属性组件，所以要在 {@link #onAttach} 里往剑上挂一个带唯一 id 的修饰符——
 * 组件是跟着物品存档走的，挂一次就永久生效。
 */
public final class AnnihilationCore implements SwordCore
{
    public static final AnnihilationCore INSTANCE = new AnnihilationCore();

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "annihilation_core");

    /** 攻击力加成 */
    public static final float DAMAGE_BONUS = 4.0F;

    /** 瞬闪斩伤害加成 */
    public static final float BLINK_BONUS = 6.0F;

    /** 修饰符 id，必须唯一，否则会和别的词条互相覆盖 */
    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(FirstSwordCore.MODID, "core.annihilation.damage");

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    @Override
    public Component displayName()
    {
        return Component.translatable("core.firstsword_core.annihilation");
    }

    @Override
    public Component description()
    {
        return Component.translatable("core.firstsword_core.annihilation.desc");
    }

    @Override
    public void onAttach(ItemStack stack)
    {
        ItemAttributeModifiers modifiers = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers updated = modifiers.withModifierAdded(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(MODIFIER_ID, DAMAGE_BONUS,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, updated);
    }

    @Override
    public float blinkDamage(float base)
    {
        return base + BLINK_BONUS;
    }
}
