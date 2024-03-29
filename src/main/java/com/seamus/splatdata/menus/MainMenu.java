package com.seamus.splatdata.menus;

import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.datapack.ShopDataListener;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.splatcraft.forge.registries.SplatcraftItems;

public class MainMenu extends MenuContainer{
    public MainMenu(ServerPlayer player) {
        super(MenuSize.SIZE_1X5, new TextComponent("Main Menu"), player);
    }

    @Override
    public void init(ServerPlayer player) {
        addButton(0, 0, new GotoMenuButton(SplatcraftData.applyLore(new ItemStack(SplatcraftItems.splattershot.get(), 1), new TextComponent("Join, create, or manage involvement in a room")), new TextComponent("Rooms"), RoomMenuMain::new));
        addButton(0, 2, new GotoMenuButton(SplatcraftData.applyLore(new ItemStack(Items.MAP, 1), new TextComponent("Select the map you would like to play")), new TextComponent("Set map vote"), (p) -> new VoteMenu(p, 0)));
        if (!ShopDataListener.shopItems.isEmpty()) addButton(0, 4, new GotoMenuButton(SplatcraftData.applyLore(new ItemStack(SplatcraftItems.sunkenCrate.get()), new TextComponent("Purchase items using currency from matches")), new TextComponent("Shop"), (p) -> new ShopMenu(p, 0)));
    }
}
