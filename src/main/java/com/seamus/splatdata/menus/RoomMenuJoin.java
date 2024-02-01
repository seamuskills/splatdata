package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class RoomMenuJoin extends MultiPageMenu{
    public RoomMenuJoin(ServerPlayer player, int page) {
        super(player,page);
    }

    @Override
    public void init(ServerPlayer sourcePlayer) {
        ArrayList<MenuButton> buttonList = new ArrayList<>();
        for (Player p : player.getLevel().getPlayers((p) -> {
            CapInfo caps = Capabilities.get(p);
            return caps.inMatch() && !WorldCaps.get(p.getLevel()).activeMatches.get(caps.match).inProgress;
        })){
            ItemStack head = new ItemStack(Blocks.PLAYER_HEAD);
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
