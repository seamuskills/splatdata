package com.seamus.splatdata.menus;

import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MultiPageMenu extends MenuContainer{
    int page;
    MenuButton[] buttons = {};
    public MultiPageMenu(ServerPlayer player, int page, TextComponent name) {
        super(MenuSize.SIZE_6X9, name, player);
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
