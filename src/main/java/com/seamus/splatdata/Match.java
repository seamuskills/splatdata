package com.seamus.splatdata;

import com.seamus.splatdata.commands.SpawnCommand;
import com.seamus.splatdata.datapack.GameTypeListener;
import com.seamus.splatdata.datapack.MatchGameType;
import com.seamus.splatdata.datapack.StageData;
import com.seamus.splatdata.datapack.StageDataListener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.splatcraft.forge.blocks.SpawnPadBlock;
import net.splatcraft.forge.data.Stage;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfo;
import net.splatcraft.forge.data.capabilities.saveinfo.SaveInfoCapability;
import net.splatcraft.forge.items.remotes.ColorChangerItem;
import net.splatcraft.forge.items.remotes.InkDisruptorItem;
import net.splatcraft.forge.items.remotes.TurfScannerItem;
import net.splatcraft.forge.registries.SplatcraftCommands;
import net.splatcraft.forge.tileentities.SpawnPadTileEntity;
import net.splatcraft.forge.util.ColorUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Match {
    public int timeLeft = -1;
    public int timeToStart = -1;

    public int cutsceneTime = -1;
    public Stage stage;
    public ArrayList<UUID> players;
    public List<String> teams;
    public UUID id;

    public String stageID;
    public ServerLevel level;
    public boolean inProgress;
    public CustomBossEvent bossbar;
    public enum matchStates{intro, gameplay, ending}
    public matchStates currentState = matchStates.intro;
    public TurfScannerItem.TurfScanResult results;
    public TreeMap<Integer, Integer> scores;
    public MatchGameType matchGameType;
    public String splatWinner = "";
    public UUID host;
    public int[] password = {};
    public Match(ServerPlayer p, ServerLevel l, UUID matchid){
        matchGameType = GameTypeListener.gameTypes.get("splatdata:turfwar");
        id = matchid;
        players = new ArrayList<>();
        stalk(p);
        host = p.getUUID();
        level = l;
        inProgress = false;
        CustomBossEvents bossEvents = level.getServer().getCustomBossEvents();
        bossbar = bossEvents.create(new ResourceLocation("splatdata", id.toString()), new TextComponent("[if you can see this report it please]"));
        bossbar.setVisible(true);
        bossbar.setColor(BossEvent.BossBarColor.GREEN);
    }
    public Match(ServerPlayer p, ServerLevel l) {
        this( p, l, UUID.randomUUID());
    }

    public void closeMatch(){
        CustomBossEvents bossEvents = level.getServer().getCustomBossEvents();
        bossbar.setPlayers(new ArrayList<ServerPlayer>()); //if I don't do this, the bossbar will stay on the player's screen even if I destroy it...
        bossEvents.remove(bossbar);
        WorldCaps.get(level).activeMatches.remove(this.id);
//        ChunkPos pos1 = level.getChunkAt(stage.cornerA).getPos();
//        ChunkPos pos2 = level.getChunkAt(stage.cornerB).getPos();
//        for (int x = 0; x < Math.abs(pos2.x - pos1.x); x++){
//            for (int z = 0; z < Math.abs(pos2.z - pos1.z); z++){
//                ForgeChunkManager.forceChunk(level, "splatdata", stage.cornerA, x, z, false, false);
//            }
//        }
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
            teams = new ArrayList<>(stage.getTeamIds());
            teams.removeIf(StageDataListener.stages.get(stageID).ignoreTeams::contains);
            return true;
        }else{
            return false;
        }
    }

    public List<ServerPlayer> getPlayerList(Boolean rand){
        List<Player> playerlist = new ArrayList<>(players.stream().map(level::getPlayerByUUID).toList());
        if (rand) Collections.shuffle(playerlist);
        playerlist = playerlist.stream().filter(Objects::nonNull).toList();
        return new ArrayList<>(playerlist.stream().map((entity) -> {return (ServerPlayer)entity;}).toList());
    }

    public List<ServerPlayer> getPlayerList(){
        return getPlayerList(false);
    }

    public void setTeamColors(){
        float offset = level.random.nextFloat();
        for (int teamIndex = 0; teamIndex < teams.size(); teamIndex++){
            float h = (teamIndex / (float)teams.size()); // (index / number of teams + 1) divide hue evenly between teams, add 1 to number of teams so the 2 outer most teams in the color space can't overlap.
            int color = Color.HSBtoRGB((offset + h) % 1, 1, 1);
            color &= 0x00ffffff; //cause the color it returns has an extra FF at the beginning
            color = Math.floorMod(color, 0xffffff);
            //Level level, BlockPos from, BlockPos to, int color, int mode, int affectedColor, String stage, String affectedTeam
            ColorChangerItem.replaceColor(level, stage.cornerA, stage.cornerB, color, 1, stage.getTeamColor(teams.get(teamIndex)), stageID, teams.get(teamIndex));
            //stage.setTeamColor(teams.get(teamIndex), color);
        }
    }

    public void update(){
        players.removeIf((u) -> {return level.getPlayerByUUID(u) == null;});
        if (players.isEmpty()){
            closeMatch();
            return;
        }

        bossbar.setPlayers(getPlayerList());
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
        //wave respawn code
        if (matchGameType.rMode == MatchGameType.respawnMode.wave || matchGameType.rMode == MatchGameType.respawnMode.waveOrTimed) {
            for (String t : teams) {
                List<ServerPlayer> p = getPlayerList();
                p.removeIf((player) -> ColorUtils.getPlayerColor(player) != stage.getTeamColor(t)); //remove all non-team members
                p.removeIf((player) -> Capabilities.get(player).respawnTimeTicks >= 0); //remove all dead players
                if (p.isEmpty()) {
                    for (Player player : p) {
                        CapInfo info = Capabilities.get(player);
                        info.waveRespawning = true;
                        if (matchGameType.rMode == MatchGameType.respawnMode.waveOrTimed){
                            info.respawnTimeTicks = 0;
                        }
                    }
                }
            }
        }

        bossbar.setMax((int)(Config.Data.matchTime.get() * 20));
        if (matchGameType != null) bossbar.setMax((int)matchGameType.matchTime);
        bossbar.setValue(timeLeft);
        if (timeLeft == -1){
            //start of match code
            timeLeft = (int)(Config.Data.matchTime.get() * 20);
            if (matchGameType != null) timeLeft = (int) matchGameType.matchTime;
        }else if (timeLeft > 0){
            //mid-match code
            bossbar.setName(new TextComponent("Time Left: " + ((timeLeft / 1200) % 60) + ":" + ((timeLeft / 20) % 60)));
        }else{
            //end Match code
            currentState = matchStates.ending;
        }
        timeLeft--;
    }

    private void endingCutscene(){
        if (cutsceneTime == -1) {
            for (ServerPlayer player : getPlayerList()) {
                Capabilities.get(player).respawnTimeTicks = -1;
                player.setGameMode(GameType.SPECTATOR);
                bossbar.setVisible(false);
            }
            switch(matchGameType.wCondition) {
                case turf:
                    results = TurfScannerItem.scanTurf(level, level, stage.cornerA, stage.cornerB, 1, getPlayerList());
                    scores = results.getScores();
                    break;
                case splats:
                    splatWinner = "Tie!";
                    int highest = 0;
                    for (String t : teams){
                        List<ServerPlayer> players = getPlayerList();
                        players.removeIf((p) -> ColorUtils.getPlayerColor(p) != stage.getTeamColor(t)); //todo fix, null error donno why
                        int splatCount = 0;
                        for (Player p : players){
                            splatCount += Capabilities.get(p).matchSplats;
                        }
                        if (splatCount > highest){
                            splatWinner = t;
                            highest = splatCount;
                        }else if (splatCount == highest){
                            splatWinner = "Tie!";
                        }
                    }
                    break;
            }
            cutsceneTime = (int) (Config.Data.introLength.get() * 20);
        }else if(cutsceneTime > 0) {
            TextComponent scoreText = new TextComponent("");
            switch (matchGameType.wCondition) {
                case turf:
                    for (Map.Entry<Integer, Integer> score : scores.entrySet()) {
                        scoreText.append(new TextComponent((int) (((float) score.getValue() / (float) results.getScanVolume()) * 100) + "%" + " ").withStyle(Style.EMPTY.withColor(score.getKey())));
                    }
                    break;
                case splats:
                    boolean validWinner = teams.contains(splatWinner);
                    Style color = Style.EMPTY.withColor(0xffffff);
                    if (validWinner) color = Style.EMPTY.withColor(stage.getTeamColor(splatWinner));
                    scoreText = (TextComponent) new TextComponent(splatWinner).withStyle(color);
            }
            for (Player player : getPlayerList()){
                player.displayClientMessage(scoreText, true);
            }
        }else{
            currentState = matchStates.intro;
            inProgress = false;
            for (ServerPlayer player : getPlayerList()){
                CapInfo playerCaps = Capabilities.get(player);
                playerCaps.lobbyStatus = CapInfo.lobbyStates.notReady;

                SpawnCommand.tpToSpawn(player, true, true);
                playerCaps.cash += Config.Data.cashPayout.get();
                switch (matchGameType.wCondition) {
                    case turf:
                        if (ColorUtils.getPlayerColor(player) == results.getCommandResult()) {
                            playerCaps.cash += Config.Data.winBonus.get();
                        }
                        break;
                    case splats:
                        if (teams.contains(splatWinner)){
                            if (ColorUtils.getPlayerColor(player) == stage.getTeamColor(splatWinner)){
                                playerCaps.cash += Config.Data.winBonus.get();
                            }
                        }
                        break;
                }
                ColorUtils.setPlayerColor(player, playerCaps.preferredColor);
            }
            bossbar.setVisible(true);

            ChunkPos pos1 = level.getChunkAt(stage.cornerA).getPos();
            ChunkPos pos2 = level.getChunkAt(stage.cornerB).getPos();
            for (int x = 0; x < Math.abs(pos2.x - pos1.x); x++){
                for (int z = 0; z < Math.abs(pos2.z - pos1.z); z++){
                    ForgeChunkManager.forceChunk(level, "splatdata", stage.cornerA, x, z, false, false);
                }
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
            for (ServerPlayer player : getPlayerList()){
                player.setGameMode(GameType.SPECTATOR);
                CapInfo caps = Capabilities.get(player);
                caps.respawnTimeTicks = -1;
                caps.matchSplats = 0;
            }
        }else if (cutsceneTime > 0){
            //middle stuff
        }else{
            for (ServerPlayer player : getPlayerList()){
                if (Capabilities.get(player).lobbyStatus != CapInfo.lobbyStates.spectator)
                    player.setGameMode(GameType.ADVENTURE);
            }
            warpPlayers(stageID, getPlayerList(), false);
            currentState = matchStates.gameplay;
            bossbar.setVisible(true);
        }
        cutsceneTime--;
    }

    private void notStarted(){
        //if there are no players that aren't ready or spectating
        List<ServerPlayer> notReady = getPlayerList().stream().filter((player) -> {
                CapInfo caps = Capabilities.get(player);
                return caps.lobbyStatus != CapInfo.lobbyStates.ready && caps.lobbyStatus != CapInfo.lobbyStates.spectator;
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
        players.add(p.getUUID());
        CapInfo caps = Capabilities.get(p);
        caps.match = id;
    }

    public void excommunicate(ServerPlayer p, boolean tp){
        players.remove(p.getUUID());
        CapInfo caps = Capabilities.get(p);
        caps.lobbyStatus = CapInfo.lobbyStates.out;
        caps.match = null;
        caps.waveRespawning = false;
        ColorUtils.setPlayerColor(p, caps.preferredColor);
        if (tp || inProgress) SpawnCommand.tpToSpawn(p, true, true);
        if (getPlayerList().isEmpty()){
            closeMatch();
        }else if (host == p.getUUID()){
            host = players.get(p.level.random.nextInt(players.size()));
            broadcast(new TextComponent("Host left the match. New host chosen: ").append(level.getPlayerByUUID(host).getName()));
        }
    }

    public void broadcast(Component component){
        for (Player p : getPlayerList()){
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

        ArrayList<String> votes = new ArrayList<>(getPlayerList().stream().map((p) -> {
            return Capabilities.get(p).vote;
        }).filter((s) -> {return validStages.containsKey(s) || s.equals("Random");}).toList());

        if (validStages.isEmpty()){
            broadcast(new TextComponent("All stages are currently in use! Please wait and try again later!").withStyle(ChatFormatting.RED));
            return;
        }

        boolean validStage;
        if (votes.size() / players.size() >= Config.Data.varietyRequirement.get() / 100 && !votes.isEmpty()){
            String vote = votes.get(level.random.nextInt(votes.size()));
            if (vote.equals("Random")) vote = validStages.get((String)validStages.keySet().toArray()[level.random.nextInt(validStages.size())]).id;
            validStage = setStage(vote);
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

        if (Config.Data.randomColors.get()) setTeamColors();

//        ChunkPos pos1 = level.getChunkAt(stage.cornerA).getPos();
//        ChunkPos pos2 = level.getChunkAt(stage.cornerB).getPos();
//        for (int x = 0; x < Math.abs(pos2.x - pos1.x); x++){
//            for (int z = 0; z < Math.abs(pos2.z - pos1.z); z++){
//                ForgeChunkManager.forceChunk(level, "splatdata", stage.cornerA, x, z, true, false);
//            }
//        }

        int teamAssign = 0;
        for (ServerPlayer player : getPlayerList(true)) {
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
        List<ServerPlayer> noSpectators = getPlayerList().stream().filter((p) -> {return Capabilities.get(p).lobbyStatus != CapInfo.lobbyStates.spectator;}).toList();
        warpPlayers(stageID, noSpectators, true);
        for (ServerPlayer player : getPlayerList()){
            if (noSpectators.contains(player)) continue;
            ServerPlayer target = noSpectators.get(level.random.nextInt(noSpectators.size()));
            player.teleportTo(target.position().x, target.position().y, target.position().z);
        }

        InkDisruptorItem.clearInk(level, stage.cornerA, stage.cornerB);

        inProgress = true;
    }

    public boolean playerInvolved(Player player){
        return players.contains(player.getUUID());
    }
}
