package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
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

public class RoomMenuJoin extends MenuContainer{
    int page;
    public RoomMenuJoin(ServerPlayer player, int page) {
        super(MenuSize.SIZE_6X9, new TextComponent("select player to join"), player);
        this.page = page;
    }

    @Override
    public void init(ServerPlayer sourcePlayer) {
        int startIndex = page * 54;
        if (page > 0){
            addButton(0, new FunctionButton(new ItemStack(Items.TIPPED_ARROW), new TextComponent("previous page"), (player) -> {
                player.openMenu(new RoomMenuJoin(player, page - 1));
            }));
            startIndex--;
        }
        List<ServerPlayer> joinable = player.getLevel().getPlayers((p) -> {
            CapInfo caps = Capabilities.get(p);
            return caps.inMatch() && !WorldCaps.get(p.getLevel()).activeMatches.get(caps.match).inProgress;
        });
        for (int index = startIndex; index < joinable.size(); index++){
            if (index > startIndex + 53) {
                addButton(index, new FunctionButton(new ItemStack(Items.ARROW), new TextComponent("next page"), (player) -> {
                    player.openMenu(new RoomMenuJoin(player, page + 1));
                }));
            }
            Player targetPlayer = joinable.get(index);
            ItemStack head = new ItemStack(Blocks.PLAYER_HEAD);
            head.getOrCreateTag().putString("SkullOwner", targetPlayer.getGameProfile().getName());
            addButton(index, new FunctionButton(head, targetPlayer.getName(), (player) -> {
                RoomCommand.joinRoom(player, targetPlayer);
                player.closeContainer();
            }));
        }
    }
}
