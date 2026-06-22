package com.nukemod.entity;

import com.nukemod.NukeMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BulletEntity extends Entity {

    private float damage = 5.0F;
    private float radiationLevel = 0.0F;
    private boolean gaussRound = false;
    private int life = 0;

    public BulletEntity(EntityType<? extends BulletEntity> entityType, World world) {
        super(entityType, world);
    }

    public BulletEntity(World world, LivingEntity owner, float damage, float radiation, boolean gauss) {
        super(ModEntities.BULLET, world);
        this.damage = damage;
        this.radiationLevel = radiation;
        this.gaussRound = gauss;
        this.setNoGravity(gauss);
    }

    public void setVelocityFromAngles(float pitch, float yaw, float speed) {
        float radPitch = pitch * 0.017453292F;
        float radYaw = yaw * 0.017453292F;
        double vx = -Math.sin(radYaw) * Math.cos(radPitch) * speed;
        double vy = -Math.sin(radPitch) * speed;
        double vz = Math.cos(radYaw) * Math.cos(radPitch) * speed;
        this.setVelocity(vx, vy, vz);
        this.velocityDirty = true;
    }

    @Override
    public void tick() {
        super.tick();
        life++;

        // Remove after 10 seconds
        if (life > 200) {
            this.discard();
            return;
        }

        if (!hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0, -0.03, 0));
        }

        // Movement
        this.move(MovementType.SELF, this.getVelocity());

        // Friction
        this.setVelocity(this.getVelocity().multiply(gaussRound ? 1.0 : 0.99));

        // Client particles
        if (this.getWorld().isClient && life % 2 == 0) {
            int color = gaussRound ? 0x00AAFF : (radiationLevel > 0 ? 0x55FF55 : 0xFFAA00);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g2 = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT,
                    getX(), getY(), getZ(), r, g2, b);
        }

        // Hit detection
        if (!this.getWorld().isClient) {
            BlockHitResult blockHit = this.getWorld().raycast(new RaycastContext(
                    this.getPos(), this.getPos().add(this.getVelocity()),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, this));
            
            if (blockHit.getType() != HitResult.Type.MISS) {
                onHit(blockHit);
                return;
            }

            // Entity collision
            Box box = this.getBoundingBox().expand(0.3);
            this.getWorld().getEntitiesByClass(LivingEntity.class, box,
                    e -> e.isAlive() && !e.isSpectator() && e.squaredDistanceTo(this) > 0.01).forEach(this::onHitEntity);
        }
    }

    private void onHit(BlockHitResult hit) {
        if (gaussRound && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.SONIC_BOOM, getX(), getY(), getZ(), 1, 0.2, 0.2, 0.2, 0);
            sw.spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 3, 0.3, 0.3, 0.3, 0);
        }
        this.discard();
    }

    private void onHitEntity(LivingEntity target) {
        if (this.getWorld().isClient || !target.isAlive()) return;

        target.damage(this.getDamageSources().magic(), damage);
        
        if (radiationLevel > 0) {
            target.addStatusEffect(new StatusEffectInstance(
                    NukeMod.RADIATION, (int)(200 * radiationLevel), Math.max(1, (int)radiationLevel)));
        }
        if (gaussRound) {
            target.setVelocity(target.getVelocity().add(0, 1.5, 0));
            target.velocityModified = true;
        }
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 5, 0.2, 0.2, 0.2, 0.1);
        }
        this.discard();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("bullet_damage")) this.damage = nbt.getFloat("bullet_damage");
        if (nbt.contains("radiation")) this.radiationLevel = nbt.getFloat("radiation");
        this.gaussRound = nbt.getBoolean("gauss");
        this.life = nbt.getInt("life");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("bullet_damage", this.damage);
        nbt.putFloat("radiation", this.radiationLevel);
        nbt.putBoolean("gauss", this.gaussRound);
        nbt.putInt("life", this.life);
    }

    @Override
    protected void initDataTracker() {}
}
