package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.seamus.splatdata.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpawnCommand {
    public SpawnCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("spawn").executes((Command) -> {
            if (!(Command.getSource().getEntity() instanceof Player)){
                Command.getSource().sendFailure(new TextComponent("Only players may run this command."));
                return 0;
            }
            BlockPos spawn = WorldInfo.getSpawn((ServerPlayer) Command.getSource().getEntity());
            Command.getSource().getEntity().teleportTo(spawn.getX() + 0.5, spawn.getY() + SplatcraftData.blockHeight(spawn, Command.getSource().getPlayerOrException().getLevel()), spawn.getZ() + 0.5);
            CapInfo playerCaps = Capabilities.get((ServerPlayer)Command.getSource().getEntity());
            WorldInfo worldCaps = WorldCaps.get(Command.getSource().getEntity().getLevel());

            if (playerCaps.inMatch()){
                Match match = worldCaps.activeMatches.get(playerCaps.match);
                if (match.inProgress) {
                    match.excommunicate((ServerPlayer) Command.getSource().getEntity());
                    Command.getSource().getEntity().sendMessage(new TextComponent("You have left the room you were in due to teleporting to spawn!").withStyle(ChatFormatting.RED), Command.getSource().getEntity().getUUID());
                }
            }
            return 0;
        }));
    }
}
