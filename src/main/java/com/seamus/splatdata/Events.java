package com.seamus.splatdata;

import com.seamus.splatdata.commands.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.splatcraft.forge.registries.SplatcraftItems;

import java.util.*;

@Mod.EventBusSubscriber(modid="splatdata")
public class Events {
    private final static HashMap<UUID, double[]> deathPos = new HashMap<>();

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event){
        new SpawnCommand(event.getDispatcher());
        new RoomCommand(event.getDispatcher());
        new ChangePrefColorCommand(event.getDispatcher());
        new MenuCommand(event.getDispatcher());
        new SetSpawnCommand(event.getDispatcher());
        new VoteCommand(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event){
        event.register(CapInfo.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player)
            event.addCapability(new ResourceLocation("splatdata", "data"), new Capabilities());
    }

    @SubscribeEvent
    public static void attachWorldCaps(AttachCapabilitiesEvent<Level> event){
        if (event.getObject().dimension() != Level.OVERWORLD){
            return;
        }
        WorldCaps caps = new WorldCaps();
        event.addCapability(new ResourceLocation("splatdata", "leveldata"), caps);
        WorldInfo info = caps.getCapability(WorldCaps.CAPABILITY).orElseThrow(NullPointerException::new);

        try{
            info.owner = event.getObject();
        }catch(NullPointerException npe){
            System.out.println("oops! all errors!");
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event){
        if (event.phase == TickEvent.Phase.START){
            return;
        }
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
        if (level == null) return;
        WorldInfo info = WorldCaps.get(level);
        for (Match match : info.activeMatches.values()){
                match.update();
        }
    }

    @SubscribeEvent
    public static void serverEnd(ServerStoppingEvent event){
        for (Match match : WorldCaps.get(event.getServer().getLevel(Level.OVERWORLD)).activeMatches.values()){
            match.closeMatch();
        }
    }

    @SubscribeEvent
    public static void serverStart(ServerStartedEvent event){
        Collection<CustomBossEvent> bossEvents = event.getServer().getCustomBossEvents().getEvents();
        bossEvents.removeIf(bossEvent -> bossEvent.getTextId().getNamespace().equals("splatdata"));
    }

    //figure out later, invisible players to replace spectator mode
//    @SubscribeEvent
//    public static void playerRenderStart(RenderPlayerEvent.Pre event){
//        if (!Capabilities.hasCapability(event.getPlayer())) return;
//        System.out.println("render event");
//        Player player = event.getPlayer();
//        CapInfo info = Capabilities.get(player);
//        if (info.inMatch()){
//            Match.matchStates state = WorldCaps.get(player.level).activeMatches.get(info.match).currentState;
//            System.out.println(state);
//            if (state == Match.matchStates.intro || state == Match.matchStates.ending){
//                event.setCanceled(true);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void useItem(PlayerInteractEvent.RightClickItem event){
//        if (!(event.getEntity() instanceof Player player)) return;
//        if (!Capabilities.hasCapability(player)) return;
//        CapInfo info = Capabilities.get(player);
//        if (info.inMatch()){
//            Match.matchStates state = WorldCaps.get(player.level).activeMatches.get(info.match).currentState;
//            if (state == Match.matchStates.intro || state == Match.matchStates.ending){
//                event.setCanceled(true);
//            }
//        }
//    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event){
        if (event.phase == TickEvent.Phase.START && event.player.level.isInWorldBounds(new BlockPos(event.player.position())))
            deathPos.put(event.player.getUUID(), new double[]{event.player.position().x, event.player.position().y, event.player.position().z, event.player.getXRot(), event.player.getYHeadRot()});
        if (event.player.level.isClientSide || event.phase == (TickEvent.Phase.END)){
            return;
        }
        if (Config.Data.forceInkTanks.get()){
            Set<Item> jrWeapons = new HashSet<>();
            jrWeapons.add(SplatcraftItems.splattershotJr.get());
            jrWeapons.add(SplatcraftItems.kensaSplattershotJr.get());
            if (event.player.getInventory().hasAnyOf(jrWeapons) && !event.player.getInventory().getItem(102).sameItem(new ItemStack(SplatcraftItems.inkTankJr.get()))){
                event.player.getInventory().setItem(102, new ItemStack(SplatcraftItems.inkTankJr.get()));
            }else if (!event.player.getInventory().getItem(102).sameItem(new ItemStack(SplatcraftItems.inkTank.get()))){
                event.player.getInventory().setItem(102, new ItemStack(SplatcraftItems.classicInkTank.get()));
            }
        }
        if (Capabilities.hasCapability(event.player)){
            CapInfo capInfo = Capabilities.get(event.player);
            WorldInfo worldCaps = WorldCaps.get(event.player.level);

            if (!(worldCaps.activeMatches.containsKey(capInfo.match)) && capInfo.inMatch()){
                capInfo.match = null;
                SpawnCommand.tpToSpawn((ServerPlayer) event.player, true, true);
                event.player.sendMessage(new TextComponent("A communication error has occurred.").withStyle(ChatFormatting.RED), event.player.getUUID());
                capInfo.lobbyStatus = CapInfo.lobbyStates.out;
            }else if (worldCaps.activeMatches.containsKey(capInfo.match)){
                if (!worldCaps.activeMatches.get(capInfo.match).players.contains(event.player.getUUID())){
                    capInfo.match = null;
                    SpawnCommand.tpToSpawn((ServerPlayer) event.player, true, true);
                    event.player.sendMessage(new TextComponent("A communication error has occurred.").withStyle(ChatFormatting.RED), event.player.getUUID());
                    capInfo.lobbyStatus = CapInfo.lobbyStates.out;
                }
            }

            //lobby code
//            if (lobby != null){
//                if (new AABB(lobby.cornerA, lobby.cornerB).expandTowards(1, 1, 1).contains(event.player.position())){
//                    if (capInfo.lobbyStatus == CapInfo.lobbyStates.out) {
//                        capInfo.lobbyStatus = CapInfo.lobbyStates.notReady;
//                        event.player.sendMessage(new TextComponent("[ Welcome to the lobby! You have been marked as ").withStyle(ChatFormatting.DARK_GREEN).append(new TextComponent("not ready").withStyle(ChatFormatting.DARK_RED).append(new TextComponent(" ]").withStyle(ChatFormatting.DARK_GREEN))), event.player.getUUID());
//                    }
//                }else{
//                    if (capInfo.lobbyStatus != CapInfo.lobbyStates.out)
//                        event.player.sendMessage(new TextComponent("[ You have left the lobby ]").withStyle(ChatFormatting.DARK_RED), event.player.getUUID());
//                    capInfo.lobbyStatus = CapInfo.lobbyStates.out;
//                }
//            }

            //respawn timer code
            if (capInfo.respawnTimeTicks >= 0) {
                int respawnTime = (int) (Config.Data.respawnTime.get() * 20);
                if (capInfo.inMatch()) respawnTime = (int)(worldCaps.activeMatches.get(capInfo.match).gameType.respawnTime * 20);
                //if an admin sets their own gamemode (or someone elses) to non-spec I want the respawn timer to get out of the way.
                if (((ServerPlayer)event.player).gameMode.getGameModeForPlayer() != GameType.SPECTATOR){
                    capInfo.respawnTimeTicks = -1;
                    return;
                }
                capInfo.respawnTimeTicks--;
                if (capInfo.respawnTimeTicks < respawnTime * 0.7) {
                    event.player.displayClientMessage(new TextComponent("Respawn in " + ((capInfo.respawnTimeTicks / 20) + 1)), true);
                }else{
                    event.player.displayClientMessage(new TextComponent(Capabilities.get(event.player).deathMessage).withStyle(ChatFormatting.DARK_RED), true);
                }
            }
            if (capInfo.respawnTimeTicks == 0){
                //event.player.getServer().getPlayerList().respawn((ServerPlayer)event.player, false);
                BlockPos respawnPos = ((ServerPlayer) event.player).getRespawnPosition();
                if (respawnPos != null) {
                   //event.player.teleportTo(respawnPos.getX(), respawnPos.getY(), respawnPos.getZ());
                    ((ServerPlayer) event.player).connection.teleport(respawnPos.getX() + 0.5, respawnPos.getY() + SplatcraftData.blockHeight(respawnPos, event.player.getLevel()), respawnPos.getZ() + 0.5, ((ServerPlayer) event.player).getRespawnAngle(), 0.0f);
                    event.player.displayClientMessage(new TextComponent("Respawned!").withStyle(ChatFormatting.GREEN), true);
                }else{
                    event.player.displayClientMessage(new TextComponent("Respawn point null!").withStyle(ChatFormatting.RED), true);
                }
                ((ServerPlayer)event.player).setGameMode(capInfo.respawnGamemode);
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
    public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event){
        double[] oldPos = deathPos.get(event.getPlayer().getUUID());
        ServerPlayer serverPlayer = (ServerPlayer)event.getPlayer();
        serverPlayer.teleportTo((ServerLevel)serverPlayer.level,oldPos[0], oldPos[1], oldPos[2], (float)oldPos[4], (float)oldPos[3]);
    }

    @SubscribeEvent
    public static void playerDied(LivingDeathEvent event){
        if (!Capabilities.hasCapability(event.getEntityLiving()))
            return;
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
                return;

            DamageSource source = event.getSource();
            Entity killer = source.getEntity();

            CapInfo playerData = Capabilities.get(player);

            //repeated code bad I know, but I have to avoid a NullPointerException...
            if (killer != null){
                if (!killer.equals(player)) {
                    //player.sendMessage(new TextComponent("Splatted by ").withStyle(ChatFormatting.DARK_RED).append(killer.getName()), player.getUUID());
                    playerData.deathMessage = "Splatted by " + killer.getName().getString();
                }else {
                    //player.sendMessage(new TextComponent("Splatted Self.").withStyle(ChatFormatting.DARK_RED), player.getUUID());
                    playerData.deathMessage = "Splatted yourself.";
                }
            }else{
                //player.sendMessage(new TextComponent("Splatted Self.").withStyle(ChatFormatting.DARK_RED), player.getUUID());
                playerData.deathMessage = "Splatted yourself.";
            }

            playerData.respawnTimeTicks = (int)(Config.Data.respawnTime.get() * 20);
            playerData.respawnGamemode = player.gameMode.getGameModeForPlayer();
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
