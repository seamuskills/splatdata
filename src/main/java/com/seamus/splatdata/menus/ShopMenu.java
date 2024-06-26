package com.seamus.splatdata.menus;

import com.seamus.splatdata.capabilities.CapInfo;
import com.seamus.splatdata.capabilities.Capabilities;
import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.datapack.ShopDataListener;
import com.seamus.splatdata.datapack.ShopItem;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.registries.SplatcraftItems;

import java.util.ArrayList;

public class ShopMenu  extends MultiPageMenu{
    public ShopMenu(ServerPlayer p, int page){
        super(p, page, new TextComponent("Shop"));
    }

    @Override
    public void init(ServerPlayer player){
        ArrayList<MenuButton> buttonsList = new ArrayList<>();
        buttonsList.add(new FunctionButton(new ItemStack(SplatcraftItems.powerEgg.get()), new TextComponent("You have $" + Capabilities.get(player).cash), (p) -> {}));
        for (ShopItem item : ShopDataListener.shopItems){
            if (!Capabilities.get(player).unlockedWeapons.contains(item.id)) {
                MutableComponent name = item.item.getHoverName().copy().append(new TextComponent(", $" + item.cost));
                ItemStack displayItem = item.item.copy();
                displayItem.getOrCreateTag().putBoolean("splatdata.forced", false);
                buttonsList.add(new FunctionButton(SplatcraftData.applyLore(displayItem, new TextComponent("Click to attempt purchase")), name, (p) -> {
                    if (ShopMenu.purchase(p, item)){
                        p.sendMessage(new TextComponent("Item purchased!").withStyle(ChatFormatting.GREEN), p.getUUID());
                    }else{
                        p.sendMessage(new TextComponent("Not enough cash!").withStyle(ChatFormatting.RED), p.getUUID());
                    }
                    p.closeContainer();
                }));
            }else{
                MutableComponent name = item.item.getHoverName().copy();
                ItemStack displayItem = item.item.copy();
                displayItem.getOrCreateTag().putBoolean("splatdata.forced", false);
                buttonsList.add(new FunctionButton(SplatcraftData.applyLore(displayItem, new TextComponent("You own this item, click to grab it at no cost")), name, (p) -> {
                    p.getInventory().setItem(item.slot, item.item.copy());
                    p.closeContainer();
                }));
            }
        }
        buttons = buttonsList.toArray(MenuButton[]::new);
        super.init(player);
    }

    public static boolean purchase(ServerPlayer player, ShopItem item){
        if (!ShopDataListener.shopItems.stream().map((i) -> i.id).toList().contains(item.id)){
            throw new IllegalStateException("Attempted to purchase item that doesn't exist in the shop!");
        }
        CapInfo caps = Capabilities.get(player);
        if (caps.cash >= item.cost){
            caps.cash -= item.cost;
            caps.unlockedWeapons.add(item.id);
            player.getInventory().setItem(0, item.item.copy());
            return true;
        }else{
            return false;
        }
    }
}
