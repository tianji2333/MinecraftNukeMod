package com.nukemod.item;

import net.minecraft.item.ItemStack;

public class GaussCannonItem extends GunItem {
    public GaussCannonItem(Settings settings) {
        super(settings, 30.0F, 5.0F, 40, 0.5F, 0.0F, true, 1);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }
}
