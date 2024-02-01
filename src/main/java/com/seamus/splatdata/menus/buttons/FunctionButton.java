package com.seamus.splatdata.menus.buttons;

import com.seamus.splatdata.menus.MenuContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FunctionButton extends MenuButton
{
    final ExecutableFunction function;
    final MenuContainer container;
    public FunctionButton(ItemStack displayItem, Component displayText, ExecutableFunction function, MenuContainer container)
    {
        super(displayItem, displayText);
        this.function = function;
        this.container = container;
    }

    public FunctionButton(ItemStack displayItem, Component displayText, ExecutableFunction function){
        this(displayItem, displayText, function, null);
    }

    @Override
    public void onClick(ServerPlayer player)
    {
        function.execute(player);
        if (container != null) {
            container.init(player);
        }
    }

    public interface ExecutableFunction{
        void execute(ServerPlayer player);
    }
}