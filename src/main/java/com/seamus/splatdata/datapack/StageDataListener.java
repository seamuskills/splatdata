package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
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
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        stages.clear();
        for (JsonElement json : resourceMap.values()){
            StageData data = new StageData(GsonHelper.getAsString((JsonObject) json, "id"), Component.Serializer.fromJson(json.getAsJsonObject().getAsJsonObject("author")), Component.Serializer.fromJson(json.getAsJsonObject().getAsJsonObject("displayName")), ShapedRecipe.itemStackFromJson(json.getAsJsonObject().getAsJsonObject("icon")));
            stages.put(data.id, data);
        }
    }
}
