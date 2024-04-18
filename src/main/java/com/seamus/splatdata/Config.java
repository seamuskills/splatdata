package com.seamus.splatdata;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;

public class Config {
    private final static ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public final static ForgeConfigSpec configInstance;

    static{
        Data.init(builder);
        configInstance = builder.build();
    }

        public static void loadConfig(ForgeConfigSpec config, String path)
        {
            final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();

            file.load();
            config.setConfig(file);
        }

    public static class Data{
        public static ForgeConfigSpec.DoubleValue respawnTime;
        public static ForgeConfigSpec.DoubleValue matchTime;
        public static ForgeConfigSpec.IntValue varietyRequirement;
        public static ForgeConfigSpec.DoubleValue readyToStartTime;
        public static ForgeConfigSpec.DoubleValue introLength;
        public static ForgeConfigSpec.BooleanValue randomColors;
        public static ForgeConfigSpec.BooleanValue forceInkTanks;
        public static ForgeConfigSpec.IntValue cashPayout;
        public static ForgeConfigSpec.IntValue winBonus;
        public static ForgeConfigSpec.BooleanValue forceFestInk;
        public static void init(ForgeConfigSpec.Builder builder){
            respawnTime = builder.comment("How long the respawn time is (in seconds) used when a player is not in a match").defineInRange("splatdata.respawn.respawntime", 7.0f ,0.0d, Double.MAX_VALUE);
            matchTime = builder.comment("How long (in seconds) a match should last when the game type doesn't specify").defineInRange("splatdata.match.matchTime", 180, 1, Double.MAX_VALUE);
            varietyRequirement = builder.comment("The percentage of player votes that must be valid before the stage is chosen randomly.").defineInRange("splatdata.command.voteRequirement", 50, 1, 100);
            readyToStartTime = builder.comment("How many seconds until a match starts after everyone is ready").defineInRange("splatdata.match.readyToStartTime", 5, 0.1, Double.MAX_VALUE);
            introLength = builder.comment("How many seconds the intro cutscene should take before the match begins").defineInRange("splatdata.match.introLength", 5, 1, Double.MAX_VALUE);
            randomColors = builder.comment("Whether teams should be of random colors when a match starts on a given stage. Will destroy original colors used!").define("splatdata.match.randomColors",true);
            forceInkTanks = builder.comment("Whether to force the players to wear an ink tank").define("splatdata.misc.forceInkTanks", true);
            cashPayout = builder.comment("The amount of cash awarded to a player when they finish a match").defineInRange("splatdata.match.payout", 10, 0, Integer.MAX_VALUE);
            winBonus = builder.comment("The amount of extra cash a player will get for winning a match").defineInRange("splatdata.match.winBonus", 5, 0, Integer.MAX_VALUE);
            forceFestInk = builder.comment("Whether or not splatfest bands are forced into the inventory of all players for the duration of a splatfest, this will make them all paint with sparkly ink!").define("splatdata.misc.forceSplatfestInk", true);
        }
    }
}
