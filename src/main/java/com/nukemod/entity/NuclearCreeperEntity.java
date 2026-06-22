package com.nukemod.entity;

import com.nukemod.explosion.NuclearExplosion;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class NuclearCreeperEntity extends HostileEntity {

    private int fuseTime = 0;
    private static final int MAX_FUSE = 30;

    public NuclearCreeperEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 60;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new CreeperFuseGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.add(3, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        goalSelector.add(5, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // Radiation aura
        if (!this.getWorld().isClient && this.age % 40 == 0 && this.isAlive()) {
            this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(6),
                    e -> e != this && e.isAlive()).forEach(e -> {
                e.addStatusEffect(new StatusEffectInstance(com.nukemod.NukeMod.RADIATION, 60, 0));
            });
        }

        // Fuse logic
        if (fuseTime > 0) {
            fuseTime++;
            if (fuseTime >= MAX_FUSE) {
                doNuclearExplosion();
            }
        }
    }

    public void ignite() {
        this.fuseTime = 1;
        this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
    }

    private void doNuclearExplosion() {
        if (!this.getWorld().isClient) {
            ServerWorld sw = (ServerWorld) this.getWorld();
            sw.createExplosion(this, this.getX(), this.getY(), this.getZ(),
                    2.0F, World.ExplosionSourceType.MOB);
            NuclearExplosion nuke = new NuclearExplosion(sw,
                    this.getX(), this.getY(), this.getZ(), 30.0F);
            nuke.explode();
        }
        this.discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("fuse", (short) fuseTime);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("fuse")) fuseTime = nbt.getShort("fuse");
    }

    private static class CreeperFuseGoal extends Goal {
        private final NuclearCreeperEntity creeper;
        private LivingEntity target;

        CreeperFuseGoal(NuclearCreeperEntity creeper) { this.creeper = creeper; }

        @Override
        public boolean canStart() {
            target = creeper.getTarget();
            return target != null && creeper.distanceTo(target) < 3.0;
        }

        @Override
        public void start() { creeper.ignite(); }

        @Override
        public void stop() { creeper.fuseTime = 0; }

        @Override
        public boolean shouldContinue() { return target != null && target.isAlive() && creeper.distanceTo(target) < 5.0; }
    }
}
