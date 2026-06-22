package com.nukemod.item;

import net.minecraft.item.ItemStack;

public class RadiationShotgunItem extends GunItem {
    public RadiationShotgunItem(Settings settings) {
        super(settings, 4.0F, 2.0F, 20, 12.0F, 3.0F, false, 5);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }
}
