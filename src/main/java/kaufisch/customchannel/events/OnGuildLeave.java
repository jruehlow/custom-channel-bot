package kaufisch.customchannel.events;

import kaufisch.customchannel.utils.Database;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

public class OnGuildLeave extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnGuildLeave.class);

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        Database db = new Database();
        db.deleteGuild(event.getGuild().getId());
        logger.info("GUILD LEFT | NAME: " + event.getGuild().getName() + " | ID: " + event.getGuild().getId());
    }

}
