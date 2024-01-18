package com.seamus.splatdata;

import com.seamus.splatdata.commands.*;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.splatcraft.forge.data.Stage;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfoCapability;
import net.splatcraft.forge.registries.SplatcraftCapabilities;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid="splatdata")
public class Events {
    private final static HashMap<UUID, double[]> deathPos = new HashMap<UUID, double[]>();

    private static Stage lobby;

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event){
        new SpawnCommand(event.getDispatcher());
        new RoomCommand(event.getDispatcher());
        new ChangePrefColorCommand(event.getDispatcher());
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
    public static void serverStart(ServerStartingEvent event){
        lobby = SaveInfoCapability.get(event.getServer()).getStages().get(Config.Data.stageName.get());
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event){
        if (event.phase == TickEvent.Phase.START)
            deathPos.put(event.player.getUUID(), new double[]{event.player.position().x, event.player.position().y, event.player.position().z, event.player.getXRot(), event.player.getYHeadRot()});
        if (event.player.level.isClientSide || event.phase == (TickEvent.Phase.END)){
            return;
        }
        if (Capabilities.hasCapability(event.player)){
            CapInfo capInfo = Capabilities.get(event.player);
            WorldInfo worldCaps = WorldCaps.get(event.player.level);

            if (!(worldCaps.activeMatches.containsKey(capInfo.match)) && capInfo.inMatch()){
                capInfo.match = null;
                BlockPos spawn = WorldInfo.getSpawn((ServerPlayer)event.player);
                event.player.teleportTo(spawn.getX(), spawn.getY(), spawn.getZ());
                event.player.sendMessage(new TextComponent("A communication error has occurred.").withStyle(ChatFormatting.RED), event.player.getUUID());
                capInfo.lobbyStatus = CapInfo.lobbyStates.out;
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
                //if an admin sets their own gamemode (or someone elses) to non-spec I want the respawn timer to get out of the way.
                if (((ServerPlayer)event.player).gameMode.getGameModeForPlayer() != GameType.SPECTATOR){
                    capInfo.respawnTimeTicks = -1;
                    return;
                }
                capInfo.respawnTimeTicks--;
                if (capInfo.respawnTimeTicks < (Config.Data.respawnTime.get() * 20) * 0.7) {
                    ((ServerPlayer) event.player).displayClientMessage(new TextComponent("Respawn in " + ((capInfo.respawnTimeTicks / 20) + 1)), true);
                }else{
                    ((ServerPlayer) event.player).displayClientMessage(new TextComponent(Capabilities.get(event.player).deathMessage).withStyle(ChatFormatting.DARK_RED), true);
                }
            }
            if (capInfo.respawnTimeTicks == 0){
                ((ServerPlayer)event.player).setGameMode(capInfo.respawnGamemode);
                //event.player.getServer().getPlayerList().respawn((ServerPlayer)event.player, false);
                BlockPos respawnPos = ((ServerPlayer) event.player).getRespawnPosition();
                if (respawnPos != null) {
                    BlockState respawnBlock = event.player.level.getBlockState(respawnPos);
                    VoxelShape blockShape = respawnBlock.getCollisionShape(event.player.level, respawnPos);
                    double blockHeight = !blockShape.isEmpty() ? blockShape.bounds().maxY : 0;
                   //event.player.teleportTo(respawnPos.getX(), respawnPos.getY(), respawnPos.getZ());
                    ((ServerPlayer) event.player).connection.teleport(respawnPos.getX() + 0.5, respawnPos.getY() + blockHeight, respawnPos.getZ() + 0.5, ((ServerPlayer) event.player).getRespawnAngle(), 0.0f);
                    event.player.displayClientMessage(new TextComponent("Respawned!").withStyle(ChatFormatting.GREEN), true);
                    //figure out rotation later ig idk
                }else{
                    event.player.displayClientMessage(new TextComponent("Respawn point null!").withStyle(ChatFormatting.RED), true);
                }
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
