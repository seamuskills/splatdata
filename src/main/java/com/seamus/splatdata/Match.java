package com.seamus.splatdata;

import net.minecraft.gametest.framework.TeamcityTestReporter;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.splatcraft.forge.data.Stage;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfoCapability;
import net.splatcraft.forge.util.ColorUtils;

import java.lang.reflect.Array;
import java.util.*;

public class Match {
    public int timeLeft = 0;
    public Stage stage;
    public ArrayList<Player> players;
    public int teamAmount;
    public List<String> teams;
    public UUID id;

    public String stageID = "";
    public ServerLevel level;
    public Match(String stageid, ArrayList<Player> p, ServerLevel l, UUID matchid){
        players = p;
        stageID = stageid;
        stage = SaveInfoCapability.get(l.getServer()).getStages().get(stageID);
        teamAmount = stage.getTeamIds().size();
        teams = (List<String>) stage.getTeamIds();
        id = matchid;
        level = l;
    }

    Match(String stageid, ArrayList<Player> p, ServerLevel l) {
        this(stageid, p, l, UUID.randomUUID());
    }

    public void startGame(){
        int teamAssign = 0;
        for (Player player : players) {
            CapInfo caps = Capabilities.get(player);
            switch (caps.lobbyStatus) {
                case out:
                    throw new RuntimeException("Someone outside the lobby was queued!");
                case notReady:
                    throw new RuntimeException("Someone was not ready!");
                case ready:
                    caps.team = teams.get(teamAssign);
                    teamAssign = (teamAssign + 1) % teamAmount;
                    ColorUtils.setPlayerColor(player, stage.getTeamColor(caps.team));
                    break;
                case spectator:
                    ColorUtils.setPlayerColor(player, 0xffffff);
                    caps.team = "spec";
                    break;
            }
        }
    }

    public CompoundTag writeNBT(CompoundTag compoundTag) {
        //finish this
        ListTag ids = new ListTag();
        for (Player player : players) ids.add(players.indexOf(player), StringTag.valueOf(player.getStringUUID()));
        compoundTag.putInt("time",timeLeft);
        compoundTag.putString("stage", stageID);
        compoundTag.put("players", ids);
        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
        ListTag ids = (ListTag)nbt.get("players");
        if (ids == null) throw new RuntimeException("No players list tag!");
        players = new ArrayList<Player>();
        for (Tag id : ids){players.add(level.getPlayerByUUID(UUID.fromString(((StringTag)id).toString())));}
        timeLeft = nbt.getInt("time");
        stageID = nbt.getString("stage");
        stage = SaveInfoCapability.get(level.getServer()).getStages().get(stageID);
        teamAmount = stage.getTeamIds().size();
        teams = (List<String>)stage.getTeamIds();
    }

    public boolean playerInvolved(Player player){
        return players.contains(player);
    }
}
