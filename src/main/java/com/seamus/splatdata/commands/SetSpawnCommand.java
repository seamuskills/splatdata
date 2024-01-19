package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.WorldCaps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SetSpawnCommand {
    public SetSpawnCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("setspawn").requires(source -> source.hasPermission(3)).then(Commands.argument("position", BlockPosArgument.blockPos()).executes((Command) -> {
            ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
            if (level == null){
                Command.getSource().sendFailure(new TextComponent("Null overworld!"));
                return 0;
            }
            BlockPos newSpawn = BlockPosArgument.getLoadedBlockPos(Command, "position");
            WorldCaps.get(level).spawn = newSpawn;
            Command.getSource().sendSuccess(new TextComponent("/spawn coordinates changed to " + newSpawn.toShortString()), true);
            return 0;
        })));
    }
}
