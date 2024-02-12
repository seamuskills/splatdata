package com.seamus.splatdata.menus;

import com.seamus.splatdata.datapack.ColorListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.items.FilterItem;
import net.splatcraft.forge.registries.SplatcraftItems;

import java.awt.*;

public class PrefColorMenu extends MenuContainer{
    ItemStack filter;
    int filterIndex = 0;
    public PrefColorMenu(ServerPlayer player) {
        super(MenuSize.SIZE_6X9, new TextComponent("Preferred Color"), player);
    }

    @Override
    public void init(ServerPlayer player) {
        if (filter == null && !ColorListener.filters.isEmpty())
            filter = ColorListener.filters.keySet().toArray(new ItemStack[0])[0];
        //todo, scrollmenu on top for all tags under color_menu
        //todo, make buttons using inked_wool in order to select a color

    }
}
