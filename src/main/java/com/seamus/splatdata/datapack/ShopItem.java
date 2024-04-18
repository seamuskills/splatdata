package com.seamus.splatdata.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ShopItem {
    public int cost;
    public ItemStack item;
    public ResourceLocation id;
    public int slot;

    public ShopItem(ItemStack item, int cost, ResourceLocation id, boolean force, int slot){
        this.cost = cost;
        this.item = item;
        this.id = id;
        this.item.getOrCreateTag().putBoolean("splatdata.forced", force);
        this.slot = slot;
    }
}
