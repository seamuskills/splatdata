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

public class UnreadyCommand {
    public UnreadyCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("unready").executes((Command) -> {
            CommandSourceStack source = Command.getSource();
            if (!(source.getEntity() instanceof Player)){
                source.sendFailure(new TextComponent("This command can only be executed by players."));
                return 0;
            }
            ServerPlayer player = (ServerPlayer)source.getEntity();
            CapInfo caps = Capabilities.get(player);
            if (caps.lobbyStatus != CapInfo.lobbyStates.out){
                if (caps.lobbyStatus == CapInfo.lobbyStates.notReady){
                    source.sendFailure(new TextComponent("You are already been marked as not ready."));
                    return 0;
                }
                caps.lobbyStatus = CapInfo.lobbyStates.notReady;
                source.sendSuccess(new TextComponent("[ You have been marked as ").withStyle(ChatFormatting.GREEN).append(new TextComponent("not ready").withStyle(ChatFormatting.DARK_RED)).append(new TextComponent(" ]").withStyle(ChatFormatting.GREEN)), false);
            }else{
                source.sendFailure(new TextComponent("You are not in the lobby! Players outside the lobby are not considered for readiness."));
            }
            return 0;
        }));
    }
}
