package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.splatcraft.forge.registries.SplatcraftItems;

import java.util.List;

public class MultiPageMenu extends MenuContainer{
    int page;
    MenuButton[] buttons = {};
    public MultiPageMenu(ServerPlayer player, int page) {
        super(MenuSize.SIZE_6X9, new TextComponent("select player to join"), player);
        this.page = page;
    }

    @Override
    public void init(ServerPlayer sourcePlayer) {
        int startIndex = page * 54;
        if (page > 0){
            addButton(0, new FunctionButton(new ItemStack(Items.TIPPED_ARROW), new TextComponent("previous page"), (player) -> {
                page--;
                init(sourcePlayer);
            }));
            startIndex--;
        }
        for (int index = startIndex; index < buttons.length; index++){
            if (index > startIndex + 53) {
                addButton(index, new FunctionButton(new ItemStack(Items.ARROW), new TextComponent("next page"), (player) -> {
                    page++;
                    init(sourcePlayer);
                }));
            }
            addButton(index, buttons[index]);
        }
    }
}
