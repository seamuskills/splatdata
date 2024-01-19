package com.seamus.splatdata.datapack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class StageData{
    public String id;
    public Component author;
    public Component displayName;
    public ItemStack icon;
    public StageData(String id, Component author, Component displayName, ItemStack icon){
        this.id = id;
        this.author = author;
        this.displayName = displayName;
        this.icon = icon;
    }
}
