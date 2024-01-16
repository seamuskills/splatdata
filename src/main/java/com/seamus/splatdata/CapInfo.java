package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class CapInfo {
    public enum lobbyStates{out, notReady, ready, spectator}
    public int respawnTimeTicks = 0;
    public GameType respawnGamemode = GameType.SURVIVAL;
    public String deathMessage = "";
    public String team = "";
    public boolean inMatch = false;
    public lobbyStates lobbyStatus = lobbyStates.out;
    public int preferredColor;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        compoundTag.putInt("RespawnTicks",respawnTimeTicks);
        compoundTag.putInt("RespawnGamemode", respawnGamemode.getId());
        compoundTag.putString("deathMessage", deathMessage);
        compoundTag.putString("team", team);
        compoundTag.putBoolean("inMatch", inMatch);
        compoundTag.putInt("lobbyStatus", lobbyStatus.ordinal());
        compoundTag.putInt("prefColor", preferredColor);
        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
        respawnTimeTicks = nbt.getInt("RespawnTicks");
        respawnGamemode = GameType.byId(nbt.getInt("RespawnGamemode"));
        deathMessage = nbt.getString("deathMessage");
        team = nbt.getString("team");
        inMatch = nbt.getBoolean("inMatch");
        lobbyStatus = lobbyStates.values()[nbt.getInt("lobbyStatus")];
        preferredColor = nbt.getInt("prefColor");
    }
}
