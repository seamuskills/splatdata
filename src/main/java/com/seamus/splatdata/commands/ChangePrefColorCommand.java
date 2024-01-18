package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.splatcraft.forge.commands.arguments.InkColorArgument;
import net.splatcraft.forge.util.ColorUtils;

public class ChangePrefColorCommand {
    public ChangePrefColorCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("preferredcolor").then(Commands.argument("color", InkColorArgument.inkColor()).executes((Command) -> {
            if (!(Command.getSource().getEntity() instanceof Player)) {
                Command.getSource().sendFailure(new TextComponent("Only players can run this command"));
                return 0;
            }
            CapInfo caps = Capabilities.get((Player)Command.getSource().getEntity());
            caps.preferredColor = InkColorArgument.getInkColor(Command, "color");
            if (caps.lobbyStatus == CapInfo.lobbyStates.out){
                ColorUtils.setPlayerColor((Player)Command.getSource().getEntity(), caps.preferredColor);
            }
            Command.getSource().sendSuccess(new TextComponent("Preferred color set!"), false);
            return 0;
        })));
    }
}
