package com.seamus.splatdata.menus;

import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.datapack.StageData;
import com.seamus.splatdata.datapack.StageDataListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.Objects;

public class VoteMenu extends MultiPageMenu{
    public int page = 0;
    public VoteMenu(ServerPlayer player, int page) {
        super(player, page, new TextComponent("Vote for a map"));
    }

    @Override
    public void init(ServerPlayer originPlayer) {
        ArrayList<MenuButton> buttonList = new ArrayList<>();
        buttonList.add(new FunctionButton(SplatcraftData.applyLore(new ItemStack(Items.CHORUS_FRUIT), new TextComponent("Set vote for randomly selected stage")), new TextComponent("Random Stage"), (player) -> {
            Capabilities.get(player).vote = "Random"; player.closeContainer();
            player.sendMessage(new TextComponent("map vote set to choose randomly"), player.getUUID());
        }));
        for (StageData stage : StageDataListener.stages.values()){
            ItemStack icon = stage.icon.copy();
            if (Objects.equals(Capabilities.get(originPlayer).vote, stage.id)){
                icon.enchant(Enchantments.MENDING, 1);
            }
            buttonList.add(new FunctionButton(SplatcraftData.applyLore(icon, new TextComponent("Click to set vote")), stage.displayName, (player) -> {
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
