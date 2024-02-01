package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.datapack.StageData;
import com.seamus.splatdata.datapack.StageDataListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class VoteMenu extends MultiPageMenu{
    public int page = 0;
    public VoteMenu(ServerPlayer player, int page) {
        super(player, page);
    }

    @Override
    public void init(ServerPlayer originPlayer) {
        ArrayList<MenuButton> buttonList = new ArrayList<>();
        buttonList.add(new FunctionButton(new ItemStack(Items.CHORUS_FRUIT), new TextComponent("Random Stage"), (player) -> {
            Capabilities.get(player).vote = "Random"; player.closeContainer();
            player.sendMessage(new TextComponent("map vote set to choose randomly"), player.getUUID());
        }));
        for (StageData stage : StageDataListener.stages.values()){
            buttonList.add(new FunctionButton(stage.icon, stage.displayName, (player) -> {
                CapInfo caps = Capabilities.get(player);
                caps.vote = stage.id;
                player.sendMessage(new TextComponent("map vote set to ").append(stage.displayName), player.getUUID());
                player.closeContainer();
            }));
        }
        buttons = buttonList.toArray(MenuButton[]::new);
        super.init(originPlayer);
    }
}
