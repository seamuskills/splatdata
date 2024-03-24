package com.seamus.splatdata.menus;

import com.seamus.splatdata.*;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.splatcraft.forge.registries.SplatcraftItems;
import net.splatcraft.forge.util.ColorUtils;

import java.util.Map;

public class JoinFestMenu extends MenuContainer{
    public JoinFestMenu(ServerPlayer player) {
        super(MenuContainer.MenuSize.SIZE_1X5, new TextComponent("Join team"), player);
    }

    @Override
    public void init(ServerPlayer player) {
        WorldInfo worldInfo = WorldCaps.get(player.level);

        int index = 0;
        for (Map.Entry<String, FestTeam> team : worldInfo.festTeams.entrySet()){
            ItemStack item = new ItemStack(SplatcraftItems.inkedWool.get());
            ColorUtils.setInkColor(item, team.getValue().color);
            SplatcraftData.applyLore(item, new TextComponent("Click to join team! You cannot join another team after joining!"));
            addButton(index, new FunctionButton(item, new TextComponent(team.getKey()), (p) -> {
                Capabilities.get(p).festTeam = team.getKey();
                p.closeContainer();
            }));
            index++;
        }
    }
}
