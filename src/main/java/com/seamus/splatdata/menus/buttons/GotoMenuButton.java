package com.seamus.splatdata.menus.buttons;

import com.seamus.splatdata.menus.MenuContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GotoMenuButton extends MenuButton
{
    public final MenuFactory<? extends MenuContainer> targetMenu;
    public GotoMenuButton(ItemStack displayItem, Component displayText, MenuFactory<? extends MenuContainer> targetMenu)
    {
        super(displayItem, displayText);
        this.targetMenu = targetMenu;
    }

    @Override
    public void onClick(ServerPlayer player)
    {
        player.openMenu(targetMenu.create(player));
    }

    public interface MenuFactory<T extends MenuContainer>
    {
        T create(ServerPlayer player);
    }
}
