package com.seamus.splatdata;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

import java.util.Arrays;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("splatdata")
public class SplatcraftData {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public SplatcraftData() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.configInstance);
        Config.loadConfig(Config.configInstance, FMLPaths.CONFIGDIR.get().resolve("splatdata.toml").toString());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    }

    public static double blockHeight(BlockPos block, Level level){
        VoxelShape shape = level.getBlockState(block).getShape(level, block);
        if (shape.isEmpty()){
            return 0;
        }else {
            return shape.bounds().getYsize();
        }
    }

    public static ItemStack applyLore(ItemStack item, Component loreComponent){
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(loreComponent)));
        item.getOrCreateTagElement("display").put("Lore", lore);
        return item;
    }
    public static ItemStack applyLoreArray(ItemStack item, Component[] loreArray){
        ListTag lore = new ListTag();
        Arrays.stream(loreArray).forEach((c) -> {lore.add(StringTag.valueOf(Component.Serializer.toJson(c)));});
        item.getOrCreateTagElement("display").put("Lore", lore);
        return item;
    }
}
