package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.CapInfo;
import com.seamus.splatdata.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ReadyCommand {
    public ReadyCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("ready").executes((Command) -> {
            CommandSourceStack source = Command.getSource();
            if (!(source.getEntity() instanceof Player)){
                source.sendFailure(new TextComponent("This command can only be executed by players."));
                return 0;
            }
            ServerPlayer player = (ServerPlayer)source.getEntity();
            CapInfo caps = Capabilities.get(player);
            if (caps.lobbyStatus != CapInfo.lobbyStates.out){
                if (caps.lobbyStatus == CapInfo.lobbyStates.ready){
                    source.sendFailure(new TextComponent("You are already been marked as ready."));
                    return 0;
                }
                caps.lobbyStatus = CapInfo.lobbyStates.ready;
                source.sendSuccess(new TextComponent("[ You have been marked as ready ]").withStyle(ChatFormatting.GREEN), false);
            }else{
                source.sendFailure(new TextComponent("You are not in the lobby! Players outside the lobby are not considered for readiness."));
            }
            return 0;
        }));

    }
}
