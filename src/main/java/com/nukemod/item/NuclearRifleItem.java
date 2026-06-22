package com.nukemod.item;

import net.minecraft.item.ItemStack;

public class NuclearRifleItem extends GunItem {
    public NuclearRifleItem(Settings settings) {
        super(settings, 8.0F, 3.0F, 10, 2.0F, 2.0F, false, 1);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }
}
