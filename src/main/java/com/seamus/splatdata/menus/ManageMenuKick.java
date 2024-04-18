package com.seamus.splatdata.menus;

import com.seamus.splatdata.capabilities.CapInfo;
import com.seamus.splatdata.capabilities.Capabilities;
import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.capabilities.WorldCaps;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.MenuButton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class ManageMenuKick extends MultiPageMenu{
    public ManageMenuKick(ServerPlayer player, int page) {
        super(player,page, new TextComponent("Kick player"));
    }

    @Override
    public void init(ServerPlayer sourcePlayer) {
        ArrayList<MenuButton> buttonList = new ArrayList<>();
        for (ServerPlayer p : player.getLevel().getPlayers((p) -> {
            CapInfo caps = Capabilities.get(p);
            if (caps.inMatch()) {
                return caps.match.equals(Capabilities.get(sourcePlayer).match);
            }else{
                return false;
            }
        })){
            ItemStack head = SplatcraftData.applyLore(new ItemStack(Blocks.PLAYER_HEAD), new TextComponent("Click to kick"));
            head.getOrCreateTag().putString("SkullOwner", p.getGameProfile().getName());
            buttonList.add(new FunctionButton(head, p.getName(), (player) -> {
                WorldCaps.get(sourcePlayer.level).activeMatches.get(Capabilities.get(sourcePlayer).match).excommunicate(p);
                p.sendMessage(new TextComponent("You have been kicked by the host.").withStyle(ChatFormatting.RED), p.getUUID());
                p.closeContainer();
                init(sourcePlayer);
            }));
        }
        buttons = buttonList.toArray(MenuButton[]::new);
        super.init(sourcePlayer);
    }
}
