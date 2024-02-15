package com.seamus.splatdata.datapack;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.seamus.splatdata.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.registries.ForgeRegistries;
import net.splatcraft.forge.registries.SplatcraftAttributes;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

            HashMap<Attribute, Double> attributes = new HashMap<>();
            if (entry.getValue().getAsJsonObject().has("attributes")){
                JsonObject attributeObject = GsonHelper.getAsJsonObject(entry.getValue().getAsJsonObject(),"attributes");
                for (String attributeString : attributeObject.keySet()){
                    switch (attributeString){
                        case "MOVEMENT_SPEED":
                            attributes.put(Attributes.MOVEMENT_SPEED, GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "INK_SWIM_SPEED":
                            attributes.put(SplatcraftAttributes.inkSwimSpeed.get(), GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "MAX_HEALTH":
                            attributes.put(Attributes.MAX_HEALTH, GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "ARMOR":
                            attributes.put(Attributes.ARMOR, GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "ARMOR_TOUGHNESS":
                            attributes.put(Attributes.ARMOR_TOUGHNESS, GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "SUPER_JUMP_TRAVEL_TIME":
                            attributes.put(SplatcraftAttributes.superJumpTravelTime.get(), GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "SUPER_JUMP_WINDUP_TIME":
                            attributes.put(SplatcraftAttributes.superJumpWindupTime.get(), GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "SUPER_JUMP_HEIGHT":
                            attributes.put(SplatcraftAttributes.superJumpHeight.get(), GsonHelper.getAsDouble(attributeObject, attributeString));
                            break;
                        case "JUMP_STRENGTH":
                            attributes.put(Attributes.JUMP_STRENGTH, GsonHelper.getAsDouble(attributeObject, attributeString));
                        default:
                            LogManager.getLogger("splatdata").warn("Invalid attribute used in datapack! file: " + entry.getKey() + " attribute: " + attributeString);
                            break;
                    }
                }
            }

            gameTypes.put(entry.getKey().toString(), new MatchGameType(displayName, description,winCon, respawnMode, gameTime, respawnTime, item, attributes));
        }
    }
}
