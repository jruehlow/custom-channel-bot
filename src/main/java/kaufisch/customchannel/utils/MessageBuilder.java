package kaufisch.customchannel.utils;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class MessageBuilder {

    Database db;
    ConfigManager cm;

    final static Logger logger = LoggerFactory.getLogger(MessageBuilder.class);

    public MessageBuilder() {
        db = new Database();
        cm = new ConfigManager();
    }

    public void resendMessage(TextChannel channel) {
        String msgId = db.getMessage(channel.getGuild().getId(), channel.getId());
        channel.deleteMessageById(msgId).queue(c -> channel.sendMessage(infoMessage(channel.getGuild().getId()).build()).queue(
                message -> db.updateMessageId(channel.getGuild().getId(), channel.getId(), message.getId())
        ), new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE,
                c -> channel.sendMessage(infoMessage(channel.getGuild().getId()).build()).queue(
                        message -> db.updateMessageId(channel.getGuild().getId(), channel.getId(), message.getId())
                )
        ));
    }

    public EmbedBuilder newLeaderMessage(String guild, String channelName, String newLeader) {
        JsonObject language;
        if (db.getLanguage(guild) == 2)
            language = cm.getGerman();
        else
            language = cm.getEnglish();
        return new EmbedBuilder()
                .setTitle(channelName)
                .setColor(Color.GREEN)
                .setDescription("***" + newLeader + " " + language.getAsJsonObject("newLeaderMessage").get("desc").getAsString() + "!***")
                .setFooter(language.get("footer").getAsString());
    }

    public EmbedBuilder infoMessage(String guild) {
        String p = db.getPrefix(guild);
        JsonObject language;
        if (db.getLanguage(guild) == 2)
            language = cm.getGerman();
        else
            language = cm.getEnglish();
        return new EmbedBuilder()
                .setDescription(String.format("***%s(%s)***", language.getAsJsonObject("infoMessage").get("desc").getAsString(), db.getLink(guild)))
                .setColor(Color.CYAN)
                .addField("",
                        "``" + p + "name name`` " + language.getAsJsonObject("infoMessage").get("name").getAsString() + "\n" +
                                "``" + p + "size 1-99`` " + language.getAsJsonObject("infoMessage").get("size").getAsString() + "\n" +
                                "``" + p + "ban @user`` " + language.getAsJsonObject("infoMessage").get("ban").getAsString() + "\n" +
                                "``" + p + "unban @user`` " + language.getAsJsonObject("infoMessage").get("unban").getAsString() + "\n" +
                                "``" + p + "leader @user`` " + language.getAsJsonObject("infoMessage").get("leader").getAsString() + "\n\n" +
                                "``" + p + "open`` " + language.getAsJsonObject("infoMessage").get("open").getAsString() + "\n" +
                                "``" + p + "close`` " + language.getAsJsonObject("infoMessage").get("close").getAsString() + "\n" +
                                "``" + p + "invite`` " + language.getAsJsonObject("infoMessage").get("invite").getAsString() + "\n"
                        , false)
                .setFooter(language.get("footer").getAsString());
    }

    public EmbedBuilder helpCommand(String guild) {
        String p = db.getPrefix(guild);
        JsonObject language;
        if (db.getLanguage(guild) == 2)
            language = cm.getGerman();
        else
            language = cm.getEnglish();
        return new EmbedBuilder()
                .setColor(Color.CYAN)
                .setDescription(language.getAsJsonObject("help").get("desc").getAsString())
                .addField("**" + p + "prefix value**", language.getAsJsonObject("help").get("prefix").getAsString(), true)
                .addField("**" + p + "recreate**", language.getAsJsonObject("help").get("recreate").getAsString(), true)
                .addField("**" + p + "lang id**", String.format(language.getAsJsonObject("help").get("lang").getAsString(), p), true)
                .setFooter(language.get("footer").getAsString());
    }

    public EmbedBuilder waitToCreate(String guild) {
        JsonObject language;
        if (db.getLanguage(guild) == 2)
            language = cm.getGerman();
        else
            language = cm.getEnglish();
        return new EmbedBuilder()
                .setTitle(language.getAsJsonObject("errors").get("waitToCreateTitle").getAsString())
                .setColor(Color.CYAN)
                .setDescription(language.getAsJsonObject("errors").get("waitToCreate").getAsString())
                .setFooter(language.get("footer").getAsString());
    }

}
