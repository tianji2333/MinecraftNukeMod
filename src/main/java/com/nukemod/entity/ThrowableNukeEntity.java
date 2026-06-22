package com.nukemod.entity;

import com.nukemod.explosion.NuclearExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrowableNukeEntity extends Entity {

    private int fuse;
    private float explosionPower;
    private boolean waterOnly;
    private int radiationExtra;

    public ThrowableNukeEntity(EntityType<? extends ThrowableNukeEntity> entityType, World world) {
        super(entityType, world);
        this.fuse = 200;
        this.explosionPower = 100.0F;
        this.waterOnly = false;
        this.radiationExtra = 0;
        this.intersectionChecked = true;
    }

    public ThrowableNukeEntity(World world, double x, double y, double z, float power, int fuseTicks, boolean waterOnly, int radiationExtra) {
        this(ModEntities.THROWABLE_NUKE, world);
        setPosition(x, y, z);
        this.fuse = fuseTicks;
        this.explosionPower = power;
        this.waterOnly = waterOnly;
        this.radiationExtra = radiationExtra;
        double vx = this.random.nextGaussian() * 0.05;
        double vz = this.random.nextGaussian() * 0.05;
        this.setVelocity(vx, 0.3, vz);
    }

    public void setVelocityFromThrow(float pitch, float yaw, float speed, float divergence) {
        float radPitch = pitch * 0.017453292F;
        float radYaw = yaw * 0.017453292F;
        double vx = -Math.sin(radYaw) * Math.cos(radPitch) * speed;
        double vy = -Math.sin(radPitch) * speed;
        double vz = Math.cos(radYaw) * Math.cos(radPitch) * speed;
        this.setVelocity(vx, vy, vz);
        this.velocityDirty = true;
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        if (waterOnly && !this.isSubmergedInWater()) {
            if (this.age > 100) {
                this.discard();
                return;
            }
        }

        if (this.fuse <= 0) {
            this.discard();
            this.explode();
            return;
        }

        this.fuse--;

        Vec3d velocity = this.getVelocity();
        this.setVelocity(velocity.add(0, -0.03, 0));
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.99));

        if (this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
            if (this.fuse > 20) {
                this.fuse = Math.min(this.fuse, 40);
            }
        }

        if (this.isSubmergedInWater()) {
            this.setVelocity(this.getVelocity().multiply(0.8));
            if (this.getWorld().isClient && this.age % 4 == 0) {
                for (int i = 0; i < 5; i++) {
                    this.getWorld().addParticle(ParticleTypes.BUBBLE,
                            this.getX() + (random.nextDouble() - 0.5) * 0.8,
                            this.getY() + (random.nextDouble() - 0.5) * 0.8,
                            this.getZ() + (random.nextDouble() - 0.5) * 0.8,
                            0, 0.2, 0);
                }
            }
            if (waterOnly && this.fuse > 60) {
                this.fuse = 60;
            }
        }

        if (this.getWorld().isClient) {
            if (this.fuse % 3 == 0) {
                int color = waterOnly ? 0x4444FF : 0xFF4444;
                double r = ((color >> 16) & 0xFF) / 255.0;
                double g = ((color >> 8) & 0xFF) / 255.0;
                double b = (color & 0xFF) / 255.0;
                this.getWorld().addParticle(ParticleTypes.ENTITY_EFFECT,
                        this.getX() + (random.nextDouble() - 0.5) * 0.3,
                        this.getY() + 0.3 + (random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (random.nextDouble() - 0.5) * 0.3,
                        r, g, b);
            }
            if (this.fuse < 30 && this.fuse % 2 == 0) {
                this.getWorld().addParticle(ParticleTypes.FLAME,
                        this.getX() + (random.nextDouble() - 0.5) * 0.6,
                        this.getY() + 0.3 + random.nextDouble() * 0.4,
                        this.getZ() + (random.nextDouble() - 0.5) * 0.6,
                        0, 0.1, 0);
            }
        }
    }

    private void explode() {
        if (this.getWorld().isClient) return;
        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        NuclearExplosion explosion = new NuclearExplosion(
                serverWorld, this.getX(), this.getY(), this.getZ(), explosionPower);
        explosion.explode();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("fuse")) this.fuse = nbt.getShort("fuse");
        if (nbt.contains("power")) this.explosionPower = nbt.getFloat("power");
        if (nbt.contains("waterOnly")) this.waterOnly = nbt.getBoolean("waterOnly");
        if (nbt.contains("radiationExtra")) this.radiationExtra = nbt.getInt("radiationExtra");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("fuse", (short) this.fuse);
        nbt.putFloat("power", this.explosionPower);
        nbt.putBoolean("waterOnly", this.waterOnly);
        nbt.putInt("radiationExtra", this.radiationExtra);
    }

    public void setFuse(short fuse) { this.fuse = fuse; }
    public int getFuse() { return fuse; }
    public float getExplosionPower() { return explosionPower; }
    public void setExplosionPower(float power) { this.explosionPower = power; }
}
