package com.seamus.splatdata.menus;

import com.seamus.splatdata.capabilities.Capabilities;
import com.seamus.splatdata.capabilities.CapInfo;
import com.seamus.splatdata.SplatcraftData;
import com.seamus.splatdata.commands.RoomCommand;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import com.seamus.splatdata.menus.buttons.GotoMenuButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.splatcraft.forge.registries.SplatcraftItems;
import net.splatcraft.forge.util.ColorUtils;

public class RoomMenuMain extends MenuContainer{
    public RoomMenuMain(ServerPlayer player) {
        super(MenuSize.SIZE_1X5, new TextComponent("Room Menu"), player);
    }

    @Override
    public void init(ServerPlayer p) {
        buttons.clear();
        CapInfo caps = Capabilities.get(p);
        ItemStack inkwell = new ItemStack(SplatcraftItems.inkwell.get());
        ColorUtils.setColorLocked(inkwell, true);
        ColorUtils.setInkColor(inkwell, ColorUtils.getEntityColor(p));
        SplatcraftData.applyLore(inkwell, new TextComponent("Set the color you revert to when leaving a match, use /preferredcolor <color> to use exact hex values"));
        if (caps.inMatch()){
            addButton(0, 0, new GotoMenuButton(inkwell, new TextComponent("Preferred color"), PrefColorMenu::new));
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
            addButton(0, 4, new GotoMenuButton(new ItemStack(Items.ANVIL), new TextComponent("Match Settings"), ManageMenu::new));
        }else{
            addButton(0, 0, new FunctionButton(SplatcraftData.applyLore(new ItemStack(Blocks.CRAFTING_TABLE), new TextComponent("Create a room for others to join")), new TextComponent("create room"), RoomCommand::createRoom, this));
            addButton(0, 2, new GotoMenuButton(inkwell, new TextComponent("Preferred color"), PrefColorMenu::new));
            addButton(0, 4, new FunctionButton(SplatcraftData.applyLore(new ItemStack(Blocks.PLAYER_HEAD), new TextComponent("Join existing room hosted by another player")), new TextComponent("join room"), (player) -> player.openMenu(new RoomMenuJoin(player, 0))));
        }
    }
}
