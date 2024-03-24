package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.datapack.CreditsListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CreditsCommand {
    public CreditsCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("credits").executes((Command) -> {
            Player player = Command.getSource().getPlayerOrException();
            for (Component text : CreditsListener.credits){
                player.sendMessage(text, player.getUUID());
            }
            return 0;
        }));
    }
}
