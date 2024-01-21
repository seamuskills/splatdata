package com.seamus.splatdata;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seamus.splatdata.datapack.StageData;
import com.seamus.splatdata.datapack.StageDataListener;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.commands.SetSpawnCommand;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.splatcraft.forge.blocks.SpawnPadBlock;
import net.splatcraft.forge.commands.ScanTurfCommand;
import net.splatcraft.forge.data.Stage;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfo;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfoCapability;
import net.splatcraft.forge.items.remotes.InkDisruptorItem;
import net.splatcraft.forge.items.remotes.RemoteItem;
import net.splatcraft.forge.items.remotes.TurfScannerItem;
import net.splatcraft.forge.tileentities.SpawnPadTileEntity;
import net.splatcraft.forge.util.ColorUtils;

import java.util.*;

public class Match {
    public int timeLeft = -1;
    public int timeToStart = -1;

    public int cutsceneTime = -1;
    public Stage stage;
    public Collection<ServerPlayer> players;
    public List<String> teams;
    public UUID id;

    public String stageID;
    public ServerLevel level;
    public boolean inProgress;
    public CustomBossEvent bossbar;
    public enum matchStates{intro, gameplay, ending}
    public matchStates currentState = matchStates.intro;
    public RemoteItem.RemoteResult results;
    public Match(ArrayList<ServerPlayer> p, ServerLevel l, UUID matchid){
        players = p;
        id = matchid;
        level = l;
        inProgress = false;
        CustomBossEvents bossEvents = level.getServer().getCustomBossEvents();
        bossbar = bossEvents.create(new ResourceLocation("splatdata", id.toString()), new TextComponent("[if you can see this report it please]"));
        bossbar.setVisible(true);
        bossbar.setColor(BossEvent.BossBarColor.GREEN);
    }

    public void closeMatch(){
        CustomBossEvents bossEvents = level.getServer().getCustomBossEvents();
        bossbar.setPlayers(new ArrayList<ServerPlayer>()); //if I don't do this, the bossbar will stay on the player's screen even if I destroy it...
        bossEvents.remove(bossbar);
        WorldCaps.get(level).activeMatches.remove(this.id);
    }
    //    private static int createBar(CommandSourceStack p_136592_, ResourceLocation p_136593_, Component p_136594_) throws CommandSyntaxException {
//        CustomBossEvents $$3 = p_136592_.getServer().getCustomBossEvents();
//        if ($$3.get(p_136593_) != null) {
//            throw ERROR_ALREADY_EXISTS.create(p_136593_.toString());
//        } else {
//            CustomBossEvent $$4 = $$3.create(p_136593_, ComponentUtils.updateForEntity(p_136592_, p_136594_, (Entity)null, 0));
//            p_136592_.sendSuccess(new TranslatableComponent("commands.bossbar.create.success", new Object[]{$$4.getDisplayName()}), true);
//            return $$3.getEvents().size();
//        }
//    }

    public boolean setStage(String stageid){
        SaveInfo saveInfo = SaveInfoCapability.get(level.getServer());
        if (saveInfo.getStages().containsKey(stageid)){
            stageID = stageid;
            stage = saveInfo.getStages().get(stageID);
            teams = stage.getTeamIds().stream().toList();
            return true;
        }else{
            return false;
        }
    }

    public Match(ArrayList<ServerPlayer> p, ServerLevel l) {
        this( p, l, UUID.randomUUID());
    }

    public void update(){
        bossbar.setPlayers(players);
        if (inProgress){
            switch (currentState){
                case intro:
                    introCutscene();
                    break;
                case gameplay:
                    gameplay();
                    break;
                case ending:
                    endingCutscene();
                    break;
            }
        }else{
            notStarted();
        }
    }

    private void gameplay(){
        bossbar.setMax((int)(Config.Data.matchTime.get() * 20));
        bossbar.setValue(timeLeft);
        if (timeLeft == -1){
            //start of match code
            timeLeft = (int)(Config.Data.matchTime.get() * 20);
        }else if (timeLeft > 0){
            //mid-match code
            bossbar.setName(new TextComponent("Time Left: " + ((timeLeft / 1200) % 60) + ":" + ((timeLeft / 20) % 60)));
        }else{
            //end Match code
            currentState = matchStates.ending;
            System.out.println("gameplay ending");
        }
        timeLeft--;
    }

