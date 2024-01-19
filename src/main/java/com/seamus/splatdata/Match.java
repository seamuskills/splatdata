package com.seamus.splatdata;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.splatcraft.forge.data.Stage;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfoCapability;
import net.splatcraft.forge.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Match {
    public int timeLeft = 0;
    public Stage stage;
    public ArrayList<Player> players;
    public int teamAmount;
    public List<String> teams;
    public UUID id;

    public String stageID;
    public ServerLevel level;
    public boolean inProgress;
    public Match(String stageid, ArrayList<Player> p, ServerLevel l, UUID matchid){
        players = p;
        stageID = stageid;
        stage = SaveInfoCapability.get(l.getServer()).getStages().get(stageID);
        teamAmount = stage.getTeamIds().size();
        teams = stage.getTeamIds().stream().toList();
        id = matchid;
        level = l;
        inProgress = false;
    }

    public Match(String stageid, ArrayList<Player> p, ServerLevel l) {
        this(stageid, p, l, UUID.randomUUID());
    }

    public void update(){
        //todo make this decrease the timer and some other stuff...
    }

    public void stalk(Player p){
        players.add(p);
        CapInfo caps = Capabilities.get(p);
        caps.match = id;
    }

    public void excommunicate(ServerPlayer p, boolean tp){
        players.remove(p);
        Stage lobby = SaveInfoCapability.get(level.getServer()).getStages().get(Config.Data.stageName.get());
        if (!(new AABB(lobby.cornerA, lobby.cornerB).expandTowards(1, 1, 1).contains(p.position())) && tp){
            BlockPos spawn = WorldInfo.getSpawn(p);
            p.teleportTo(spawn.getX(), spawn.getY()+SplatcraftData.blockHeight(spawn, p.getLevel()),spawn.getZ());
        }
        CapInfo caps = Capabilities.get(p);
        caps.lobbyStatus = CapInfo.lobbyStates.out;
        caps.match = null;
        if (players.isEmpty()){
            WorldCaps.get(p.getLevel()).activeMatches.remove(this.id);
        }
    }

    public void broadcast(Component component){
        for (Player p : players){
            p.sendMessage(component, p.getUUID());
        }
    }

    public void excommunicate(ServerPlayer p){
        excommunicate(p, false);
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
        inProgress = true;
    }

//    public CompoundTag writeNBT(CompoundTag compoundTag) {
//        //finish this
//        ListTag ids = new ListTag();
//        for (Player player : players){
//            IntArrayTag currentID = NbtUtils.createUUID(player.getUUID());
//            ids.add(players.indexOf(player), currentID);
//        }
//        compoundTag.putInt("time",timeLeft);
//        compoundTag.putString("stage", stageID);
//        compoundTag.put("players", ids);
//        compoundTag.putUUID("id",id);
//        return compoundTag;
//    }
//
//    public static Match readNBT(CompoundTag nbt , Level owner) {
//        ServerLevel level = (ServerLevel)owner;
//        if (level == null) throw new NullPointerException("Null level!");
//        ListTag ids = nbt.getList("players", Tag.TAG_COMPOUND);
//        ArrayList<Player> players = new ArrayList<Player>();
//        for (Tag id : ids){
//            ServerPlayer p = (ServerPlayer)level.getPlayerByUUID(NbtUtils.loadUUID(id));
//            if (p == null){
//                continue;
//            }
//            players.add(p);
//        }
//        return new Match(nbt.getString("stage"), players, level, nbt.getUUID("id"));
//    }

    public boolean playerInvolved(Player player){
        return players.contains(player);
    }
}
