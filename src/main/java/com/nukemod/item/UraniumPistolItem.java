package com.nukemod.item;

import net.minecraft.item.ItemStack;

public class UraniumPistolItem extends GunItem {
    public UraniumPistolItem(Settings settings) {
        super(settings, 5.0F, 2.5F, 5, 4.0F, 1.0F, false, 1);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }
}
