package com.seamus.splatdata.menus;

import com.seamus.splatdata.*;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;

public class RoomMenuJoin extends MultiPageMenu{
    public RoomMenuJoin(ServerPlayer player, int page) {
        super(player,page, new TextComponent("Join player"));
    }

    @Override
    public void init(ServerPlayer sourcePlayer) {
        ArrayList<MenuButton> buttonList = new ArrayList<>();
        ArrayList<ServerPlayer> players = new ArrayList<>();
        for (Match m : WorldCaps.get(player.level).activeMatches.values()){
            players.add((ServerPlayer) sourcePlayer.level.getPlayerByUUID(m.host));
        }
        for (Player p : players){
            Match targetMatch = WorldCaps.get(player.level).activeMatches.get(Capabilities.get(p).match);
            TextComponent[] lore = {
                    (TextComponent) new TextComponent(targetMatch.password.length == 0 ? "No password" : "Password Protected").withStyle(targetMatch.password.length == 0 ? ChatFormatting.GREEN : ChatFormatting.GOLD),
                    (TextComponent) new TextComponent("Game type: ").append(targetMatch.matchGameType.displayName),
                    (TextComponent) new TextComponent(targetMatch.inProgress ? "In progress" : "Click to join").withStyle(targetMatch.inProgress ? ChatFormatting.RED : ChatFormatting.GREEN),
            };
            ItemStack head = SplatcraftData.applyLoreArray(new ItemStack(Blocks.PLAYER_HEAD), lore);
            head.getOrCreateTag().putString("SkullOwner", p.getGameProfile().getName());
            buttonList.add(new FunctionButton(head, p.getName(), (player) -> {
                RoomCommand.joinRoom(player, p);
                player.closeContainer();
            }));
        }
        buttons = buttonList.toArray(MenuButton[]::new);
        super.init(sourcePlayer);
    }
}
