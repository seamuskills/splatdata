package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.menus.VoteMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class VoteCommand {
    public VoteCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("vote").executes((Command) -> {
            Command.getSource().getPlayerOrException().openMenu(new VoteMenu(Command.getSource().getPlayerOrException(), 0));
            return 0;
        }));
    }
}
