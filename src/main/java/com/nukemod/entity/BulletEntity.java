package com.nukemod.entity;

import com.nukemod.NukeMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BulletEntity extends PersistentProjectileEntity {

    private float damage = 5.0F;
    private float radiationLevel = 0.0F;
    private boolean gaussRound = false;

    public BulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public BulletEntity(World world, LivingEntity owner, float damage, float radiation, boolean gauss) {
        super(ModEntities.BULLET, owner, world);
        this.damage = damage;
        this.radiationLevel = radiation;
        this.gaussRound = gauss;
        this.setNoGravity(gauss);
    }

    @Override
    protected void onHit(LivingEntity target) {
        target.damage(getDamageSources().mobProjectile(this, (LivingEntity) getOwner()), damage);
        if (radiationLevel > 0) {
            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                    NukeMod.RADIATION, (int)(200 * radiationLevel), (int)radiationLevel));
        }
        if (gaussRound) {
            target.setVelocity(target.getVelocity().add(0, 1.5, 0));
            target.velocityModified = true;
            target.timeUntilRegen = 0;
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient && hitResult.getType() != HitResult.Type.MISS) {
            if (gaussRound && this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                sw.spawnParticles(ParticleTypes.SONIC_BOOM, getX(), getY(), getZ(), 5, 0.3, 0.3, 0.3, 0.05);
            }
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age > 200) {
            this.discard();
        }
        if (this.getWorld().isClient && this.age % 2 == 0) {
            int color = gaussRound ? 0x00AAFF : (radiationLevel > 0 ? 0x55FF55 : 0xFFAA00);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT,
                    getX(), getY(), getZ(), r, g, b);
        }
    }

    @Override
    protected ItemStack asItemStack() { return ItemStack.EMPTY; }

    @Override
    protected void initDataTracker() {}

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("bullet_damage")) this.damage = nbt.getFloat("bullet_damage");
        if (nbt.contains("radiation")) this.radiationLevel = nbt.getFloat("radiation");
        this.gaussRound = nbt.getBoolean("gauss");
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("bullet_damage", this.damage);
        nbt.putFloat("radiation", this.radiationLevel);
        nbt.putBoolean("gauss", this.gaussRound);
    }
}