    private void endingCutscene(){
        if (cutsceneTime == -1) {
            for (ServerPlayer player : players) {
                if (Capabilities.hasCapability(player)) Capabilities.get(player).respawnTimeTicks = -1;
                player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
                bossbar.setVisible(false);
            }
            results = TurfScannerItem.scanTurf(level, level, stage.cornerA, stage.cornerB, 1, new ArrayList<>());
            broadcast(results.getOutput());
            cutsceneTime = (int)(Config.Data.introLength.get() * 20);
        }else if(cutsceneTime > 0){
            //middle code to be added
        }else{
            currentState = matchStates.intro;
            inProgress = false;
            for (ServerPlayer player : players){
                CapInfo playerCaps = Capabilities.get(player);
                playerCaps.lobbyStatus = CapInfo.lobbyStates.notReady;
                player.setGameMode(GameType.ADVENTURE);

                BlockPos spawn = WorldInfo.getSpawn(player);
                player.teleportTo(spawn.getX() + 0.5, spawn.getY() + SplatcraftData.blockHeight(spawn, level), spawn.getZ() + 0.5);
                player.setRespawnPosition(Level.OVERWORLD,spawn, 0, true, false);
            }
        }
        cutsceneTime--;
    }

    private void introCutscene(){
        if (cutsceneTime == -1) {
            broadcast(StageDataListener.stages.get(stageID).displayName);
            broadcast(new TextComponent("by ").append(StageDataListener.stages.get(stageID).author));
            bossbar.setVisible(false);
            cutsceneTime = (int)(Config.Data.introLength.get() * 20);
            for (ServerPlayer player : players){
                player.setGameMode(GameType.SPECTATOR);
            }
        }else if (cutsceneTime > 0){
            for (ServerPlayer player : players){
                Vec3 angle = player.getLookAngle().multiply(0.1, 0.1, 0.1);
                player.teleportTo(player.position().x + angle.x, player.position().y + angle.y, player.position().z + angle.z);
            }
        }else{
            for (ServerPlayer player : players){
                if (Capabilities.get(player).lobbyStatus != CapInfo.lobbyStates.spectator)
                    player.setGameMode(GameType.ADVENTURE);
            }
            System.out.println("ended");
            warpPlayers(stageID, players, false);
            currentState = matchStates.gameplay;
            bossbar.setVisible(true);
        }
        cutsceneTime--;
    }

    private void notStarted(){
        //if there are no players that aren't ready or spectating
        List<ServerPlayer> notReady = players.stream().filter((player) -> {
            if (Capabilities.hasCapability(player)) {
                CapInfo caps = Capabilities.get(player);
                return caps.lobbyStatus != CapInfo.lobbyStates.ready && caps.lobbyStatus != CapInfo.lobbyStates.spectator;
            }else{
                return true;
            }
        }).toList();

        if (notReady.isEmpty()){
            bossbar.setName(new TextComponent("Everyone is ready!"));
            bossbar.setMax((int)(Config.Data.readyToStartTime.get() * 20));
            bossbar.setValue(timeToStart);
            if (timeToStart == -1){
                timeToStart = (int)(Config.Data.readyToStartTime.get() * 20);
            }else if (timeToStart == 0){
                startGame();
            }
            timeToStart--;
        }else{
            bossbar.setName(new TextComponent("players ready to begin: " + (players.size() - notReady.size()) + "/" + (players.size())));
            bossbar.setMax(players.size());
            bossbar.setValue(players.size() - notReady.size());
            timeToStart = -1;
        }
    }

    public void stalk(ServerPlayer p){
        players.add(p);
        CapInfo caps = Capabilities.get(p);
        caps.match = id;
    }

