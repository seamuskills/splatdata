package com.seamus.splatdata.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ShopItem {
    public int cost;
    public ItemStack item;
    public ResourceLocation id;

    public ShopItem(ItemStack item, int cost, ResourceLocation id){
        this.cost = cost;
        this.item = item;
        this.id = id;
    }
}
