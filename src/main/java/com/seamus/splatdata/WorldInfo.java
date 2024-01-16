package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.GameType;
import net.splatcraft.forge.util.ColorUtils;
import net.splatcraft.forge.util.InkColor;

import java.util.ArrayList;

public class WorldInfo {
    ArrayList<Match> activeMatches;
    boolean fest;
    InkColor[] festTeams;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        ListTag matchesTag = new ListTag();
        for (Match match : activeMatches) matchesTag.add(match.writeNBT(new CompoundTag()));
        compoundTag.put("matches",matchesTag);

        ListTag festTeamsTag = new ListTag();
        for (InkColor color : festTeams) festTeamsTag.add(IntTag.valueOf(color.getColor()));
        compoundTag.put("festTeams",festTeamsTag);

        compoundTag.putBoolean("fest", fest);

        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
    }
}