    public void excommunicate(ServerPlayer p, boolean tp){
        players.remove(p);
        Stage lobby = SaveInfoCapability.get(level.getServer()).getStages().get(Config.Data.stageName.get());
        if (!(new AABB(lobby.cornerA, lobby.cornerB).expandTowards(1, 1, 1).contains(p.position())) && (tp || inProgress)){
            BlockPos spawn = WorldInfo.getSpawn(p);
            p.teleportTo(spawn.getX(), spawn.getY()+SplatcraftData.blockHeight(spawn, p.getLevel()),spawn.getZ());
            p.setGameMode(GameType.ADVENTURE);
        }
        CapInfo caps = Capabilities.get(p);
        caps.lobbyStatus = CapInfo.lobbyStates.out;
        caps.match = null;
        if (players.isEmpty()){
            closeMatch();
        }
    }

    public void broadcast(Component component){
        for (Player p : players){
            p.sendMessage(component, p.getUUID());
        }
    }

    public void excommunicate(ServerPlayer p){
        excommunicate(p, false);
    }

    private boolean warpPlayers(String stageId, Collection<ServerPlayer> targets, boolean setSpawn){ //totally not yoinked from splatcraft...
        HashMap<String, Stage> stages = SaveInfoCapability.get(level.getServer()).getStages();

        if (!stages.containsKey(stageId))
            return false;

        Stage stage = stages.get(stageId);
        Level stageLevel = level.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, stage.dimID));


        BlockPos blockpos2 = new BlockPos(Math.min(stage.cornerA.getX(), stage.cornerB.getX()), Math.min(stage.cornerB.getY(), stage.cornerA.getY()), Math.min(stage.cornerA.getZ(), stage.cornerB.getZ()));
        BlockPos blockpos3 = new BlockPos(Math.max(stage.cornerA.getX(), stage.cornerB.getX()), Math.max(stage.cornerB.getY(), stage.cornerA.getY()), Math.max(stage.cornerA.getZ(), stage.cornerB.getZ()));

        HashMap<Integer, ArrayList<SpawnPadTileEntity>> spawnPads = new HashMap<>();

        for (int x = blockpos2.getX(); x <= blockpos3.getX(); x++)
            for (int y = blockpos2.getY(); y <= blockpos3.getY(); y++)
                for (int z = blockpos2.getZ(); z <= blockpos3.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (stageLevel.getBlockEntity(pos) instanceof SpawnPadTileEntity) {
                        SpawnPadTileEntity te = (SpawnPadTileEntity) stageLevel.getBlockEntity(pos);

                        if(!spawnPads.containsKey(te.getColor()))
                            spawnPads.put(te.getColor(), new ArrayList<>());
                        spawnPads.get(te.getColor()).add(te);
                    }
                }

        if(spawnPads.isEmpty())
            return false;

        HashMap<Integer, Integer> playersTeleported = new HashMap<>();
        for (ServerPlayer player : targets) {
            int playerColor = ColorUtils.getPlayerColor(player);

            if (spawnPads.containsKey(playerColor)) {
                if (!playersTeleported.containsKey(playerColor))
                    playersTeleported.put(playerColor, 0);

                SpawnPadTileEntity te = spawnPads.get(playerColor).get(playersTeleported.get(playerColor) % spawnPads.get(playerColor).size());

                float pitch = te.getLevel().getBlockState(te.getBlockPos()).getValue(SpawnPadBlock.DIRECTION).toYRot();

                if (stageLevel == player.level)
                    player.connection.teleport(te.getBlockPos().getX() + .5, te.getBlockPos().getY() + .5, te.getBlockPos().getZ() + .5, pitch, 0);
                else
                    player.teleportTo((ServerLevel) stageLevel, te.getBlockPos().getX() + .5, te.getBlockPos().getY() + .5, te.getBlockPos().getZ(), pitch, 0);

                if(setSpawn)
                    player.setRespawnPosition(player.level.dimension(), te.getBlockPos(), player.level.getBlockState(te.getBlockPos()).getValue(SpawnPadBlock.DIRECTION).toYRot(), false, true);

                playersTeleported.put(playerColor, playersTeleported.get(playerColor) + 1);
            }
        }

        int result = 0;
        for(int i : playersTeleported.values())
            result += i;

        return result != 0;
    }

    public void startGame(){
        currentState = matchStates.intro;

        HashMap<String, StageData> validStages = StageDataListener.stages;

        for (Match match : WorldCaps.get(level).activeMatches.values()){
            if (match.id == id || match.stageID.isEmpty()){ continue;}
            validStages.remove(match.stageID);
        }

        ArrayList<String> votes = new ArrayList<>(players.stream().map((p) -> {
            return Capabilities.get(p).vote;
        }).filter(validStages::containsKey).toList());

        if (validStages.isEmpty()){
            broadcast(new TextComponent("All stages are currently in use! Please wait and try again later!").withStyle(ChatFormatting.RED));
            return;
        }

        boolean validStage = false;
        if (votes.size() / players.size() >= Config.Data.varietyRequirement.get() / 100){
            validStage = setStage(votes.get(level.random.nextInt(votes.size())));
        }else{
            broadcast(new TextComponent("Not enough players have a valid vote, choosing randomly from all maps").withStyle(ChatFormatting.YELLOW));
            validStage = setStage((String) validStages.keySet().toArray()[level.random.nextInt(validStages.size())]);
        }

        if (!(validStages.containsKey(stageID))){ //quick last check, this should never fire.
            validStage = false;
        }

        if (!validStage){
            broadcast(new TextComponent("Invalid stage selected! Please contact admin!").withStyle(ChatFormatting.RED));
            return; //can't start with no stage
        }

        int teamAssign = 0;
        for (ServerPlayer player : players) {
            CapInfo caps = Capabilities.get(player);
            player.setHealth(player.getMaxHealth());
            switch (caps.lobbyStatus) {
                case out:
                    throw new RuntimeException("Someone outside the lobby was queued!");
                case notReady:
                    throw new RuntimeException("Someone was not ready!");
                case ready:
                    caps.team = teams.get(teamAssign);
                    teamAssign = (teamAssign + 1) % teams.size();
                    ColorUtils.setPlayerColor(player, stage.getTeamColor(caps.team));
                    break;
                case spectator:
                    ColorUtils.setPlayerColor(player, 0xffffff);
                    caps.team = "spec";
                    break;
            }
        }
        List<ServerPlayer> noSpectators = players.stream().filter((p) -> {return !p.isSpectator();}).toList();
        warpPlayers(stageID, noSpectators, true);
        for (ServerPlayer player : players.stream().filter(ServerPlayer::isSpectator).toList()){
            ServerPlayer target = noSpectators.get(level.random.nextInt(noSpectators.size()));
            player.teleportTo(target.position().x, target.position().y, target.position().z);
        }

        InkDisruptorItem.clearInk(level, stage.cornerA, stage.cornerB);

        inProgress = true;
    }

//    public CompoundTag writeNBT(CompoundTag compoundTag) {
//        //finish this
//        ListTag ids = new ListTag();
//        for (Player player : players){
//            IntArrayTag currentID = NbtUtils.createUUID(player.getUUID());
//            ids.add(players.indexOf(player), currentID);
//        }
//        compoundTag.putInt("time",timeLeft);
//        compoundTag.putString("stage", stageID);
//        compoundTag.put("players", ids);
//        compoundTag.putUUID("id",id);
//        return compoundTag;
//    }
//
//    public static Match readNBT(CompoundTag nbt , Level owner) {
//        ServerLevel level = (ServerLevel)owner;
//        if (level == null) throw new NullPointerException("Null level!");
//        ListTag ids = nbt.getList("players", Tag.TAG_COMPOUND);
//        ArrayList<Player> players = new ArrayList<Player>();
//        for (Tag id : ids){
//            ServerPlayer p = (ServerPlayer)level.getPlayerByUUID(NbtUtils.loadUUID(id));
//            if (p == null){
//                continue;
//            }
//            players.add(p);
//        }
//        return new Match(nbt.getString("stage"), players, level, nbt.getUUID("id"));
//    }

    public boolean playerInvolved(Player player){
        return players.contains(player);
    }
}
