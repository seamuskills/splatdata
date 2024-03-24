package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;

public class FestTeam {
    public int color = 0;
    public String id = "";
    public int wins = 0;
    public int players = 0;
    public int matches = 0;
    public byte clout = 0; //I will byte you!
    public FestTeam(String id, int color){
        this.color = color;
        this.id = id;
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("color", color);
        tag.putString("teamID", id);
        tag.putInt("wins", wins);
        tag.putInt("playerCount", players);
        tag.putInt("matchesPlayed", matches);
        tag.putByte("clout", clout);
        return tag;
    }

    public static FestTeam fromTag(CompoundTag tag){
        FestTeam team = new FestTeam(tag.getString("teamID"), tag.getInt("color"));
        team.wins = tag.getInt("wins");
        team.players = tag.getInt("playerCount");
        team.matches = tag.getInt("matchesPlayed");
        team.clout = tag.getByte("clout");
        return team;
    }
}
