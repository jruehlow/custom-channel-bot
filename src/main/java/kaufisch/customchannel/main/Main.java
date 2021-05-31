package kaufisch.customchannel.main;

import kaufisch.customchannel.events.MessageEvent;
import kaufisch.customchannel.events.OnGuildJoin;
import kaufisch.customchannel.events.OnGuildLeave;
import kaufisch.customchannel.events.VoiceChannelHandler;
import kaufisch.customchannel.utils.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("   _____          _                   _____ _                            _ ");
        System.out.println("  / ____|        | |                 / ____| |                          | |");
        System.out.println(" | |    _   _ ___| |_ ___  _ __ ___ | |    | |__   __ _ _ __  _ __   ___| |");
        System.out.println(" | |   | | | / __| __/ _ \\| '_ ` _ \\| |    | '_ \\ / _` | '_ \\| '_ \\ / _ \\ |");
        System.out.println(" | |___| |_| \\__ \\ || (_) | | | | | | |____| | | | (_| | | | | | | |  __/ |");
        System.out.println("  \\_____\\__,_|___/\\__\\___/|_| |_| |_|\\_____|_| |_|\\__,_|_| |_|_| |_|\\___|_|");
        System.out.println("                                                                           ");
        System.out.println();
        ConfigManager cm = new ConfigManager();
        cm.getAuthToken();
        try {
            String token = cm.getAuthToken();
            if (token.equals("not found")) {
                logger.error("config.json not found!\n");
                System.exit(0);
            } else if (token.equals("default values")) {
                logger.error("Please make sure that you fill config.json with values!");
                System.exit(0);
            } else {
                JDA jda = JDABuilder
                        .createDefault(cm.getAuthToken())
                        .setAutoReconnect(true)
                        .setChunkingFilter(ChunkingFilter.NONE)
                        .setActivity(Activity.playing(">help"))
                        .addEventListeners(new MessageEvent())
                        .addEventListeners(new VoiceChannelHandler())
                        .addEventListeners(new OnGuildJoin())
                        .addEventListeners(new OnGuildLeave())
                        .build();
                jda.awaitReady();
            }
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
