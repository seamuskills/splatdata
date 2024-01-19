package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.menus.MainMenu;
import com.seamus.splatdata.menus.MenuContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MenuCommand {
    public MenuCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("menu").executes((Command) -> {
            if (!(Command.getSource().getEntity() instanceof Player)){
                Command.getSource().sendFailure(new TextComponent("Only players may run this command!"));
                return 0;
            }
            ServerPlayer player = (ServerPlayer)Command.getSource().getEntity();
            player.openMenu(new MainMenu(player));
            return 0;
        }));
    }
}
