package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class CapInfo {
    public enum lobbyStates{out, notReady, ready, spectator}
    public int respawnTimeTicks = 0;
    public GameType respawnGamemode = GameType.SURVIVAL;
    public String deathMessage = "";
    public int team = -1;
    public boolean inMatch = false;
    public lobbyStates lobbyStatus = lobbyStates.out;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        compoundTag.putInt("RespawnTicks",respawnTimeTicks);
        compoundTag.putInt("RespawnGamemode", respawnGamemode.getId());
        compoundTag.putString("deathMessage", deathMessage);
        compoundTag.putInt("team", team);
        compoundTag.putBoolean("inMatch", inMatch);
        compoundTag.putInt("lobbyStatus", lobbyStatus.ordinal());
        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
        respawnTimeTicks = nbt.getInt("RespawnTicks");
        respawnGamemode = GameType.byId(nbt.getInt("RespawnGamemode"));
        deathMessage = nbt.getString("deathMessage");
        team = nbt.getInt("team");
        inMatch = nbt.getBoolean("inMatch");
        lobbyStatus = lobbyStates.values()[nbt.getInt("lobbyStatus")];
    }
}
