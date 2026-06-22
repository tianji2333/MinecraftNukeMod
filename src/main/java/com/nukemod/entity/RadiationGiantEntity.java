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
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RadiationGiantEntity extends ZombieEntity {

    private static final TrackedData<Integer> CHARGE_TICKS = DataTracker.registerData(RadiationGiantEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public RadiationGiantEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 100;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ARMOR, 8.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CHARGE_TICKS, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new ChargeAttackGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.add(3, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        goalSelector.add(5, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        int charge = this.dataTracker.get(CHARGE_TICKS);
        if (charge > 0) {
            this.dataTracker.set(CHARGE_TICKS, charge - 1);
        }

        // Radiation aura
        Box auraBox = this.getBoundingBox().expand(8.0);
        this.getWorld().getEntitiesByClass(LivingEntity.class, auraBox,
                e -> e != this && e.isAlive()).forEach(e -> {
            e.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 100, 1));
        });

        // Particles
        if (this.age % 10 == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.ENTITY_EFFECT,
                    this.getX() + (random.nextDouble() - 0.5) * 3,
                    this.getY() + 0.1,
                    this.getZ() + (random.nextDouble() - 0.5) * 3,
                    2, 0, 0, 0, 0);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 200, 2));
            living.setOnFireFor(5);
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                    getSoundCategory(), 1.5F, 0.5F);
        }
        return hit;
    }

    @Override
    protected float getSoundVolume() { return 3.0F; }

    static class ChargeAttackGoal extends Goal {
        private final RadiationGiantEntity giant;
        private int cooldown = 0;

        ChargeAttackGoal(RadiationGiantEntity giant) { this.giant = giant; }

        @Override
        public boolean canStart() {
            if (cooldown-- > 0) return false;
            LivingEntity target = giant.getTarget();
            return target != null && target.isAlive() && giant.distanceTo(target) < 6.0;
        }

        @Override
        public void start() {
            giant.dataTracker.set(CHARGE_TICKS, 20);
            giant.getWorld().playSound(null, giant.getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR,
                    giant.getSoundCategory(), 2.0F, 0.5F);
        }

        @Override
        public void tick() {
            if (giant.dataTracker.get(CHARGE_TICKS) == 10) {
                if (giant.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                            giant.getX(), giant.getY(), giant.getZ(), 5, 2, 0.5, 2, 0);
                    Box slamBox = new Box(giant.getBlockPos()).expand(5);
                    sw.getEntitiesByClass(LivingEntity.class, slamBox,
                            e -> e != giant && e.isAlive()).forEach(e -> {
                        double dist = e.squaredDistanceTo(giant);
                        if (dist < 25) {
                            e.damage(giant.getDamageSources().mobAttack(giant), 8.0F);
                            Vec3d knockback = e.getPos().subtract(giant.getPos()).normalize().multiply(2.0);
                            e.setVelocity(knockback.x, 1.0, knockback.z);
                        }
                    });
                }
            }
        }

        @Override
        public boolean shouldContinue() { return giant.dataTracker.get(CHARGE_TICKS) > 0; }

        @Override
        public void stop() { cooldown = 100; }
    }
}
