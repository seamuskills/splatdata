package com.seamus.splatdata.datapack;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DatapackHandler {
    public static final StageDataListener stageListener = new StageDataListener();
    public static final ShopDataListener shopListener = new ShopDataListener();
    public static final GameTypeListener gameTypeListener = new GameTypeListener();
    public static final ColorListener colorListener = new ColorListener();
    public static final CreditsListener creditsListener = new CreditsListener();
    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event){
        event.addListener(stageListener);
        event.addListener(shopListener);
        event.addListener(gameTypeListener);
        event.addListener(colorListener);
        event.addListener(creditsListener);
    }
}
