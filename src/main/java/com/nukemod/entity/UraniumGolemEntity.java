package com.nukemod.entity;

import com.nukemod.NukeMod;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UraniumGolemEntity extends IronGolemEntity {

    private static final TrackedData<Integer> SHOOT_COOLDOWN = DataTracker.registerData(UraniumGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public UraniumGolemEntity(EntityType<? extends IronGolemEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 80;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.18)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_ARMOR, 12.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOT_COOLDOWN, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new RangedAttackGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 0.9, false));
        goalSelector.add(3, new WanderAroundFarGoal(this, 0.7));
        goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        goalSelector.add(5, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        int cd = this.dataTracker.get(SHOOT_COOLDOWN);
        if (cd > 0) {
            this.dataTracker.set(SHOOT_COOLDOWN, cd - 1);
        }

        if (this.age % 5 == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.END_ROD,
                    this.getX() + (random.nextDouble() - 0.5) * 1.5,
                    this.getY() + 1.5 + (random.nextDouble() - 0.5) * 1.5,
                    this.getZ() + (random.nextDouble() - 0.5) * 1.5,
                    1, 0, 0, 0, 0.02);
        }

        Box auraBox = this.getBoundingBox().expand(4.0);
        this.getWorld().getEntitiesByClass(LivingEntity.class, auraBox,
                e -> e != this && e.isAlive()).forEach(e -> {
            e.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 80, 1));
        });
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 160, 2));
            living.setOnFireFor(3);
        }
        return hit;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 3, 1, 1, 1, 0);
            sw.spawnParticles(ParticleTypes.INSTANT_EFFECT, getX(), getY() + 1, getZ(), 20, 2, 2, 2, 0);
        }
        super.onDeath(source);
    }

    static class RangedAttackGoal extends Goal {
        private final UraniumGolemEntity golem;
        private int chargeTime = 0;

        RangedAttackGoal(UraniumGolemEntity golem) { this.golem = golem; }

        @Override
        public boolean canStart() {
            if (golem.dataTracker.get(SHOOT_COOLDOWN) > 0) return false;
            LivingEntity target = golem.getTarget();
            return target != null && target.isAlive() && golem.distanceTo(target) > 4.0 && golem.distanceTo(target) < 20.0;
        }

        @Override
        public void start() {
            chargeTime = 20;
            golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target == null) return;
            golem.getLookControl().lookAt(target, 30, 30);

            chargeTime--;
            if (golem.getWorld() instanceof ServerWorld sw && chargeTime % 4 == 0) {
                sw.spawnParticles(ParticleTypes.END_ROD,
                        golem.getX(), golem.getEyeY(), golem.getZ(),
                        3, 0.3, 0.3, 0.3, 0.05);
            }

            if (chargeTime <= 0) {
                if (golem.getWorld() instanceof ServerWorld sw) {
                    var bullet = new BulletEntity(golem.getWorld(), golem, 15.0F, 3.0F, true);
                    Vec3d toTarget = target.getPos().subtract(golem.getPos()).add(0, target.getHeight()/2, 0).normalize();
                    bullet.setPosition(golem.getX(), golem.getEyeY(), golem.getZ());
                    bullet.setVelocity(toTarget.multiply(2.0));
                    bullet.pickupType = net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission.DISALLOWED;
                    sw.spawnEntity(bullet);
                    sw.spawnParticles(ParticleTypes.SONIC_BOOM, golem.getX(), golem.getEyeY(), golem.getZ(), 1, 0, 0, 0, 0);
                }
                golem.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 2.0F, 1.2F);
                golem.dataTracker.set(SHOOT_COOLDOWN, 60);
            }
        }

        @Override
        public boolean shouldContinue() { return chargeTime > 0 && golem.getTarget() != null && golem.getTarget().isAlive(); }
    }
}
