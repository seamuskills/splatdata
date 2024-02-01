package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;

import java.util.HashMap;
import java.util.Map;

public class GameTypeListener extends SimpleJsonResourceReloadListener {
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    private static final String gameTypePath = "gameTypes";
    public GameTypeListener() {
        super(gsonInstance, gameTypePath);
    }

    public static HashMap<String, GameType> gameTypes = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        gameTypes.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()){
            GameType.winCon winCon = GameType.winCon.turf;
            if (entry.getValue().getAsJsonObject().has("winCondition"))
                winCon = GameType.winCon.valueOf(GsonHelper.getAsString((JsonObject) entry.getValue(), "winCondition"));
            GameType.respawnMode respawnMode = GameType.respawnMode.normal;
            if (entry.getValue().getAsJsonObject().has("respawnMode"))
                respawnMode = GameType.respawnMode.valueOf(GsonHelper.getAsString((JsonObject)entry.getValue(), "respawnMode"));
            float gameTime = 3600; //3 min default
            if (entry.getValue().getAsJsonObject().has("timerMax"))
                gameTime = GsonHelper.getAsFloat((JsonObject) entry.getValue(), "timerMax");

            float respawnTime = 5.0f;
            if (entry.getValue().getAsJsonObject().has("respawnTime"))
                respawnTime = GsonHelper.getAsFloat((JsonObject) entry.getValue(), "respawnTime");
            gameTypes.put(entry.getKey().toString(), new GameType(winCon, respawnMode, gameTime, respawnTime));
        }
    }
}
