package com.seamus.splatdata.menus.buttons;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public abstract class MenuButton
{
    private final ItemStack displayItem;
    private final Component displayText;

    public MenuButton(ItemStack displayItem, Component displayText)
    {
        this.displayItem = displayItem;
        this.displayText = displayText;
    }

    public MenuButton(ItemStack displayItem, String displayName)
    {
        this(displayItem, new TextComponent(displayName));
    }

    public ItemStack getDisplayItem()
    {
        return displayItem;
    }

    public Component getDisplayText()
    {
        return displayText;
    }

    public abstract void onClick(ServerPlayer player);
}