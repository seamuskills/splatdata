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
        public static ForgeConfigSpec.ConfigValue<String> stageName;
        public static ForgeConfigSpec.IntValue matchTime;
        public static void init(ForgeConfigSpec.Builder builder){
            respawnTime = builder.comment("How long the respawn time is (in seconds)").defineInRange("splatcraftdata.respawntime", 7.0f ,0.0d, Double.MAX_VALUE);
            stageName = builder.comment("id of the /stage stage used for the lobby").define("splatcraftdata.stageName","s3Lobby");
            matchTime = builder.comment("How long (in seconds) a match should last").defineInRange("splatcraftdata.matchTime", 180, 1, Integer.MAX_VALUE);
        }
    }
}
