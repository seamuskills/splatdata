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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpawnCommand {
    public SpawnCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("spawn").executes((Command) -> {
            if (!(Command.getSource().getEntity() instanceof Player)){
                Command.getSource().sendFailure(new TextComponent("Only players may run this command."));
                return 0;
            }
            tpToSpawn(Command.getSource().getPlayerOrException(), true, true);
            return 0;
        }));
    }

    public static void tpToSpawn(ServerPlayer p, boolean setspawn, boolean foilRespawn){
        BlockPos spawn = WorldInfo.getSpawn(p);
        p.teleportTo(spawn.getX() + 0.5, spawn.getY() + SplatcraftData.blockHeight(spawn, p.getLevel()), spawn.getZ() + 0.5);
        CapInfo playerCaps = Capabilities.get(p);
        WorldInfo worldCaps = WorldCaps.get(p.getLevel());

        if (playerCaps.inMatch()){
            Match match = worldCaps.activeMatches.get(playerCaps.match);
            if (match.inProgress) {
                match.excommunicate(p);
                p.sendMessage(new TextComponent("You have left the room you were in due to teleporting to spawn!").withStyle(ChatFormatting.RED), p.getUUID());
            }
        }
        if (foilRespawn){
            playerCaps.respawnTimeTicks = -1;
            p.setGameMode(GameType.ADVENTURE);
        }

        if (setspawn) p.setRespawnPosition(Level.OVERWORLD,new BlockPos(p.position()), 0, true, false);
    }
}
