package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.splatcraft.forge.util.ColorUtils;

import java.util.ArrayList;
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
    public ArrayList<ResourceLocation> unlockedWeapons = new ArrayList<>();
    public int cash = 0;
    public boolean waveRespawning = false;
    public int matchSplats = 0;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        compoundTag.putInt("RespawnTicks",respawnTimeTicks);
        compoundTag.putInt("RespawnGamemode", respawnGamemode.getId());
        compoundTag.putString("deathMessage", deathMessage);
        compoundTag.putString("team", team);
        if (match != null) {compoundTag.putUUID("matchid", match);}
        compoundTag.putInt("lobbyStatus", lobbyStatus.ordinal());
        compoundTag.putInt("prefColor", preferredColor);
        compoundTag.putString("vote", vote);
        ListTag unlockedWeaponsTag = new ListTag();
        for (ResourceLocation res : unlockedWeapons) {unlockedWeaponsTag.add(StringTag.valueOf(res.toString()));}
        compoundTag.put("purchasedItems", unlockedWeaponsTag);
        compoundTag.putInt("cash", cash);
        compoundTag.putBoolean("waveRespawn", waveRespawning);
        compoundTag.putInt("matchSplats", matchSplats);
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
        ListTag unlockedWeaponsTag = nbt.getList("purchasedItems", ListTag.TAG_STRING);
        for (Tag t : unlockedWeaponsTag){
            unlockedWeapons.add(new ResourceLocation(t.getAsString()));
        }
        cash = nbt.getInt("cash");
        waveRespawning = nbt.getBoolean("waveRespawn");
        matchSplats = nbt.getInt("matchSplats");
    }

    public boolean inMatch(){
        return match != null;
    }
}
