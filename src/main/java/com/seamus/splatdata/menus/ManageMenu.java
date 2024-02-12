package com.seamus.splatdata.menus;

import com.seamus.splatdata.*;
import com.seamus.splatdata.datapack.ShopDataListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.splatcraft.forge.registries.SplatcraftItems;

public class ManageMenu extends MenuContainer{
    public ManageMenu(ServerPlayer player) {
        super(MenuSize.SIZE_1X5, new TextComponent("Room settings"), player);
    }

    @Override
    public void init(ServerPlayer player) {
        CapInfo caps = Capabilities.get(player);
        WorldInfo wcaps = WorldCaps.get(player.level);
        //not in a match
        if (!caps.inMatch()){
            player.closeContainer();
            player.sendMessage(new TextComponent("You are not in a match!").withStyle(ChatFormatting.RED), player.getUUID());
            return;
        }
        Match match = wcaps.activeMatches.get(caps.match);
        //if I am not the host
        if (match.host != player.getUUID()){
            ItemStack hosthead = new ItemStack(Items.PLAYER_HEAD);
            hosthead.getOrCreateTag().putString("SkullOwner", player.level.getPlayerByUUID(match.host).getGameProfile().getName());
            addButton(1, new FunctionButton(hosthead, new TextComponent("Host: ").append(hosthead.getHoverName()), (p) -> {}));
            addButton(3, new FunctionButton(match.matchGameType.icon, new TextComponent("game type: ").append(match.matchGameType.displayName), (p) -> {}));
        }else{ //I am the host
            if (match.inProgress) {
                addButton(1, new FunctionButton(new ItemStack(Items.BARRIER), new TextComponent("Stop the Match"), (p) -> {
                    if (match.currentState == Match.matchStates.gameplay) {
                        match.broadcast(new TextComponent("The host has ended the match!"));
                        match.timeLeft = 1;
                    }else{
                        p.sendMessage(new TextComponent("The match cannot be stopped right now").withStyle(ChatFormatting.RED), player.getUUID());
                    }
                }));
            }else{
                addButton(1, new FunctionButton(new ItemStack(Items.ENDER_PEARL), new TextComponent("Switch gamemodes"), (p) -> p.openMenu(new ManageGamemodeMenu(p, 0))));
            }
            addButton(2, new FunctionButton(new ItemStack(Items.TRIPWIRE_HOOK), new TextComponent("Set Password"), (p) -> {
                p.openMenu(new PasswordMenu(p, (target, password) -> {
                    match.password = password.stream().mapToInt(Integer::intValue).toArray();
                    player.sendMessage(new TextComponent("Password set!").withStyle(ChatFormatting.GREEN), player.getUUID());
                }));
            }));
            addButton(3, new FunctionButton(new ItemStack(SplatcraftItems.splatBomb.get()), new TextComponent("Kick Player"), (p) -> {
                p.openMenu(new ManageMenuKick(p, 0));
            }));
        }
    }
}
