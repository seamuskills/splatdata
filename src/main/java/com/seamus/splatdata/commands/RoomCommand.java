package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seamus.splatdata.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;

public class RoomCommand {
    public RoomCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("room").then(Commands.literal("create").executes(this::createRoom)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("join").then(Commands.argument("player", EntityArgument.player()).executes(this::joinRoom))));
        dispatcher.register(Commands.literal("room").then(Commands.literal("ready").executes(this::ready)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("unready").executes(this::unready)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("spec").executes(this::spec)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("leave").executes(this::leave)));
    }

    private int createRoom(CommandContext<CommandSourceStack> command){
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        CapInfo playerCap = Capabilities.get((Player)command.getSource().getEntity());
        if (playerCap.inMatch()){
            command.getSource().sendFailure(new TextComponent("Room creation failed, you are already in a room!"));
            return 0;
        }
        if (level != null) {
            WorldInfo worldCaps = WorldCaps.get(level);
            Match created = new Match("test", new ArrayList<>(), level);
            created.stalk((ServerPlayer)command.getSource().getEntity());
            worldCaps.activeMatches.put(created.id, created);

            playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
            command.getSource().sendSuccess(new TextComponent("Room created!"),false);
        }else{
            command.getSource().sendFailure(new TextComponent("null level!"));
        }
        return 0;
    }

    private int joinRoom(CommandContext<CommandSourceStack> command) throws CommandSyntaxException{
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        CapInfo playerCap = Capabilities.get((Player)command.getSource().getEntity());
        if (playerCap.inMatch()){
            command.getSource().sendFailure(new TextComponent("You cannot enter multiple rooms."));
            return 0;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        if (level != null){
            WorldInfo worldCaps = WorldCaps.get(level);
            Match join = null;
            for (Match match : worldCaps.activeMatches.values()){
                if (match.playerInvolved(EntityArgument.getPlayer(command, "player"))){
                    join = match;
                }
            }
            if (join == null){
                command.getSource().sendFailure(new TextComponent("No room found involving that user!"));
                return 0;
            }
            if (join.inProgress){
                command.getSource().sendFailure(new TextComponent("Room game in progress!"));
                return 0;
            }
            join.stalk((Player)command.getSource().getEntity());
            playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
            command.getSource().sendSuccess(new TextComponent("Joined room!"), false);
        }else{
            command.getSource().sendFailure(new TextComponent("null level!"));
        }
        return 0;
    }
    private int ready(CommandContext<CommandSourceStack> command){
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        CapInfo playerCap = Capabilities.get((Player)command.getSource().getEntity());
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.ready){
            command.getSource().sendFailure(new TextComponent("You are already ready!"));
            return 0;
        }
        if (!playerCap.inMatch()){
            command.getSource().sendFailure(new TextComponent("No room to ready up for!"));
            return 0;
        }
        playerCap.lobbyStatus = CapInfo.lobbyStates.ready;
        command.getSource().sendSuccess(new TextComponent("You are now ready!").withStyle(ChatFormatting.GREEN), false);
        WorldInfo worldcaps = WorldCaps.get(command.getSource().getEntity().getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)command.getSource().getEntity().getName()).append(new TextComponent(" is ready.").withStyle(ChatFormatting.GREEN)));
        return 0;
    }
    private int unready(CommandContext<CommandSourceStack> command){
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        CapInfo playerCap = Capabilities.get((Player)command.getSource().getEntity());
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.notReady || !playerCap.inMatch()){
            command.getSource().sendFailure(new TextComponent("You were not ready to begin with!"));
            return 0;
        }
        WorldInfo worldcaps = WorldCaps.get(command.getSource().getEntity().getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)command.getSource().getEntity().getName()).append(new TextComponent(" is no longer ready.").withStyle(ChatFormatting.RED)));
        playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
        command.getSource().sendSuccess(new TextComponent("You are no longer ready!").withStyle(ChatFormatting.GREEN), false);
        return 0;
    }
    private int spec(CommandContext<CommandSourceStack> command){
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        CapInfo playerCap = Capabilities.get((Player)command.getSource().getEntity());
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.spectator){
            command.getSource().sendFailure(new TextComponent("You are already spectating!"));
            return 0;
        }
        if (!playerCap.inMatch()){
            command.getSource().sendFailure(new TextComponent("No room to spectate in!"));
            return 0;
        }
        playerCap.lobbyStatus = CapInfo.lobbyStates.spectator;
        command.getSource().sendSuccess(new TextComponent("You are now spectating!").withStyle(ChatFormatting.GREEN), false);
        WorldInfo worldcaps = WorldCaps.get(command.getSource().getEntity().getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)command.getSource().getEntity().getName()).append(new TextComponent(" is ready to spectate.").withStyle(ChatFormatting.WHITE)));
        return 0;
    }

    private int leave(CommandContext<CommandSourceStack> command){
        if (!(command.getSource().getEntity() instanceof Player)){
            command.getSource().sendFailure(new TextComponent("This command can only be run by players"));
            return 0;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            WorldInfo worldcaps = WorldCaps.get(level);
            Match currentMatch = null;
            for (Match match : worldcaps.activeMatches.values()){
                if (match.playerInvolved((Player)command.getSource().getEntity())){
                    currentMatch = match;
                }
            }
            if (currentMatch == null){
                command.getSource().sendFailure(new TextComponent("You are not in a match!"));
            }else if (!currentMatch.inProgress){
                currentMatch.broadcast(((TextComponent)command.getSource().getEntity().getName()).append(new TextComponent(" has left the room!").withStyle(ChatFormatting.RED)));
                currentMatch.excommunicate((ServerPlayer) command.getSource().getEntity());
                command.getSource().sendSuccess(new TextComponent("You left the match."), false);
                CapInfo caps = Capabilities.get((Player)command.getSource().getEntity());
                caps.lobbyStatus = CapInfo.lobbyStates.out;
            }else{
                currentMatch.excommunicate((ServerPlayer) command.getSource().getEntity(), true);
                command.getSource().sendSuccess(new TextComponent("You left the match."), false);
                CapInfo caps = Capabilities.get((Player)command.getSource().getEntity());
                caps.lobbyStatus = CapInfo.lobbyStates.out;
            }
        }else{
            command.getSource().sendFailure(new TextComponent("level is null!"));
        }
        return 0;
    }
}
