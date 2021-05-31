package kaufisch.customchannel.events;

import com.google.gson.JsonObject;
import kaufisch.customchannel.utils.ConfigManager;
import kaufisch.customchannel.utils.Database;
import kaufisch.customchannel.utils.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;

public class VoiceChannelHandler extends ListenerAdapter {

    Database db;
    ConfigManager cm;
    MessageBuilder mb;
    Timer timer;

    public VoiceChannelHandler() {
        db = new Database();
        cm = new ConfigManager();
        mb = new MessageBuilder();
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        String[] vct = db.getVCT(event.getGuild().getId());
        if (event.getChannelJoined().getId().equalsIgnoreCase(vct[0]) && Objects.requireNonNull(event.getGuild().getCategoryById(vct[1])).getVoiceChannels().size() < 46)
            createChannel(event.getGuild(), event.getMember(), vct);
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        String[] vct = db.getVCT(event.getGuild().getId());
        // check if channel joined is create_channel
        if (event.getChannelJoined().getId().equalsIgnoreCase(vct[0]) && Objects.requireNonNull(event.getGuild().getCategoryById(vct[1])).getVoiceChannels().size() < 46)
            createChannel(event.getGuild(), event.getMember(), vct);
        // check if channel is created by bot
        if (db.channelExists(event.getGuild().getId(), event.getChannelLeft().getId())) {
            // check if channel is empty now
            ifChannelLeft(event.getGuild(), event.getChannelLeft(), event.getMember(), vct);
        }
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (db.channelExists(event.getGuild().getId(), event.getChannelLeft().getId())) {
            String[] vct = db.getVCT(event.getGuild().getId());
            ifChannelLeft(event.getGuild(), event.getChannelLeft(), event.getMember(), vct);
        }
    }

    private void ifChannelLeft(Guild guild, VoiceChannel channel, Member member, String[] vct) {
        try {
            // check if channel is empty
            if (channel.getMembers().size() == 0)
                // delete channel
                deleteChannel(guild, channel);
            else {
                // check if user was leader of the channel
                if (member.getId().equalsIgnoreCase(db.getLeader(guild.getId(), channel.getId()))) {
                    // set new channel-leader
                    MessageBuilder mb = new MessageBuilder();
                    Random rand = new Random();
                    List<Member> members = channel.getMembers();
                    members.removeIf(m -> m.getUser().isBot());
                    Member newLeader = members.get(rand.nextInt(members.size()));
                    guild.getTextChannelById(vct[2]).sendMessage(mb.newLeaderMessage(guild.getId(), channel.getName(), newLeader.getEffectiveName()).build()).queue(m -> mb.resendMessage(guild.getTextChannelById(vct[2])));
                    db.newChannelLeader(guild.getId(), channel.getId(), newLeader.getId());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void createChannel(Guild guild, Member member, String[] vct) {
        /*
         * if join_time db not contains user in last minuite -> add to db || kick
         */
        if(!db.hasJoinTimer(guild.getId(), member.getId())) {
            db.addJoinTimeUser(guild.getId(), member.getId());
            JsonObject language;
            if (db.getLanguage(guild.getId()) == 2)
                language = cm.getGerman().getAsJsonObject("channelNames");
            else
                language = cm.getEnglish().getAsJsonObject("channelNames");

            Objects.requireNonNull(guild.getCategoryById(vct[1])).createVoiceChannel(member.getEffectiveName() + " - VC").setUserlimit(4).queue((channel) -> {
                guild.moveVoiceMember(member, channel).queue();
                Objects.requireNonNull(guild.getVoiceChannelById(vct[0])).getManager().setName(language.get("voiceChannel").getAsString()).queue();
                db.addChannel(guild.getId(), channel.getId(), member.getId());
            });
        } else {
            member.getUser().openPrivateChannel().queue((privateChannel) -> privateChannel.sendMessage(mb.waitToCreate(guild.getId()).build()).queue());
            guild.kickVoiceMember(member).queue();
        }
    }

    private void deleteChannel(Guild guild, VoiceChannel channelLeft) {
        try {
            db.deleteChannel(guild.getId(), channelLeft.getId());
            Objects.requireNonNull(channelLeft).delete().queue();
        } catch(ErrorResponseException ignored) {}
    }
}
