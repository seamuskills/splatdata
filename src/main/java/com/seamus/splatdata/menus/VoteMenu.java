package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.datapack.StageData;
import com.seamus.splatdata.datapack.StageDataListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.splatcraft.forge.registries.SplatcraftItems;

import java.util.List;

public class VoteMenu extends MenuContainer{
    public int page = 0;
    public VoteMenu(ServerPlayer player) {
        super(MenuSize.SIZE_6X9, new TextComponent("Select a Stage"), player);
    }

    @Override
    public void init(ServerPlayer originPlayer) {
        int startIndex = page * 54;
        if (page > 0){
            addButton(0, new FunctionButton(new ItemStack(Items.TIPPED_ARROW), new TextComponent("previous page"), (player) -> {
                player.openMenu(new RoomMenuJoin(player, page - 1));
            }));
            startIndex--;
        }
        for (int index = startIndex; index < StageDataListener.stages.size(); index++){
            if (index > startIndex + 53) {
                addButton(index, new FunctionButton(new ItemStack(Items.ARROW), new TextComponent("next page"), (player) -> {
                    player.openMenu(new RoomMenuJoin(player, page + 1));
                }));
            }
            StageData stage = ((StageData)StageDataListener.stages.values().toArray()[index]);
            addButton(index, new FunctionButton(stage.icon, stage.displayName, (player) -> {
                CapInfo caps = Capabilities.get(player);
                caps.vote = stage.id;
                player.sendMessage(new TextComponent("map vote set to ").append(stage.displayName), player.getUUID());
                player.closeContainer();
            }));
        }
    }
}
