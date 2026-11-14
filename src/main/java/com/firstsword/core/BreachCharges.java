package com.firstsword.core;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import com.firstsword.FirstSwordItem;

/**
 * 破袭核心的爆裂充能：用初锋左键砍中生物时消耗一层，在目标身上炸一下。
 *
 * 不用原版的 explode()：那个是按威力算距离衰减的，做不到「固定 12 点、3×3 范围」。
 * 这里自己扫一遍范围内的生物逐个结算，顺便把粒子、声音一起补上，
 * 而且永远不会炸到自己和自家宠物。
 */
@EventBusSubscriber(modid = FirstSwordCore.MODID)
public final class BreachCharges
{
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player))
        {
            return;
        }

        if (event.isCanceled())
        {
            return;
        }

        // 只有拿着初锋的那一刀才算
        ItemStack sword = player.getMainHandItem();

        if (!(sword.getItem() instanceof FirstSwordItem))
        {
            return;
        }

        int charges = BreachCore.charges(player);

        if (charges <= 0)
        {
            return;
        }

        BreachCore.setCharges(player, charges - 1);

        ServerLevel level = player.serverLevel();
        Entity target = event.getTarget();

        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5D;
        double z = target.getZ();

        // 延后一拍再炸：先让这一刀正常结算，否则爆炸的击退会在挥砍命中之前把怪推开
        level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1,
                () -> blast(level, player, x, y, z)));

        player.displayClientMessage(
                Component.translatable("message.firstsword_core.breach.charge", charges - 1), true);
    }

    /** 在 (x, y, z) 炸一下：范围内的生物各吃固定的 12 点，自己和自家宠物除外 */
    private static void blast(ServerLevel level, ServerPlayer owner, double x, double y, double z)
    {
        double radius = BreachCore.BLAST_RADIUS;

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);

        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);

        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && !entity.isSpectator() && !isFriendly(owner, entity)))
        {
            if (victim.distanceToSqr(x, y, z) > radius * radius)
            {
                continue;
            }

            // 刚被这一刀砍过，无敌帧还没过；不清掉的话 12 点比刀子低，会被直接吞掉
            victim.invulnerableTime = 0;
            victim.hurt(level.damageSources().playerAttack(owner), BreachCore.BLAST_DAMAGE);
        }
    }

    private static boolean isFriendly(ServerPlayer owner, LivingEntity entity)
    {
        if (entity == owner)
        {
            return true;
        }

        return entity instanceof TamableAnimal pet && pet.isOwnedBy(owner);
    }

    private BreachCharges()
    {
    }
}
