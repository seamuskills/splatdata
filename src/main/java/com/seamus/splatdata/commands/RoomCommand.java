package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seamus.splatdata.*;
import com.seamus.splatdata.capabilities.CapInfo;
import com.seamus.splatdata.capabilities.Capabilities;
import com.seamus.splatdata.capabilities.WorldCaps;
import com.seamus.splatdata.capabilities.WorldInfo;
import com.seamus.splatdata.menus.ManageMenu;
import com.seamus.splatdata.menus.PasswordMenu;
import com.seamus.splatdata.menus.RoomMenuJoin;
import com.seamus.splatdata.menus.RoomMenuMain;
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

import java.util.Arrays;

public class RoomCommand {
    public RoomCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("room").then(Commands.literal("create").executes(this::createRoom)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("join").then(Commands.argument("player", EntityArgument.player()).executes((c) -> {
            if (WorldCaps.get(c.getSource().getPlayerOrException().level).activeMatches.get(Capabilities.get(c.getArgument("player", Player.class)).match).inProgress){
                c.getSource().getPlayerOrException().openMenu(new RoomMenuJoin.spectatePrompt(c.getSource().getPlayerOrException(), (ServerPlayer) c.getArgument("player", Player.class)));
                return 0;
            }else{
                return joinRoom(c.getSource().getPlayerOrException(), c.getArgument("player", Player.class));
            }
        }))));
        dispatcher.register(Commands.literal("room").then(Commands.literal("join").executes((Command) -> {
            ServerPlayer p = Command.getSource().getPlayerOrException();
            p.openMenu(new RoomMenuJoin(p, 0));
            return 0;
        })));
        dispatcher.register(Commands.literal("room").then(Commands.literal("ready").executes(this::ready)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("unready").executes(this::unready)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("spec").executes(this::spec)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("leave").executes(this::leave)));
        dispatcher.register(Commands.literal("room").then(Commands.literal("list").executes(this::list)));
        dispatcher.register(Commands.literal("room").executes((command) -> {
            command.getSource().getPlayerOrException().openMenu(new RoomMenuMain(command.getSource().getPlayerOrException()));
            return 0;
        }));
        dispatcher.register(Commands.literal("room").then(Commands.literal("manage").executes(
                (Command) -> openManageMenu(Command.getSource().getPlayerOrException())
        )));
    }

    public int openManageMenu(ServerPlayer player){
        if (Capabilities.get(player).inMatch()) {
            player.openMenu(new ManageMenu(player));
        }else{
            player.sendMessage(new TextComponent("You are not in a room!").withStyle(ChatFormatting.RED), player.getUUID());
        }
        return 0;
    }

    public int list(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        Player player = command.getSource().getPlayerOrException();
        CapInfo playerCaps = Capabilities.get(player);
        if (playerCaps.inMatch()){
            WorldInfo worldcaps = WorldCaps.get(player.getLevel());
            player.sendMessage(new TextComponent("Room Player List"), player.getUUID());
            for (Player p : worldcaps.activeMatches.get(playerCaps.match).getPlayerList()){
                player.sendMessage(p.getName(), player.getUUID());
            }
        }else{
            command.getSource().sendFailure(new TextComponent("You are not in a match!"));
        }
        return 0;
    }

    private int createRoom(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        return createRoom(command.getSource().getPlayerOrException());
    }

    public static int createRoom(ServerPlayer player){
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        CapInfo playerCap = Capabilities.get(player);
        if (playerCap.inMatch()){
            player.sendMessage(new TextComponent("Room creation failed, you are already in a room!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        if (level != null) {
            WorldInfo worldCaps = WorldCaps.get(level);
            Match created = new Match(player, level);
            worldCaps.activeMatches.put(created.id, created);

            playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
            player.sendMessage(new TextComponent("Room created!").withStyle(ChatFormatting.GREEN),player.getUUID());
        }else{
            player.sendMessage(new TextComponent("null level!").withStyle(ChatFormatting.RED), player.getUUID());
        }
        return 0;
    }

    private int joinRoom(CommandContext<CommandSourceStack> command) throws CommandSyntaxException{
        return joinRoom(command.getSource().getPlayerOrException(), EntityArgument.getPlayer(command,"player"));
    }

    public static int joinRoom(ServerPlayer player, Player target, boolean spectate){
        CapInfo playerCap = Capabilities.get(player);
        if (playerCap.inMatch()){
            player.sendMessage(new TextComponent("You cannot enter multiple rooms.").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        if (level != null){
            WorldInfo worldCaps = WorldCaps.get(level);
            Match join = null;
            for (Match match : worldCaps.activeMatches.values()){
                if (match.playerInvolved(target)){
                    join = match;
                }
            }
            if (join == null){
                player.sendMessage(new TextComponent("No room found involving that user!").withStyle(ChatFormatting.RED), player.getUUID());
                return 0;
            }
//            if (join.inProgress){
//                player.sendMessage(new TextComponent("Room game in progress!").withStyle(ChatFormatting.RED), player.getUUID());
//                return 0;
//            }
            final Match targetMatch = worldCaps.activeMatches.get(Capabilities.get(target).match);
            if (targetMatch.password.length > 0) {
                Match finalJoin = join;
                player.openMenu(new PasswordMenu(player, (source, password) -> {
                    int[] convertedPassword = password.stream().mapToInt(Integer::intValue).toArray();
                    if (Arrays.equals(convertedPassword, targetMatch.password)) {
                        finalJoin.stalk(player, spectate);
                        playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
                        player.sendMessage(new TextComponent("Joined room!").withStyle(ChatFormatting.GREEN), player.getUUID());
                        finalJoin.broadcast(((TextComponent)player.getName()).append(new TextComponent("  joined.").withStyle(ChatFormatting.GREEN)));
                    } else {
                        player.sendMessage(new TextComponent("Incorrect Password!").withStyle(ChatFormatting.RED), player.getUUID());
                    }
                }));
            }else{
                join.stalk(player, spectate);
                playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
                player.sendMessage(new TextComponent("Joined room!").withStyle(ChatFormatting.GREEN), player.getUUID());
                join.broadcast(((TextComponent)player.getName()).append(new TextComponent("  joined.").withStyle(ChatFormatting.GREEN)));
            }
        }else{
            player.sendMessage(new TextComponent("null level!").withStyle(ChatFormatting.RED), player.getUUID());
        }
        return 0;
    }

    public static int joinRoom(ServerPlayer player, Player target) {
        return joinRoom(player, target, false);
    }

    private int ready(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        return ready(command.getSource().getPlayerOrException());
    }

    public static int ready(Player player){
        CapInfo playerCap = Capabilities.get(player);
        if (WorldCaps.get(player.level).fest && playerCap.festTeam.isEmpty()){
            player.sendMessage(new TextComponent("You are not part of a splatfest team and can thusly only spectate!").withStyle(ChatFormatting.RED), player.getUUID());
            return -1;
        }
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.ready){
            player.sendMessage(new TextComponent("You are already ready!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        if (!playerCap.inMatch()){
            player.sendMessage(new TextComponent("No room to ready up for!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        playerCap.lobbyStatus = CapInfo.lobbyStates.ready;
        player.sendMessage(new TextComponent("You are now ready!").withStyle(ChatFormatting.GREEN), player.getUUID());
        WorldInfo worldcaps = WorldCaps.get(player.getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)player.getName()).append(new TextComponent(" is ready.").withStyle(ChatFormatting.GREEN)));
        return 0;
    }

    private int unready(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        return unready(command.getSource().getPlayerOrException());
    }

    public static int unready(Player player){
        CapInfo playerCap = Capabilities.get(player);
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.notReady || !playerCap.inMatch()){
            player.sendMessage(new TextComponent("You were not ready to begin with!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        WorldInfo worldcaps = WorldCaps.get(player.getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)player.getName()).append(new TextComponent(" is no longer ready.").withStyle(ChatFormatting.RED)));
        playerCap.lobbyStatus = CapInfo.lobbyStates.notReady;
        player.sendMessage(new TextComponent("You are no longer ready!").withStyle(ChatFormatting.GREEN), player.getUUID());
        return 0;
    }

    private int spec(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        return spec(command.getSource().getPlayerOrException());
    }

    public static int spec(Player player){
        CapInfo playerCap = Capabilities.get(player);
        if (playerCap.lobbyStatus == CapInfo.lobbyStates.spectator){
            player.sendMessage(new TextComponent("You are already spectating!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        if (!playerCap.inMatch()){
            player.sendMessage(new TextComponent("No room to spectate in!").withStyle(ChatFormatting.RED), player.getUUID());
            return 0;
        }
        playerCap.lobbyStatus = CapInfo.lobbyStates.spectator;
        player.sendMessage(new TextComponent("You are now spectating!").withStyle(ChatFormatting.GREEN), player.getUUID());
        WorldInfo worldcaps = WorldCaps.get(player.getLevel());
        Match match = worldcaps.activeMatches.get(playerCap.match);
        match.broadcast(((TextComponent)player.getName()).append(new TextComponent(" is ready to spectate.").withStyle(ChatFormatting.WHITE)));
        return 0;
    }

    private int leave(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        return leave(command.getSource().getPlayerOrException());
    }

    public static int leave(Player player){
        ServerLevel level = (ServerLevel)player.getLevel();
        WorldInfo worldcaps = WorldCaps.get(level);
        CapInfo playerCaps = Capabilities.get(player);
        if (!(playerCaps.inMatch())){
            player.sendMessage(new TextComponent("You are not in a match!").withStyle(ChatFormatting.RED),player.getUUID());
            return 0;
        }
        Match currentMatch = worldcaps.activeMatches.get(playerCaps.match);
        currentMatch.broadcast(((TextComponent)player.getName()).append(new TextComponent(" has left the room!").withStyle(ChatFormatting.RED)));
        currentMatch.excommunicate((ServerPlayer) player, currentMatch.inProgress);
        player.sendMessage(new TextComponent("You left the match."), player.getUUID());
        playerCaps.lobbyStatus = CapInfo.lobbyStates.out;
        return 0;
    }
}
