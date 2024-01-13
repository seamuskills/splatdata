package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class CapInfo {
    public int respawnTimeTicks = 0;
    public GameType respawnGamemode = GameType.SURVIVAL;
    public CompoundTag writeNBT(CompoundTag compoundTag) {
        compoundTag.putInt("RespawnTicks",respawnTimeTicks);
        compoundTag.putInt("RespawnGamemode", respawnGamemode.getId());
        return compoundTag;
    }

    public void readNBT(CompoundTag nbt) {
        respawnTimeTicks = nbt.getInt("RespawnTicks");
        respawnGamemode = GameType.byId(nbt.getInt("RespawnGamemode"));
    }
}
