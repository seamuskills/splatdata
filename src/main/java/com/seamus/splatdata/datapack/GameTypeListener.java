package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seamus.splatdata.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.seamus.splatdata.Config.*;

public class GameTypeListener extends SimpleJsonResourceReloadListener {
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    private static final String gameTypePath = "game_types";
    public GameTypeListener() {
        super(gsonInstance, gameTypePath);
    }

    public static HashMap<String, MatchGameType> gameTypes = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        gameTypes.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceLocationJsonElementMap.entrySet()){
            MatchGameType.winCon winCon = MatchGameType.winCon.turf;
            if (entry.getValue().getAsJsonObject().has("winCondition"))
                winCon = MatchGameType.winCon.valueOf(GsonHelper.getAsString((JsonObject) entry.getValue(), "winCondition"));
            MatchGameType.respawnMode respawnMode = MatchGameType.respawnMode.normal;
            if (entry.getValue().getAsJsonObject().has("respawnMode"))
                respawnMode = MatchGameType.respawnMode.valueOf(GsonHelper.getAsString((JsonObject)entry.getValue(), "respawnMode"));
            float gameTime = Data.matchTime.get().floatValue();
            if (entry.getValue().getAsJsonObject().has("timerMax"))
                gameTime = GsonHelper.getAsFloat((JsonObject) entry.getValue(), "timerMax");

            float respawnTime = 5.0f;
            if (entry.getValue().getAsJsonObject().has("respawnTime"))
                respawnTime = GsonHelper.getAsFloat((JsonObject) entry.getValue(), "respawnTime");

            ItemStack item = new ItemStack(Items.MAP);
            if (entry.getValue().getAsJsonObject().has("icon"))
                item = ShapedRecipe.itemStackFromJson(entry.getValue().getAsJsonObject().getAsJsonObject("icon"));

            Component displayName = Component.Serializer.fromJson(entry.getValue().getAsJsonObject().getAsJsonObject("displayName"));
            Component description = Component.Serializer.fromJson(entry.getValue().getAsJsonObject().getAsJsonObject("description"));
            gameTypes.put(entry.getKey().toString(), new MatchGameType(displayName, description,winCon, respawnMode, gameTime, respawnTime, item));
        }
    }
}
