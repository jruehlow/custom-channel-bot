package kaufisch.customchannel.events;

import com.google.gson.JsonObject;
import kaufisch.customchannel.utils.ConfigManager;
import kaufisch.customchannel.utils.Database;
import kaufisch.customchannel.utils.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

public class OnGuildJoin extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnGuildJoin.class);

    Database db;
    ConfigManager cm;
    Permission[] permsNeeded = new Permission[]{
            Permission.MANAGE_CHANNEL, Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_ROLES
    };

    public OnGuildJoin() {
        db = new Database();
        cm = new ConfigManager();
    }

    private boolean hasAllPerms(GuildJoinEvent event) {
        for (Permission perm : permsNeeded) {
            if (!event.getGuild().getSelfMember().hasPermission(perm))
                return false;
        }
        return true;
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        JsonObject language = cm.getEnglish().getAsJsonObject("channelNames");
        MessageBuilder mb = new MessageBuilder();
        db.newGuild(event.getGuild().getId());
        if (hasAllPerms(event)) {
            // create intitial channels
            event.getGuild().createCategory(language.get("category").getAsString()).queue(category -> {
                db.setCategory(event.getGuild().getId(), category.getId());
                category.createVoiceChannel(String.format(language.get("voiceChannel").getAsString(), 0)).queue(voiceChannel -> {
                    voiceChannel.createInvite().setMaxAge(0).queue(invite -> {
                        category.createTextChannel(language.get("textChannel").getAsString()).queue(textChannel -> {
                            db.setChannel(event.getGuild().getId(), voiceChannel.getId(), textChannel.getId(), invite.getUrl());
                            textChannel.sendMessage(mb.helpCommand(event.getGuild().getId()).build()).queue();
                            textChannel.sendMessage(mb.infoMessage(event.getGuild().getId()).build()).queue((message -> db.newMessage(event.getGuild().getId(), textChannel.getId(), message.getId())));
                            logger.info("GUILD JOINED | NAME: " + event.getGuild().getName() + " | ID: " + event.getGuild().getId());
                        });
                    });
                });
            });
        }
    }
}
