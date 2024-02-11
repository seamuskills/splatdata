package com.seamus.splatdata.menus;

import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.Match;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.datapack.GameTypeListener;
import com.seamus.splatdata.datapack.MatchGameType;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class ManageGamemodeMenu extends MultiPageMenu{
    public ManageGamemodeMenu(ServerPlayer player, int page) {
        super(player, page);
    }

    @Override
    public void init(ServerPlayer player){
        ArrayList<MenuButton> buttons = new ArrayList<>();
        for (MatchGameType g : GameTypeListener.gameTypes.values()){
            ItemStack icon = g.icon;
            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(g.description)));
            icon.getOrCreateTagElement("display").put("Lore", lore);
            buttons.add(new FunctionButton(icon, g.displayName, (p) -> {
                Match match = WorldCaps.get(p.level).activeMatches.get(Capabilities.get(p).match);
                if (match.inProgress){
                    p.sendMessage(new TextComponent("cannot change game type now").withStyle(ChatFormatting.RED), p.getUUID());
                }
                match.matchGameType = g;
                match.broadcast(new TextComponent("Game type changed to ").append(g.displayName));
                p.closeContainer();
            }));
        }
        this.buttons = buttons.toArray(new MenuButton[0]);
        super.init(player);
    }
}
