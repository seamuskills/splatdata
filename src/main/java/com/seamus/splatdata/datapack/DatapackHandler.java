package com.seamus.splatdata.datapack;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DatapackHandler {
    public static final StageDataListener listener = new StageDataListener();
    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event){
        event.addListener(listener);
    }
}
