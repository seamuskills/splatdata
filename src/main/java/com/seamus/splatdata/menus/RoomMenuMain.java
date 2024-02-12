package com.seamus.splatdata.menus;

import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class RoomMenuMain extends MenuContainer{
    public RoomMenuMain(ServerPlayer player) {
        super(MenuSize.SIZE_1X5, new TextComponent("Room Menu"), player);
    }

    @Override
    public void init(ServerPlayer p) {
        buttons.clear();
        CapInfo caps = Capabilities.get(p);
        if (caps.inMatch()){
            switch (caps.lobbyStatus) {
                case notReady:
                    addButton(0, 1, new FunctionButton(new ItemStack(Blocks.LIME_WOOL), new TextComponent("ready"), RoomCommand::ready, this));
                    addButton(0, 2, new FunctionButton(new ItemStack(Items.ENDER_EYE), new TextComponent("spectate"), RoomCommand::spec, this));
                    break;
                case ready:
                    addButton(0, 1, new FunctionButton(new ItemStack(Blocks.RED_WOOL), new TextComponent("unready"), RoomCommand::unready, this));
                    addButton(0, 2, new FunctionButton(new ItemStack(Items.ENDER_EYE), new TextComponent("spectate"), RoomCommand::spec, this));
                    break;
                case spectator:
                    addButton(0, 1, new FunctionButton(new ItemStack(Blocks.LIME_WOOL), new TextComponent("ready"), RoomCommand::ready, this));
                    addButton(0, 2, new FunctionButton(new ItemStack(Blocks.RED_WOOL), new TextComponent("unready"), RoomCommand::unready, this));
                    break;
            }
            addButton(0, 3, new FunctionButton(new ItemStack(Items.RABBIT_FOOT), new TextComponent("leave"), RoomCommand::leave, this));
        }else{
            addButton(0, 0, new FunctionButton(SplatcraftData.applyLore(new ItemStack(Blocks.CRAFTING_TABLE), new TextComponent("Create a room for others to join")), new TextComponent("create room"), RoomCommand::createRoom, this));
            addButton(0, 4, new FunctionButton(SplatcraftData.applyLore(new ItemStack(Blocks.PLAYER_HEAD), new TextComponent("Join existing room hosted by another player")), new TextComponent("join room"), (player) -> player.openMenu(new RoomMenuJoin(player, 0))));
        }
    }
}
