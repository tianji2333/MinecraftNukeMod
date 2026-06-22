package com.nukemod.item;

import com.nukemod.entity.ThrowableNukeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BriefcaseNukeItem extends Item {

    public BriefcaseNukeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 1.0F, 0.4F);

        if (!world.isClient) {
            ThrowableNukeEntity nuke = new ThrowableNukeEntity(world,
                    user.getX(), user.getEyeY() - 0.1, user.getZ(),
                    100.0F, 200, false, 0);
            nuke.setVelocityFromThrow(user.getPitch(), user.getYaw(), 1.2F, 1.0F);
            world.spawnEntity(nuke);
        }

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(stack, world.isClient());
    }
}
