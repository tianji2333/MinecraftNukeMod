package com.nukemod.block;

import com.nukemod.entity.PrimedNuclearBombEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class NuclearMineBlock extends Block {

    private final float explosionPower;

    public NuclearMineBlock(Settings settings, float power) {
        super(settings);
        this.explosionPower = power;
    }

    public float getExplosionPower() {
        return explosionPower;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof LivingEntity) {
            primeMine(world, pos, null);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        Item item = heldItem.getItem();

        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            primeMine(world, pos, player);
            if (item == Items.FLINT_AND_STEEL) {
                heldItem.damage(1, player, p -> p.sendToolBreakStatus(hand));
            } else {
                heldItem.decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)) {
            primeMine(world, pos, null);
        }
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient) {
            PrimedNuclearBombEntity nuke = new PrimedNuclearBombEntity(world,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null, explosionPower);
            nuke.setFuse((short) (world.random.nextInt(10) + 5));
            world.spawnEntity(nuke);
        }
    }

    private void primeMine(World world, BlockPos pos, LivingEntity igniter) {
        if (!world.isClient) {
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED,
                    SoundCategory.BLOCKS, 1.5F, 1.2F);

            PrimedNuclearBombEntity nuke = new PrimedNuclearBombEntity(world,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter, explosionPower);
            nuke.setFuse((short) 40);
            world.spawnEntity(nuke);
        }
        world.removeBlock(pos, false);
    }
}
