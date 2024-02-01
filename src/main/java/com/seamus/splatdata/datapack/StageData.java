package com.seamus.splatdata.datapack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

//format:
//data/splatdata/stage/stage-name.json
//{
//      "id":  "stage id from /stage",
//      "author": {
//          "text":"name of author of map, same format as /tellraw"
//      },
//      "displayName": {
//          "text": "display name for the stage, same format as /tellraw"
//      },
//      "icon": {
//          "item": "item id"
//      }
//      "ignoredTeams": ["team", "names", "in", "an", "array", "this tag is optional."]
//}

public class StageData{
    public String id;
    public Component author;
    public Component displayName;
    public ItemStack icon;

    public ArrayList<String> ignoreTeams;
    public StageData(String id, Component author, Component displayName, ItemStack icon, ArrayList<String> ignoreTeams){
        this.id = id;
        this.author = author;
        this.displayName = displayName;
        this.icon = icon;
        this.ignoreTeams = ignoreTeams;
    }
}
