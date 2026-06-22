package com.nukemod.item;

import com.nukemod.entity.BulletEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class GunItem extends Item {

    protected final float damage;
    protected final float bulletSpeed;
    protected final int fireCooldown;
    protected final float spread;
    protected final float radiation;
    protected final boolean gauss;
    protected final int pellets;

    public GunItem(Settings settings, float damage, float bulletSpeed, int fireCooldown,
                   float spread, float radiation, boolean gauss, int pellets) {
        super(settings.maxCount(1));
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
        this.fireCooldown = fireCooldown;
        this.spread = spread;
        this.radiation = radiation;
        this.gauss = gauss;
        this.pellets = pellets;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            for (int i = 0; i < pellets; i++) {
                BulletEntity bullet = new BulletEntity(world, user, damage, radiation, gauss);
                float yaw = user.getYaw() + (world.random.nextFloat() - 0.5F) * spread;
                float pitch = user.getPitch() + (world.random.nextFloat() - 0.5F) * spread * 0.5F;
                bullet.setVelocity(user, pitch, yaw, 0.0F, bulletSpeed, gauss ? 0.0F : 1.0F);
                bullet.pickupType = net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission.DISALLOWED;
                world.spawnEntity(bullet);
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    gauss ? SoundEvents.ENTITY_WARDEN_SONIC_BOOM : SoundEvents.ENTITY_GENERIC_EXPLODE,
                    SoundCategory.PLAYERS, gauss ? 3.0F : 1.5F,
                    gauss ? 1.5F : (0.6F + world.random.nextFloat() * 0.4F));
        }

        user.getItemCooldownManager().set(this, fireCooldown);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, world.isClient());
    }
}
