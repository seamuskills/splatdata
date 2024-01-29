package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.menus.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MenuCommand {
    public MenuCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("menu").executes((Command) -> {
            ServerPlayer player = Command.getSource().getPlayerOrException();
            player.openMenu(new MainMenu(player));
            return 0;
        }));
        dispatcher.register(Commands.literal("menu").then(Commands.literal("room").executes((Command) -> {
            ServerPlayer player = Command.getSource().getPlayerOrException();
            player.openMenu(new RoomMenuMain(player));
            return 0;
        })));
        dispatcher.register(Commands.literal("menu").then(Commands.literal("vote").executes((Command) -> {
            ServerPlayer player = Command.getSource().getPlayerOrException();
            player.openMenu(new VoteMenu(player, 0));
            return 0;
        })));
        dispatcher.register(Commands.literal("menu").then(Commands.literal("shop").executes((Command) -> {
            ServerPlayer player = Command.getSource().getPlayerOrException();
            player.openMenu(new ShopMenu(player, 0));
            return 0;
        })));
    }
}
