package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StageDataListener extends SimpleJsonResourceReloadListener {
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    private static final String stagesPath = "stage";

    public static final HashMap<String, StageData> stages = new HashMap<>();

    public StageDataListener() {
        super(gsonInstance, stagesPath);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        stages.clear();
        for (JsonElement json : resourceMap.values()){
            ArrayList<String> ignoredTeams;
            if (json.getAsJsonObject().has("ignoredTeams")){
                JsonArray jsonArray = GsonHelper.getAsJsonArray((JsonObject)json, "ignoredTeams");
                ignoredTeams = new Gson().fromJson(jsonArray, ArrayList.class);
            }else{
                ignoredTeams = new ArrayList<>();
            }
            StageData data = new StageData(GsonHelper.getAsString((JsonObject) json, "id"), Component.Serializer.fromJson(json.getAsJsonObject().getAsJsonObject("author")), Component.Serializer.fromJson(json.getAsJsonObject().getAsJsonObject("displayName")), ShapedRecipe.itemStackFromJson(json.getAsJsonObject().getAsJsonObject("icon")), ignoredTeams);
            stages.put(data.id, data);
        }
    }
}
