package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class CreditsListener extends SimpleJsonResourceReloadListener {
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    private static final String path = "credits";

    public static ArrayList<net.minecraft.network.chat.Component> credits = new ArrayList<>();

    public CreditsListener(){
        super(gsonInstance, path);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        for (JsonElement json : resourceLocationJsonElementMap.values()){
            for (JsonElement credit : GsonHelper.getAsJsonArray(json.getAsJsonObject(), "credits")){
                credits.add(net.minecraft.network.chat.Component.Serializer.fromJson(credit));
            }
        }
    }
}
