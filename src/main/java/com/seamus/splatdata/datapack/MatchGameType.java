package com.seamus.splatdata.datapack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchGameType {
    public Component displayName;
    public Component description;
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

    public ItemStack icon;

    public HashMap<Attribute, Double> attributes;

    public MatchGameType(Component displayName, Component description, winCon wCondition, respawnMode rMode, float matchTime, float respawnTime, ItemStack icon, HashMap<Attribute, Double> attributes){
        this.respawnTime = respawnTime;
        this.matchTime = matchTime * 20;
        this.wCondition = wCondition;
        this.rMode = rMode;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.attributes = attributes;
    }

}
