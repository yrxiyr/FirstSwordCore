package com.firstsword.core;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

/**
 * 牵星核心的引力场：贴地的一圈暗紫色法阵，把范围内的怪持续拽向圆心。
 *
 * 纯服务端做判定（拉拽 + 减速），客户端只负责画环和往内飞的粒子。
 */
public class GravityField extends Entity
{
    /** 存活时间（tick），80 = 4 秒，正好覆盖大流星从出生到落地的全过程 */
    public static final int LIFETIME = 80;

    /** 往圆心拽的速度（格 / tick） */
    private static final double PULL_SPEED = 0.25D;

    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(GravityField.class, EntityDataSerializers.FLOAT);

    private static final Vector3f PURPLE = new Vector3f(0.62F, 0.18F, 0.9F);
    private static final Vector3f CRIMSON = new Vector3f(0.95F, 0.15F, 0.28F);

    public GravityField(EntityType<? extends GravityField> entityType, Level level)
    {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(DATA_RADIUS, 8.0F);
    }

    public float radius()
    {
        return this.entityData.get(DATA_RADIUS);
    }

    public static void open(ServerLevel level, double x, double y, double z, float radius)
    {
        GravityField field = new GravityField(FirstSwordCore.GRAVITY_FIELD.get(), level);
        field.setPos(x, y, z);
        field.entityData.set(DATA_RADIUS, radius);
        level.addFreshEntity(field);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.level().isClientSide())
        {
            this.spawnParticles();
            return;
        }

        if (this.tickCount >= LIFETIME)
        {
            this.discard();
            return;
        }

        this.pull();
    }

    private void pull()
    {
        float radius = this.radius();

        AABB area = new AABB(
                this.getX() - radius, this.getY() - 6.0D, this.getZ() - radius,
                this.getX() + radius, this.getY() + 6.0D, this.getZ() + radius);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area))
        {
            if (!(target instanceof Enemy) || !target.isAlive())
            {
                continue;
            }

            double dx = this.getX() - target.getX();
            double dz = this.getZ() - target.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > radius || distance < 0.6D)
            {
                continue;
            }

            double dirX = dx / distance;
            double dirZ = dz / distance;

            Vec3 motion = target.getDeltaMovement();

            // 只保证「朝圆心的速度」不低于 PULL_SPEED，不做无上限的加速，
            // 否则怪会被越拽越快、直接从圆心甩过去
            double toward = -(motion.x * dirX + motion.z * dirZ);

            if (toward < PULL_SPEED)
            {
                double add = PULL_SPEED - toward;
                target.setDeltaMovement(motion.x + dirX * add, motion.y, motion.z + dirZ * add);
                target.hurtMarked = true;
            }

            // 每半秒刷新一次缓慢 II（50%），环境效果，不显示图标也不冒粒子
            if (target.tickCount % 10 == 0)
            {
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 12, 1, true, false, false));
            }
        }
    }

    private void spawnParticles()
    {
        float radius = this.radius();

        for (int i = 0; i < 3; i++)
        {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = radius * (0.75D + this.random.nextDouble() * 0.25D);

            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;

            Vector3f color = this.random.nextBoolean() ? PURPLE : CRIMSON;

            // 速度朝圆心：粒子从外圈往里收
            this.level().addParticle(
                    new DustParticleOptions(color, 2.2F),
                    x, this.getY() + 0.3D + this.random.nextDouble() * 0.6D, z,
                    -Math.cos(angle) * 0.32D, 0.02D, -Math.sin(angle) * 0.32D);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr)
    {
        return distanceSqr < 160.0D * 160.0D;
    }

    @Override
    public AABB getBoundingBoxForCulling()
    {
        // 实体本体是 0 尺寸，不放大就会被视锥直接剔掉
        return super.getBoundingBoxForCulling().inflate(this.radius()).inflate(0.5D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
    }
}
