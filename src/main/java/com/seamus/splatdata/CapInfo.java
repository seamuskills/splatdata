package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.splatcraft.forge.util.ColorUtils;

import java.util.UUID;

public class CapInfo {
    public enum lobbyStates{out, notReady, ready, spectator}
    public int respawnTimeTicks = 0;
    public GameType respawnGamemode = GameType.SURVIVAL;
    public String deathMessage = "";
    public String team = "";
    public lobbyStates lobbyStatus = lobbyStates.out;
    public int preferredColor = ColorUtils.getRandomStarterColor();
    public UUID match = null;
    public String vote = "";
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        compoundTag.putInt("RespawnTicks",respawnTimeTicks);
        compoundTag.putInt("RespawnGamemode", respawnGamemode.getId());
        compoundTag.putString("deathMessage", deathMessage);
        compoundTag.putString("team", team);
        if (match != null) {compoundTag.putUUID("matchid", match);}
        compoundTag.putInt("lobbyStatus", lobbyStatus.ordinal());
        compoundTag.putInt("prefColor", preferredColor);
        compoundTag.putString("vote", vote);
        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
        respawnTimeTicks = nbt.getInt("RespawnTicks");
        respawnGamemode = GameType.byId(nbt.getInt("RespawnGamemode"));
        deathMessage = nbt.getString("deathMessage");
        team = nbt.getString("team");
        if (nbt.contains("matchid")){ match = nbt.getUUID("matchid");}else{match = null;}
        lobbyStatus = lobbyStates.values()[nbt.getInt("lobbyStatus")];
        preferredColor = nbt.getInt("prefColor");
        vote = nbt.getString("vote");
    }

    public boolean inMatch(){
        return match != null;
    }
}
