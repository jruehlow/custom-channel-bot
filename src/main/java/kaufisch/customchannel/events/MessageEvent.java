package kaufisch.customchannel.events;

import com.google.gson.JsonObject;
import kaufisch.customchannel.utils.ConfigManager;
import kaufisch.customchannel.utils.Database;
import kaufisch.customchannel.utils.MessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;

public class MessageEvent extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(MessageEvent.class);

    Permission[] permsNeeded = new Permission[]{
            Permission.MANAGE_CHANNEL, Permission.CREATE_INSTANT_INVITE, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_MOVE_OTHERS
    };

    Database db;
    ConfigManager cm;
    MessageBuilder mb;

    public MessageEvent() {
        db = new Database();
        cm = new ConfigManager();
        mb = new MessageBuilder();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        String textChannel = db.getTextChannel(event.getGuild().getId());
        String prefix = db.getPrefix(event.getGuild().getId());
        try {
            if (Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inVoiceChannel()) {
                VoiceChannel vc = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
                if (event.getChannel().getId().equalsIgnoreCase(textChannel) && event.getMessage().getContentRaw().startsWith(prefix) && event.getAuthor().getId().equalsIgnoreCase(db.getLeader(event.getGuild().getId(), vc.getId()))) {
                    JsonObject language;
                    if (db.getLanguage(event.getGuild().getId()) == 2)
                        language = cm.getGerman().getAsJsonObject("errors");
                    else
                        language = cm.getEnglish().getAsJsonObject("errors");

                    if (event.getMessage().getContentRaw().startsWith(prefix + "name")) {
                        nameChannel(event, vc, textChannel);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "size")) {
                        sizeChannel(event, vc, textChannel, language);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "ban")) {
                        banUser(event, vc, textChannel, language);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "unban")) {
                        unbanUser(event, vc, textChannel, language);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "leader")) {
                        leader(event, vc, textChannel, language);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "open")) {
                        openChannel(event, vc, textChannel);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "close")) {
                        closeChannel(event, vc, textChannel);
                    } else if (event.getMessage().getContentRaw().startsWith(prefix + "invite")) {
                        inviteUser(event, vc, textChannel, language);
                    } else if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                        ifAdmin(event, prefix, textChannel, mb);
                    } else if (event.getChannel().getId().equalsIgnoreCase(textChannel) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                        mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
                    }
                } else if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                    ifAdmin(event, prefix, textChannel, mb);
                } else if (event.getChannel().getId().equalsIgnoreCase(textChannel) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                    mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
                }
            } else if (event.getMember().getPermissions().contains(Permission.ADMINISTRATOR) || event.getMember().getId().equals("198489051785986048")) {
                ifAdmin(event, prefix, textChannel, mb);
            } else if (event.getChannel().getId().equalsIgnoreCase(textChannel) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                try {
                    mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
                } catch (Exception e) {
                    logger.info("ERROR | Unknown Message | NAME: " + event.getGuild().getName() + " | ID: " + event.getGuild().getId());
                    event.getChannel().sendMessage(mb.infoMessage(event.getGuild().getId()).build()).queue((newMessage -> db.updateMessage(event.getGuild().getId(), event.getChannel().getId(), newMessage.getId())));
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void ifAdmin(GuildMessageReceivedEvent event, String prefix, String textChannel, MessageBuilder mb) {
        if (Integer.parseInt(db.hasPerms(event.getGuild().getId())) == 1) {
            if (event.getMessage().getContentRaw().equalsIgnoreCase(prefix + "recreate")) {
                recreateChannels(event, mb);
            } else if (event.getMessage().getContentRaw().startsWith(prefix + "prefix")) {
                changePrefix(event, mb, textChannel, prefix);
            } else if (event.getMessage().getContentRaw().startsWith(prefix + "lang")) {
                changeLanguage(event, mb, textChannel, prefix);
            } else if (event.getMessage().getContentRaw().startsWith(prefix + "help")) {
                event.getChannel().sendMessage(mb.helpCommand(event.getGuild().getId()).build()).queue(c -> {
                    if (event.getChannel().getId().equals(textChannel))
                        mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
                });
            } else if (event.getChannel().getId().equalsIgnoreCase(textChannel) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            }
        } else {
            if (event.getMessage().getContentRaw().startsWith(prefix + "help")) {
                if (hasAllPerms(event))
                    event.getChannel().sendMessage("Now create the channels with the command: ``>create``").queue();
                else {
                    StringBuilder perms = new StringBuilder();
                    for (Permission perm : permsNeeded) {
                        if (!event.getGuild().getSelfMember().hasPermission(perm))
                            perms.append(perm.getName()).append(", ");
                    }
                    perms = new StringBuilder(perms.substring(0, perms.length() - 2));
                    event.getChannel().sendMessage("The following permissions are needed: ``" + perms + "``. Please give the bot these permissions and create the channels with the command: ``>create``").queue();
                }
            } else if (event.getMessage().getContentRaw().startsWith(prefix + "create")) {
                if (hasAllPerms(event))
                    createChannels(event, mb);
                else {
                    StringBuilder perms = new StringBuilder();
                    for (Permission perm : permsNeeded) {
                        if (!event.getGuild().getSelfMember().hasPermission(perm))
                            perms.append(perm.getName()).append(", ");
                    }
                    perms = new StringBuilder(perms.substring(0, perms.length() - 2));
                    event.getChannel().sendMessage("The following permissions are needed: ``" + perms + "``.Please give the bot these permissions and create the channels with the command: ``>create``").queue();
                }
            }
        }
    }

    private void changePrefix(GuildMessageReceivedEvent event, MessageBuilder mb, String textChannel, String prefix) {
        JsonObject language;
        if (db.getLanguage(event.getGuild().getId()) == 2)
            language = cm.getGerman().getAsJsonObject("prefix");
        else
            language = cm.getEnglish().getAsJsonObject("prefix");

        String[] msg = event.getMessage().getContentRaw().split(" ");
        if (msg.length == 2 && msg[1].length() <= 3) {
            db.updatePrefix(event.getGuild().getId(), msg[1]);
            event.getChannel().sendMessage(language.get("success").getAsString() + "``" + msg[1] + "``.").queue(c -> {
                if (event.getChannel().getId().equals(textChannel))
                    mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            });
        } else {
            event.getChannel().sendMessage(String.format(language.get("info").getAsString(), prefix)).queue(c -> {
                if (event.getChannel().getId().equals(textChannel))
                    mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            });
        }
    }

    private void recreateChannels(GuildMessageReceivedEvent event, MessageBuilder mb) {
        JsonObject language;
        if (db.getLanguage(event.getGuild().getId()) == 2)
            language = cm.getGerman().getAsJsonObject("channelNames");
        else
            language = cm.getEnglish().getAsJsonObject("channelNames");

        try {
            // delete old channel
            Category categoryOld = Objects.requireNonNull(event.getGuild().getCategoryById(db.getCategory(event.getGuild().getId())));
            for (GuildChannel c : categoryOld.getChannels())
                c.delete().queue();
            categoryOld.delete().queue();
        } catch (Exception ignored) {
        } finally {
            // create new channel
            event.getGuild().createCategory(language.get("category").getAsString()).queue(category -> category.createVoiceChannel(String.format(language.get("voiceChannel").getAsString(), 0)).queue(voiceChannel -> voiceChannel.createInvite().queue(invite -> category.createTextChannel(language.get("textChannel").getAsString()).queue(newTextChannel -> {
                db.updateChannel(event.getGuild().getId(), category.getId(), voiceChannel.getId(), newTextChannel.getId(), invite.getUrl());
                try {
                    newTextChannel.sendMessage(mb.infoMessage(event.getGuild().getId()).build()).queue((newMessage -> db.updateMessage(event.getGuild().getId(), newTextChannel.getId(), newMessage.getId())));
                } catch (InsufficientPermissionException e) {
                    newTextChannel.sendMessage("``MESSAGE_EMBED_LINKS`` permission is missing!").queue();
                }
                logger.info("CHANNEL RECREATED | NAME: " + event.getGuild().getName() + " | ID: " + event.getGuild().getId());
            }))));
        }
    }

    private void banUser(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel, JsonObject language) {
        try {
            if (event.getMessage().getMentionedMembers().size() > 0) {
                for (Member m : event.getMessage().getMentionedMembers()) {
                    if (vc.getMemberPermissionOverrides().stream().anyMatch(po -> po.getMember().equals(m)))
                        vc.getManager().removePermissionOverride(m).queue(c -> vc.createPermissionOverride(m).setDeny(Permission.VOICE_CONNECT).queue());
                    else
                        vc.createPermissionOverride(m).setDeny(Permission.VOICE_CONNECT).queue();
                    if (vc.getMembers().contains(m))
                        event.getGuild().kickVoiceMember(m).queue();
                }
                event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
                mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            } else {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
            }
        } catch (Exception ignored) {
            event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
        }
    }

    private void nameChannel(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel) {
        StringBuilder channelName = new StringBuilder();
        for (int i = 1; i < event.getMessage().getContentRaw().split(" ").length; i++) {
            channelName.append(event.getMessage().getContentRaw().split(" ")[i]).append(" ");
        }
        if (channelName.length() > 0) {
            vc.getManager().setName(channelName.toString()).queue();
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
            mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
        }
    }

    private void sizeChannel(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel, JsonObject language) {
        try {
            int size = Integer.parseInt(event.getMessage().getContentRaw().split(" ")[1]);
            if (size <= 99 && size > 0) {
                vc.getManager().setUserLimit(size).queue();
                event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
                mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            } else {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("channelSize").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
            }
        } catch (NumberFormatException ignored) {
            event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("channelSize").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
        }
    }

    private void unbanUser(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel, JsonObject language) {
        try {
            if (event.getMessage().getMentionedMembers().size() > 0) {
                for (Member m : event.getMessage().getMentionedMembers()) {
                    if (vc.getMemberPermissionOverrides().stream().anyMatch(po -> po.getMember().equals(m)))
                        vc.getManager().removePermissionOverride(m).queue();
                }
                event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
                mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            } else {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
            }
        } catch (Exception ignored) {
            event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
        }
    }

    private void leader(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel, JsonObject language) {
        try {
            if (event.getMessage().getMentionedMembers().size() == 1) {
                Member newLeader = event.getMessage().getMentionedMembers().get(0);
                if (vc.getId().equalsIgnoreCase(Objects.requireNonNull(Objects.requireNonNull(newLeader.getVoiceState()).getChannel()).getId())) {
                    event.getChannel().sendMessage(mb.newLeaderMessage(event.getChannel().getGuild().getId(), vc.getName(), newLeader.getAsMention()).build()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
                    db.newChannelLeader(event.getGuild().getId(), vc.getId(), newLeader.getId());
                } else {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
                }
            } else {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
            }
        } catch (Exception ignored) {
            event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
        }
    }

    private void openChannel(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel) {
        if (vc.getRolePermissionOverrides().stream().anyMatch(p -> Objects.equals(p.getRole(), vc.getGuild().getPublicRole()))) {
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
            vc.getManager().removePermissionOverride(vc.getGuild().getPublicRole()).queue();
            vc.putPermissionOverride(vc.getGuild().getPublicRole()).clear(Permission.VOICE_CONNECT).queue(c -> vc.getManager().removePermissionOverride(vc.getGuild().getMemberById(event.getJDA().getSelfUser().getId())).queue());
        } else {
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u274C").queue();
        }
        mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
    }

    private void closeChannel(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel) {
        try {
            vc.createPermissionOverride(vc.getGuild().getMemberById(event.getJDA().getSelfUser().getId())).grant(Permission.VOICE_CONNECT).queue(c -> vc.createPermissionOverride(vc.getGuild().getPublicRole()).deny(Permission.VOICE_CONNECT).queue());
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
            mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
        } catch (PermissionException e) {
            event.getChannel().sendMessage("Manage-Role-Permission is missing!").queue();
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u274C").queue();
            mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
        } catch (IllegalStateException e) {
            event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u274C").queue();
            mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
        }
    }

    private void inviteUser(GuildMessageReceivedEvent event, VoiceChannel vc, String textChannel, JsonObject language) {
        try {
            if (event.getMessage().getMentionedMembers().size() > 0) {
                for (Member m : event.getMessage().getMentionedMembers()) {
                    vc.createPermissionOverride(m).grant(Permission.VOICE_CONNECT).queue();
                }
                event.getGuild().getTextChannelById(textChannel).addReactionById(event.getMessage().getId(), "\u2705").queue();
                mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            } else {
                event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
            }
        } catch (Exception ignored) {
            event.getChannel().sendMessage(event.getMember().getAsMention() + " " + language.get("userNotFound").getAsString()).queue(c -> mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel))));
        }
    }

    private void createChannels(GuildMessageReceivedEvent event, MessageBuilder mb) {
        JsonObject language;
        if (db.getLanguage(event.getGuild().getId()) == 2)
            language = cm.getGerman().getAsJsonObject("channelNames");
        else
            language = cm.getEnglish().getAsJsonObject("channelNames");

        // create new channel
        event.getGuild().createCategory(language.get("category").getAsString()).queue(category -> category.createVoiceChannel(String.format(language.get("voiceChannel").getAsString(), 0)).queue(voiceChannel -> voiceChannel.createInvite().queue(invite -> category.createTextChannel(language.get("textChannel").getAsString()).queue(newTextChannel -> {
            db.updateChannel(event.getGuild().getId(), category.getId(), voiceChannel.getId(), newTextChannel.getId(), invite.getUrl());
            newTextChannel.sendMessage(mb.helpCommand(event.getGuild().getId()).build()).queue();
            newTextChannel.sendMessage(mb.infoMessage(event.getGuild().getId()).build()).queue((message -> db.newMessage(event.getGuild().getId(), newTextChannel.getId(), message.getId())));
            logger.info("CHANNEL RECREATED | NAME: " + event.getGuild().getName() + " | ID: " + event.getGuild().getId());
        }))));
    }

    private void changeLanguage(GuildMessageReceivedEvent event, MessageBuilder mb, String textChannel, String prefix) {
        String[] msg = event.getMessage().getContentRaw().split(" ");
        if (msg.length == 2 && (msg[1].equals("1") || msg[1].equals("2"))) {
            // update and send msg RECREATE
            db.updateLanguage(event.getGuild().getId(), msg[1]);

            JsonObject language;
            if (Integer.parseInt(msg[1]) == 2)
                language = cm.getGerman().getAsJsonObject("language");
            else
                language = cm.getEnglish().getAsJsonObject("language");

            recreateChannels(event, mb);

            if (!event.getChannel().getId().equals(textChannel)) {
                event.getChannel().sendMessage(language.get("success").getAsString()).queue();
            }
        } else {
            // send help msg
            JsonObject language;
            if (db.getLanguage(event.getGuild().getId()) == 2)
                language = cm.getGerman();
            else
                language = cm.getEnglish();

            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setDescription(String.format(language.getAsJsonObject("language").get("info-desc").getAsString(), prefix))
                    .addField("**English**", "ID: 1", true)
                    .addField("**German**", "ID: 2", true)
                    .setFooter(language.get("footer").getAsString());
            event.getChannel().sendMessage(eb.build()).queue(c -> {
                if (event.getChannel().getId().equals(textChannel))
                    mb.resendMessage(Objects.requireNonNull(event.getGuild().getTextChannelById(textChannel)));
            });
        }
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        MessageBuilder mb = new MessageBuilder();
        for (String channel : db.getTextChannels()) {
            try {
                mb.resendMessage(Objects.requireNonNull(event.getJDA().getTextChannelById(channel)));
            } catch (Exception e) {
                logger.info("Unknown Message | Channel ID: " + channel);
            }
        }
    }

    private boolean hasAllPerms(GuildMessageReceivedEvent event) {
        for (Permission perm : permsNeeded) {
            if (!event.getGuild().getSelfMember().hasPermission(perm))
                return false;
        }
        return true;
    }
}
