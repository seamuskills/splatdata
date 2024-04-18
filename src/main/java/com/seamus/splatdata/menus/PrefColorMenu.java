package com.seamus.splatdata.menus;

import com.seamus.splatdata.capabilities.Capabilities;
import com.seamus.splatdata.datapack.ColorListener;
import com.seamus.splatdata.menus.buttons.FunctionButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.splatcraft.forge.registries.SplatcraftItems;
import net.splatcraft.forge.util.ColorUtils;

public class PrefColorMenu extends MenuContainer{
    ItemStack filter;
    int filterIndex = 0;
    int colorIndex = 0;
    public PrefColorMenu(ServerPlayer player) {
        super(MenuSize.SIZE_6X9, new TextComponent("Preferred Color"), player);
    }

    @Override
    public void init(ServerPlayer player) {
        buttons.clear();
        if (filter == null && !ColorListener.filters.isEmpty())
            filter = ColorListener.filters.keySet().toArray(new ItemStack[0])[0];
        //todo, scrollmenu on top for all tags under color_menu
        //todo, make buttons using inked_wool in order to select a color
        if (filterIndex > 0) {
            addButton(0, 0, new FunctionButton(new ItemStack(Items.TIPPED_ARROW), new TextComponent("Back"), (p) -> {
                filterIndex++;
                init(p);
            }));
        }
        if (ColorListener.filters.size() > 7){
            addButton(0, 8, new FunctionButton(new ItemStack(Items.ARROW), new TextComponent("Forward"), (p) -> {
                filterIndex--;
                init(p);
            }));
        }

        int i = 0;
        for (ItemStack key : ColorListener.filters.keySet()){
            if (i - filterIndex > 7 || i - filterIndex < 0) {
                continue;
            }
            addButton(0, 1 + (i - filterIndex), new FunctionButton(key, key.getHoverName(), (p) -> {
                filter = key;
                init(p);
            }));
            i++;
        }

        if (colorIndex > 0) {
            addButton(1, 0, new FunctionButton(new ItemStack(Items.TIPPED_ARROW), new TextComponent("Back"), (p) -> {
                colorIndex++;
                init(p);
            }));
        }
        if (ColorListener.filters.get(filter).length / 7 > 5){
            addButton(5, 8, new FunctionButton(new ItemStack(Items.ARROW), new TextComponent("Forward"), (p) -> {
                colorIndex--;
                init(p);
            }));
        }

        i = 0;
        for (int color : ColorListener.filters.get(filter)){
            int row = 1 + ((i - colorIndex) / 7);
            int col = 1 + (i % 7);
            if (row - colorIndex < 0 || row - colorIndex > 5){
                continue;
            }
            ItemStack colorIcon = new ItemStack(SplatcraftItems.inkedGlassPane.get());
            ColorUtils.setInkColor(colorIcon, color);
            addButton(row, col, new FunctionButton(colorIcon, ColorUtils.getColorName(color), (p) -> {
                Capabilities.get(p).preferredColor = color;
                if (!Capabilities.get(p).inMatch()) ColorUtils.setPlayerColor(p, color);
                p.sendMessage(new TextComponent("Color set to ").append(ColorUtils.getColorName(color)), p.getUUID());
                p.closeContainer();
            }));
            i++;
        }
    }
}
