package ru.miau.ilyushabot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.yaml.snakeyaml.Yaml;
import ru.miau.ilyushabot.functions.offers.OffersChannelListener;
import ru.miau.ilyushabot.functions.private_vcs.VoiceChannelListener;


import ru.miau.ilyushabot.managers.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IlyushaBot {
    public static Map<String, Object> config;
    public static JDA jda;
    public static Guild guild;
    public static Long guildId;
    public static void main(String[] args) throws InterruptedException {
        loadConfig();
        guildId = (Long) IlyushaBot.config.get(YamlKeys.GUILD_ID);
        jda = JDABuilder.createDefault(token())
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .enableCache(EnumSet.allOf(CacheFlag.class))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(
                        //managers
                        new ButtonsManager(),
                        new CommandManager(),
                        new ModalManager(),
                        new SelectMenuManager(),
                        //other listeners
                        new OffersChannelListener(),
                        new VoiceChannelListener()
                )
                .build().awaitReady();
        guild = jda.getGuildById(guildId);
    }

    private static String token()  {
        FileReader reader = null;
        try {
            reader = new FileReader("token.txt");
        } catch (FileNotFoundException e) {

        }
        Scanner scanner = new Scanner(reader);
        return scanner.nextLine();
    }

    public static void loadConfig() {
        Yaml yaml = new Yaml();
        try {
            FileReader reader = new FileReader("config.yml");
            config = yaml.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}