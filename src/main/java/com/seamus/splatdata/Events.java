package com.seamus.splatdata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.core.jmx.Server;

@Mod.EventBusSubscriber(modid="splatdata")
public class Events {
    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event){
        event.register(CapInfo.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player)
            event.addCapability(new ResourceLocation("splatdata", "respawn_data"), new Capabilities());
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event){
        if (event.player.level.isClientSide || event.phase == (TickEvent.Phase.END)){
            return;
        }
        if (Capabilities.hasCapability(event.player)){
            CapInfo capInfo = Capabilities.get(event.player);
            if (capInfo.respawnTimeTicks >= 0) {
                capInfo.respawnTimeTicks--;
                ((ServerPlayer)event.player).displayClientMessage(new TextComponent("Respawn in " + (capInfo.respawnTimeTicks / 20)),true);
            }
            if (capInfo.respawnTimeTicks == 0){
                ((ServerPlayer)event.player).setGameMode(capInfo.respawnGamemode);
                event.player.getServer().getPlayerList().respawn((ServerPlayer)event.player, false);
            }
        }
    }

    //why forge???
    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event){
        event.getOriginal().reviveCaps();
        if (!Capabilities.hasCapability(event.getOriginal()) || !event.isWasDeath())
            return;
        CapInfo newCaps = Capabilities.get(event.getPlayer());
        CapInfo oldCaps = Capabilities.get(event.getOriginal());

        newCaps.readNBT(oldCaps.writeNBT(new CompoundTag()));

        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void playerDied(LivingDeathEvent event){
        if (!Capabilities.hasCapability(event.getEntityLiving()))
            return;
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
                return;

            CapInfo playerData = Capabilities.get(player);
            playerData.respawnTimeTicks = 140;
            playerData.respawnGamemode = player.gameMode.getGameModeForPlayer();
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
