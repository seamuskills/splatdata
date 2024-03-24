package com.seamus.splatdata.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seamus.splatdata.Capabilities;
import com.seamus.splatdata.FestTeam;
import com.seamus.splatdata.WorldCaps;
import com.seamus.splatdata.WorldInfo;
import com.seamus.splatdata.menus.JoinFestMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.splatcraft.forge.commands.arguments.InkColorArgument;
import net.splatcraft.forge.util.ColorUtils;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.util.List;
import java.util.Map;

public class FestCommand {
    //todo: Make the command structure actually work..
    public FestCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("teams").requires((c) -> c.hasPermission(3)).then(Commands.literal("set").then(Commands.argument("teamID", StringArgumentType.word()).then(Commands.argument("color", InkColorArgument.inkColor()).executes((C) -> {
            ServerPlayer player = C.getSource().getPlayerOrException();
            WorldInfo info = WorldCaps.get(player.level);
            if (info.festTeams.size() >= 5 && !info.festTeams.containsKey(C.getArgument("teamID", String.class))){
                C.getSource().sendFailure(new TextComponent("Only up to 5 teams are permitted!"));
                return -1;
            }
            info.festTeams.put(C.getArgument("teamID", String.class), new FestTeam(C.getArgument("teamID", String.class), InkColorArgument.getInkColor(C, "color")));
            C.getSource().sendSuccess(new TextComponent("Splatfest team " + C.getArgument("teamID", String.class) + " added with color ").withStyle(ChatFormatting.GREEN).append(ColorUtils.getFormatedColorName(InkColorArgument.getInkColor(C, "color"), false)), true);
            return 0;
        }))))));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("teams").requires((c) -> c.hasPermission(3)).then(Commands.literal("remove").then(Commands.argument("teamID", StringArgumentType.string()).executes((C) -> {
            ServerPlayer player = C.getSource().getPlayerOrException();
            WorldInfo info = WorldCaps.get(player.level);
            if (info.festTeams.containsKey(C.getArgument("teamID", String.class))){
                info.festTeams.remove(C.getArgument("teamID", String.class));
                C.getSource().sendSuccess(new TextComponent("Successfully removed team: " + C.getArgument("teamID", String.class)), true);
            }else{
                C.getSource().sendFailure(new TextComponent("No team by that name was discovered!"));
            }
            return 0;
        })))));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("teams").then(Commands.literal("list").executes((C) -> {
            ServerPlayer player = C.getSource().getPlayerOrException();
            WorldInfo info = WorldCaps.get(player.level);
            if (info.festTeams.isEmpty()) {C.getSource().sendFailure(new TextComponent("No splatfest teams found."));}
            player.sendMessage(new TextComponent("Splatfest teams:"), player.getUUID());
            for (Map.Entry<String, FestTeam> team : info.festTeams.entrySet()){
                player.sendMessage(new TextComponent(team.getKey() + ": ").append(ColorUtils.getFormatedColorName(team.getValue().color, false)), player.getUUID());
            }
            return info.festTeams.size();
        }))));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("start").requires((c) -> c.hasPermission(3)).executes((C) -> {
            WorldInfo info = WorldCaps.get(C.getSource().getPlayerOrException().level);
            if (info.fest) {C.getSource().sendFailure(new TextComponent("Splatfest is already in session!")); return 0;}
            info.fest = true;
            TextComponent teamsComponent = new TextComponent("");
            List<String> teamsToList = new java.util.ArrayList<>(info.festTeams.keySet().stream().toList());
            for (Map.Entry<String, FestTeam> team : info.festTeams.entrySet()){
                teamsToList.remove(team.getKey());
                boolean last = teamsToList.isEmpty();
                teamsComponent.append(new TextComponent(team.getKey()).withStyle(ColorUtils.getColorName(team.getValue().color).getStyle()).append(new TextComponent(last ? "" : "vs ").withStyle(ChatFormatting.DARK_GRAY)));
            }
            for (Player p : C.getSource().getPlayerOrException().level.players()){
                p.sendMessage(new TextComponent("[ Splatfest has started! ]").withStyle(ChatFormatting.LIGHT_PURPLE), p.getUUID());
                p.sendMessage(new TextComponent("[ Pick a team! ").withStyle(ChatFormatting.LIGHT_PURPLE).append(teamsComponent).append(new TextComponent(" ]").withStyle(ChatFormatting.LIGHT_PURPLE)), p.getUUID());
            }
            return 0;
        })));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("end").requires((c) -> c.hasPermission(3)).executes((C) -> {
            WorldInfo info = WorldCaps.get(C.getSource().getPlayerOrException().level);
            if (!info.fest){C.getSource().sendFailure(new TextComponent("No splatfest is currently in session!")); return 0;}
            info.fest = false;
            for (Player p : C.getSource().getPlayerOrException().level.players()){
                p.sendMessage(new TextComponent("[ Splatfest has ended! ]").withStyle(ChatFormatting.LIGHT_PURPLE), p.getUUID());
            }
            return 0;
        })));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("join").executes((C) -> {
            ServerPlayer player = C.getSource().getPlayerOrException();
            if (!WorldCaps.get(player.level).fest){
                C.getSource().sendFailure(new TextComponent("No splatfest is currently being held."));
            }else if (Capabilities.get(player).festTeam.isEmpty()){
                player.openMenu(new JoinFestMenu(player));
            }else{
                C.getSource().sendFailure(new TextComponent("You are already part of a splatfest team!"));
            }
            return 0;
        })));
        dispatcher.register(Commands.literal("splatfest").then(Commands.literal("query").requires(C -> C.hasPermission(2)).executes((C) -> {
            if (WorldCaps.get(C.getSource().getLevel()).fest) {
                C.getSource().sendSuccess(new TextComponent("Splatfest is happening!"), false);
            } else {
                C.getSource().sendFailure(new TextComponent("No splatfest is going on!"));
            }
            return WorldCaps.get(C.getSource().getLevel()).fest ? 1 : 0;
        })));
    }
}
