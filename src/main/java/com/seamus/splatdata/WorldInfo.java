package com.seamus.splatdata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.splatcraft.forge.util.InkColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WorldInfo {
    public HashMap<UUID,Match> activeMatches = new HashMap<UUID,Match>();
    public boolean fest;
    public Level owner;
    public ArrayList<Integer> festTeams = new ArrayList<Integer>();
    public BlockPos spawn = null;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
//        ListTag matchesTag = new ListTag();
//        for (Match match : activeMatches.values()) matchesTag.add(match.writeNBT(new CompoundTag()));
//        compoundTag.put("matches",matchesTag);

        ListTag festTeamsTag = new ListTag();
        for (int color : festTeams) festTeamsTag.add(IntTag.valueOf(color));
        compoundTag.put("festTeams",festTeamsTag);

        compoundTag.putBoolean("fest", fest);

        if (spawn != null) {
            compoundTag.put("spawn", NbtUtils.writeBlockPos(spawn));
        }

        return compoundTag;
    }

    public static BlockPos getSpawn(ServerPlayer p){
        WorldInfo worldinfo = WorldCaps.get(p.getLevel());
        BlockPos spawn = p.getRespawnPosition();
        if (worldinfo.spawn != null){
            spawn = worldinfo.spawn;
        }
        if (spawn == null){
            spawn = p.getLevel().getSharedSpawnPos();
        }
        return spawn;
    }

    public void readNBT(CompoundTag nbt) {
//        ListTag MatchesTag = (ListTag)nbt.get("matches");
        ListTag festTeamsTag = (ListTag)nbt.get("festTeams");

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        activeMatches.clear();
//        if (MatchesTag != null) {
//            for (Object matchTag : MatchesTag.toArray()){
//                activeMatches.add(Match.readNBT((CompoundTag)matchTag, owner));
//            }
//        }

        if (festTeamsTag != null){
            festTeams = new ArrayList<Integer>();
            for (Object festTeam : festTeamsTag.toArray()) festTeams.add((int)festTeam);
        }

        if (nbt.contains("spawn")){
            spawn = NbtUtils.readBlockPos((CompoundTag)nbt.get("spawn"));
        }

        fest = nbt.getBoolean("fest");
    }
}
