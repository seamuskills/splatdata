package com.seamus.splatdata.menus.buttons.roomMenu;

import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class JoinButton extends MenuButton
{
    final Player target;
    public JoinButton(ItemStack displayItem, Component displayText, Player target)
    {
        super(displayItem, displayText);
        this.target = target;
    }

    @Override
    public void onClick(ServerPlayer player)
    {
        RoomCommand.joinRoom(player, target);
    }
}