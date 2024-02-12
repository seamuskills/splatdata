package com.seamus.splatdata.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.splatcraft.forge.registries.SplatcraftInkColors;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ColorListener extends SimpleJsonResourceReloadListener {
    private static final Gson gsonInstance = Deserializers.createFunctionSerializer().create();
    private static final String stagesPath = "color_menu";

    public static final HashMap<ItemStack, int[]> filters = new HashMap<>();

    public ColorListener() {
        super(gsonInstance, stagesPath);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> json: resourceMap.entrySet()) {
            List<Integer> colors = new ArrayList<>();

            for (JsonElement color : GsonHelper.getAsJsonArray(json.getValue().getAsJsonObject(), "colors")) {
                if (GsonHelper.isNumberValue(color)) {
                    colors.add(color.getAsInt());
                    continue;
                }

                String colorStr = color.getAsString();
                int colorInt = 0;
                if (colorStr.indexOf("#") == 0) {
                    colorInt = Integer.parseInt(colorStr.replace("#", ""), 16);
                } else {
                    ResourceLocation loc = new ResourceLocation(colorStr);
                    if (SplatcraftInkColors.REGISTRY.get().containsKey(loc)) {
                        colorInt = SplatcraftInkColors.REGISTRY.get().getValue(loc).getColor();
                    }
                }
                colors.add(colorInt);
            }

            filters.put(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json.getValue().getAsJsonObject(), "icon")), Arrays.stream(colors.toArray(new Integer[0])).mapToInt(i -> i).toArray());
        }
    }
}