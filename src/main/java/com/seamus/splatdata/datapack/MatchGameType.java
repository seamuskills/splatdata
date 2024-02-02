package com.seamus.splatdata.datapack;

public class MatchGameType {
    public float respawnTime;
    public float matchTime;

    //todo add more possible win conditions
    public enum winCon {
        turf, //turf war
        splats, //highest team splats at the end
    }
    public winCon wCondition;

    public enum respawnMode {
        normal, //normal behavior, die then wait and respawn
        wave, //wave based, all of your teammates must die then you wait respawnTime seconds to respawn.
        waveOrTimed, // wave or timed, all of your teammates must die or the respawn timer runs out for you to respawn.
        disabled //no respawning. (not recommended and will end the match if only 1 team is standing.)
    }

    public respawnMode rMode;

    public MatchGameType(winCon wCondition, respawnMode rMode, float matchTime, float respawnTime){
        this.respawnTime = respawnTime;
        this.matchTime = matchTime * 20;
        this.wCondition = wCondition;
        this.rMode = rMode;
    }

//    public void update(Match match){
//        if (rMode == respawnMode.wave || rMode == respawnMode.waveOrTimed){ //if we have to worry about waves.
//            List<String> teams = match.teams;
//            for (String team : teams){
//                if (team.equals("spec")) continue; //disallowed team name
//                int color = match.stage.getTeamColor(team);
//                List<ServerPlayer> players = match.getPlayerList();
//                players.removeIf((p) -> {return ColorUtils.getEntityColor(p) == color;}); //get only players on this team.
//                if (players.isEmpty()) continue; //nobody on the team
//                //remove players who are dead
//                //remove all players who have no caps
//                players.removeIf((p) -> {
//                    if (Capabilities.hasCapability(p)) {
//                        return Capabilities.get(p).respawnTimeTicks > 0 || p.gameMode.getGameModeForPlayer().equals(net.minecraft.world.level.GameType.SPECTATOR);
//                    }else{
//                        return false;
//                    }
//                });
//
//                if (players.isEmpty()){ //if this is empty, all the team members are dead, so respawn them.
//                    //respawn all players
//                }
//            }
//        } else if (rMode == respawnMode.waveOrTimed || rMode == respawnMode.normal) { //todo, respawn modes: normal, disabled, timedWave timed behavior.
//            List<ServerPlayer> players = match.getPlayerList();
//            players.removeIf((p) -> {return !Capabilities.hasCapability(p);});
//            for (Player p : players){
//                CapInfo caps = Capabilities.get(p);
//                if (caps.respawnTimeTicks >= 0){
//                    caps.respawnTimeTicks--;
//                }else{
//
//                }
//            }
//        }
//    }
}
