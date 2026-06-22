package com.nukemod.entity;

import com.nukemod.explosion.NuclearExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PrimedNuclearBombEntity extends Entity {
	private static final int DEFAULT_FUSE = 160;         // 8秒引信
	private int fuse;
	private float explosionPower = 100.0F;               // 爆炸威力（可配置）

	public PrimedNuclearBombEntity(EntityType<? extends PrimedNuclearBombEntity> entityType, World world) {
		super(entityType, world);
		this.fuse = DEFAULT_FUSE;
		this.intersectionChecked = true;
	}

	/** 标准核弹 */
	public PrimedNuclearBombEntity(World world, double x, double y, double z, LivingEntity igniter, float power) {
		this(ModEntities.NUCLEAR_BOMB, world);
		setPosition(x, y, z);
		this.fuse = DEFAULT_FUSE;
		this.explosionPower = power;
		double vx = this.random.nextGaussian() * 0.02;
		double vz = this.random.nextGaussian() * 0.02;
		this.setVelocity(vx, 0.2, vz);
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	public void tick() {
		if (this.fuse <= 0) {
			this.discard();
			this.explode();
			return;
		}

		this.fuse--;

		Vec3d velocity = this.getVelocity();
		this.setVelocity(velocity.add(0, -0.04, 0));
		this.move(MovementType.SELF, this.getVelocity());
		this.setVelocity(this.getVelocity().multiply(0.98));

		if (this.isOnGround()) {
			this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
		}

		if (this.getWorld().isClient) {
			if (this.fuse % 2 == 0) {
				this.getWorld().addParticle(ParticleTypes.SMOKE,
						this.getX() + (random.nextDouble() - 0.5) * 0.4,
						this.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.2,
						this.getZ() + (random.nextDouble() - 0.5) * 0.4,
						0, 0, 0);
			}
			if (this.fuse < 60) {
				for (int i = 0; i < 3; i++) {
					this.getWorld().addParticle(ParticleTypes.FLAME,
							this.getX() + (random.nextDouble() - 0.5) * 0.8,
							this.getY() + 0.3 + random.nextDouble() * 0.6,
							this.getZ() + (random.nextDouble() - 0.5) * 0.8,
							(random.nextDouble() - 0.5) * 0.15,
							0.1 + random.nextDouble() * 0.1,
							(random.nextDouble() - 0.5) * 0.15);
				}
				this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
						this.getX() + (random.nextDouble() - 0.5) * 0.6,
						this.getY() + 0.2,
						this.getZ() + (random.nextDouble() - 0.5) * 0.6,
						0, 0.1, 0);
			}
			if (this.fuse < 20 && this.fuse % 2 == 0) {
				this.getWorld().addParticle(ParticleTypes.INSTANT_EFFECT,
						this.getX() + (random.nextDouble() - 0.5) * 1.2,
						this.getY() + 0.8 + (random.nextDouble() - 0.5) * 0.8,
						this.getZ() + (random.nextDouble() - 0.5) * 1.2,
						0, 0, 0);
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

	public void setFuse(short fuse) { this.fuse = fuse; }
	public int getFuse() { return fuse; }
	public float getExplosionPower() { return explosionPower; }
	public void setExplosionPower(float power) { this.explosionPower = power; }

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.contains("fuse")) this.fuse = nbt.getShort("fuse");
		if (nbt.contains("power")) this.explosionPower = nbt.getFloat("power");
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putShort("fuse", (short) this.fuse);
		nbt.putFloat("power", this.explosionPower);
	}
}
