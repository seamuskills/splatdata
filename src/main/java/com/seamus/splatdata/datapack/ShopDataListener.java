package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class ShopDataListener extends SimpleJsonResourceReloadListener {
    private static final String shopPath = "shop";
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    public static final ArrayList<ShopItem> shopItems = new ArrayList<>();
    public ShopDataListener() {
        super(gsonInstance, shopPath);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        shopItems.clear();
        for (Map.Entry<ResourceLocation, JsonElement> json: resourceLocationJsonElementMap.entrySet()){
            boolean force = true;
            int slot = 0;
            if (json.getValue().getAsJsonObject().has("force")){
                force = GsonHelper.getAsBoolean(json.getValue().getAsJsonObject(), "force");
            }
            if (json.getValue().getAsJsonObject().has("slot")){
                slot = GsonHelper.getAsInt(json.getValue().getAsJsonObject(), "slot");
            }

            shopItems.add(new ShopItem(ShapedRecipe.itemStackFromJson(json.getValue().getAsJsonObject().getAsJsonObject("reward")), GsonHelper.getAsInt((JsonObject) json.getValue(), "cost"), json.getKey(), force, slot));
        }
    }
}
