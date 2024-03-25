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
import net.minecraft.server.commands.TeamCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.server.ServerLifecycleHooks;
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
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

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
    public boolean noMoney = false;
    public HashMap<Attribute, AttributeModifier> modifiers = new HashMap<>();

    public List<PlayerTeam> scoreboardTeams = new ArrayList<>();
    public String wipeoutWin = "";
    public Match(ServerPlayer p, ServerLevel l, UUID matchid){
        matchGameType = GameTypeListener.gameTypes.get("splatdata:turfwar");
        if (matchGameType.matchTime < 100){noMoney = true;}
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

    public int teamCount(String team){
        if (!teams.contains(team) || stage == null){
            LogManager.getLogger("splatdata").warn("Invalid team or stage when attempting to get team count...");
            return 0;
        }else{
            return getPlayerList().stream().filter((p) -> ColorUtils.getPlayerColor(p) == stage.getTeamColor(team)).toArray().length;
        }
    }

    public void closeMatch(){
        CustomBossEvents bossEvents = level.getServer().getCustomBossEvents();
        bossbar.setPlayers(new ArrayList<ServerPlayer>()); //if I don't do this, the bossbar will stay on the player's screen even if I destroy it...
        bossEvents.remove(bossbar);
        for (PlayerTeam t : scoreboardTeams){
            ServerLifecycleHooks.getCurrentServer().getScoreboard().removePlayerTeam(t);
        }
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
            scoreboardTeams = new ArrayList<>();
            for (String t : teams){
                PlayerTeam team = ServerLifecycleHooks.getCurrentServer().getScoreboard().addPlayerTeam("splatdata:" + t + "(match:" + id + ")");
                team.setDeathMessageVisibility(Team.Visibility.NEVER);
                team.setNameTagVisibility(Team.Visibility.HIDE_FOR_OTHER_TEAMS);
                team.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
                team.setSeeFriendlyInvisibles(false);
                scoreboardTeams.add(team);
            }
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

    public void removeMods(ServerPlayer p){
        if (modifiers.isEmpty()){
            return;
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entrySet()) {
            p.getAttribute(entry.getKey()).removeModifier(entry.getValue());
        }
    }

    public void addMods(ServerPlayer player){
        for (Attribute attribute : matchGameType.attributes.keySet()){
            AttributeModifier mod = new AttributeModifier(attribute.getRegistryName().getPath(), matchGameType.attributes.get(attribute), AttributeModifier.Operation.MULTIPLY_BASE);
            modifiers.put(attribute, mod);
            try {
                player.getAttribute(attribute).addTransientModifier(mod);
            }catch(NullPointerException e){
                Logger.getLogger("splatdata").warning("Player does not have attribute " + attribute.getRegistryName());
            }
        }
    }

    private void gameplay(){
        //wave respawn code
        if (matchGameType.rMode == MatchGameType.respawnMode.wave || matchGameType.rMode == MatchGameType.respawnMode.waveOrTimed) {
            for (String t : teams) {
                List<ServerPlayer> p = getPlayerList();
                p.removeIf((player) -> ColorUtils.getPlayerColor(player) != stage.getTeamColor(t)); //remove all non-team members
                List<ServerPlayer> dead = p.stream().filter((player) -> Capabilities.get(player).respawnTimeTicks >= 0).toList(); //get all dead players
                p.removeIf((player) -> Capabilities.get(player).respawnTimeTicks >= 0); //remove all dead players from the list doing the initial check
                if (p.isEmpty()) {
                    for (Player player : dead) {
                        CapInfo info = Capabilities.get(player);
                        info.waveRespawning = true;
                        if (matchGameType.rMode == MatchGameType.respawnMode.waveOrTimed){
                            info.respawnTimeTicks = 0;
                        }
                    }
                }
            }
        }
        if (matchGameType.rMode == MatchGameType.respawnMode.disabled && teams.size() > 1){
            ArrayList<String> aliveTeams = new ArrayList<>(); //all teams that are alive
            List<ServerPlayer> players = getPlayerList(); //query this once so the function only has to run once...
            for (String t : teams){
                List<ServerPlayer> teamPlayers = players.stream().filter((player) -> ColorUtils.getPlayerColor(player) == stage.getTeamColor(t)).toList(); //get all players on this team
                if (teamPlayers.stream().filter((player) -> Capabilities.get(player).respawnTimeTicks <= 0).toArray().length > 0){ //see if there are any alive players
                    aliveTeams.add(t);
                }
            }
            if (aliveTeams.size() == 1){
                wipeoutWin = aliveTeams.get(0);
                timeLeft = 1;
            }else if (aliveTeams.isEmpty()) {
                wipeoutWin = "nobody wins"; //technically this breaks if there is a team called "nobody wins" but who the hell is gonna name a team that???
                timeLeft = 1;
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
                removeMods(player);
                Capabilities.get(player).respawnTimeTicks = -1;
                player.setGameMode(GameType.SPECTATOR);
                ServerLifecycleHooks.getCurrentServer().getScoreboard().removePlayerFromTeam(player.getStringUUID());
                bossbar.setVisible(false);
            }
            for (PlayerTeam t : scoreboardTeams){
                ServerLifecycleHooks.getCurrentServer().getScoreboard().removePlayerTeam(t);
            }
            switch(matchGameType.wCondition) {
                case turf:
                    results = TurfScannerItem.scanTurf(level, level, stage.cornerA, stage.cornerB, 1, new ArrayList<>());
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
            if (!wipeoutWin.isEmpty()){
                scoreText = new TextComponent("winner by wipe out: " + wipeoutWin);
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
                if (!noMoney) {
                    playerCaps.cash += Config.Data.cashPayout.get();
                    if (wipeoutWin.isEmpty()) {
                        switch (matchGameType.wCondition) {
                            case turf:
                                if (ColorUtils.getPlayerColor(player) == results.getCommandResult()) {
                                    playerCaps.cash += Config.Data.winBonus.get();
                                }
                                break;
                            case splats:
                                if (teams.contains(splatWinner)) {
                                    if (ColorUtils.getPlayerColor(player) == stage.getTeamColor(splatWinner)) {
                                        playerCaps.cash += Config.Data.winBonus.get();
                                    }
                                }
                                break;
                        }
                    }else if (teams.contains(wipeoutWin)){
                        if (stage.getTeamColor(wipeoutWin) == ColorUtils.getPlayerColor(player)){
                            playerCaps.cash += Config.Data.winBonus.get();
                        }
                    }
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

    public void soundoff(SoundEvent sound){
        for (ServerPlayer player : getPlayerList()){
            player.playNotifySound(sound, SoundSource.VOICE, 1.0f, 1.0f);
        }
    }

    public void stalk(ServerPlayer p, boolean spectate){
        CapInfo caps = Capabilities.get(p);
        caps.match = id;

        if (inProgress){
            if (spectate){
                p.setGameMode(GameType.SPECTATOR);
                ColorUtils.setPlayerColor(p, 0xffffff);
                List<ServerPlayer> players = getPlayerList();
                p.setGameMode(GameType.SPECTATOR);
                Vec3 position = players.get(p.level.random.nextInt(players.size())).position();
                p.teleportTo(position.x, position.y, position.z); //stupid that it doesn't take vec3 directly...
            }else{
                String leastTeam = null;
                int teamLeast = Integer.MAX_VALUE;
                for (String team : teams){
                    if (teamCount(team) <= teamLeast){
                        leastTeam = team;
                        teamLeast = teamCount(team);
                    }
                    System.out.println(team + teamCount(team));
                }
                ColorUtils.setPlayerColor(p, stage.getTeamColor(leastTeam));
                PlayerTeam scoreTeam = ServerLifecycleHooks.getCurrentServer().getScoreboard().getPlayersTeam("splatdata:" + leastTeam + "(match:" + id + ")");
                if (scoreTeam != null) ServerLifecycleHooks.getCurrentServer().getScoreboard().addPlayerToTeam(p.getStringUUID(), scoreTeam);
                Collection<ServerPlayer> playerForWarp = new ArrayList<>();
                playerForWarp.add(p);
                warpPlayers(stageID, playerForWarp, true);
                if (currentState != matchStates.gameplay){
                    p.setGameMode(GameType.SPECTATOR);
                }else {
                    p.kill();
                }
            }
        }
        players.add(p.getUUID());
    }

    public void stalk(ServerPlayer p){
        stalk(p, false);
    }

    public void excommunicate(ServerPlayer p, boolean tp){
        players.remove(p.getUUID());
        ServerLifecycleHooks.getCurrentServer().getScoreboard().removePlayerFromTeam(p.getStringUUID());
        removeMods(p);
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

    public HashMap<String, StageData> getValidStages(){
        HashMap<String, StageData> validStages = StageDataListener.stages;
        for (Match match : WorldCaps.get(level).activeMatches.values()){
            if (match.id == id || match.stageID.isEmpty() || match.stage.getTeamIds().isEmpty()){continue;}
            validStages.remove(match.stageID);
        }
        return validStages;
    }

    public ArrayList<String> getVotes(HashMap<String, StageData> stages){
        ArrayList<String> votes = new ArrayList<>(getPlayerList().stream().map((p) -> {return Capabilities.get(p).vote;}).toList());
        votes = new ArrayList<>(votes.stream().filter((s) -> stages.containsKey(s) || s.equals("Random")).toList());
        return votes;
    }

    public String decideStage(ArrayList<String> votes, HashMap<String, StageData> validStages){
        if (votes.size() / players.size() >= Config.Data.varietyRequirement.get() / 100 && !votes.isEmpty()){
            String vote = votes.get(level.random.nextInt(votes.size()));
            if (vote.equals("Random")) vote = validStages.get((String)validStages.keySet().toArray()[level.random.nextInt(validStages.size())]).id;
            return vote;
        }else{
            broadcast(new TextComponent("Not enough players have a valid vote, choosing randomly from all maps").withStyle(ChatFormatting.YELLOW));
            return (String) validStages.keySet().toArray()[level.random.nextInt(validStages.size())];
        }
    }

    public void startGame(){
        wipeoutWin = "";
        currentState = matchStates.intro;

        HashMap<String, StageData> validStages = getValidStages();

        if (validStages.isEmpty()){
            broadcast(new TextComponent("All stages are currently in use! Please wait and try again later!").withStyle(ChatFormatting.RED));
            return;
        }

        ArrayList<String> votes = getVotes(validStages);

        boolean validStage = setStage(decideStage(votes, validStages));

        if (!(validStages.containsKey(stageID))){ //quick last check, this should never fire.
            validStage = false;
        }

        if (!validStage){
            broadcast(new TextComponent("Invalid stage selected! Please contact admin! Stage: " + stageID).withStyle(ChatFormatting.RED));
            soundoff(SoundEvents.ENDERMAN_DEATH);
            return; //can't start with no stage
        }

        if (Config.Data.randomColors.get() || WorldCaps.get(level).fest) setTeamColors();

        modifiers = new HashMap<>(); //reset it

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
                    PlayerTeam scoreTeam = ServerLifecycleHooks.getCurrentServer().getScoreboard().getPlayersTeam("splatdata:" + teamAssign + "(match:" + id + ")");
                    if (scoreTeam != null) ServerLifecycleHooks.getCurrentServer().getScoreboard().addPlayerToTeam(player.getStringUUID(), scoreTeam);
                    teamAssign = (teamAssign + 1) % teams.size();
                    ColorUtils.setPlayerColor(player, stage.getTeamColor(caps.team));
                    break;
                case spectator:
                    ColorUtils.setPlayerColor(player, 0xffffff);
                    caps.team = "spec";
                    break;
            }
            if (!matchGameType.attributes.isEmpty()){
                addMods(player);
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
