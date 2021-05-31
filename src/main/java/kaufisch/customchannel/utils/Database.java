package kaufisch.customchannel.utils;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles MySQL Database
 *
 * @author Kaufisch
 */

public class Database {

    private MysqlDataSource mysqlDataSource;

    /**
     * Initialize Database Connection
     */
    public Database() {
        try {
            ConfigManager cm = new ConfigManager();
            HashMap<String, String> creds = cm.getDatabaseCreds();
            mysqlDataSource = new MysqlDataSource();
            mysqlDataSource.setUser(creds.get("user"));
            mysqlDataSource.setPassword(creds.get("password"));
            mysqlDataSource.setServerName(creds.get("host"));
            mysqlDataSource.setDatabaseName(creds.get("database"));
            mysqlDataSource.setServerTimezone("UTC");
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    /*
     * GET
     */

    public int getLanguage(String guild) {
        int res = 0;
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(String.format("SELECT language FROM config WHERE guild='%s';", guild)); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res = rs.getInt(1);
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    public String getTextChannel(String guild) {
        String[] args = {guild};
        return getSingleString("SELECT textChannel FROM config WHERE guild=?;", args);
    }

    public String getCategory(String guild) {
        String[] args = {guild};
        return getSingleString("SELECT category FROM config WHERE guild=?;", args);
    }

    public String getLink(String guild) {
        String[] args = {guild};
        return getSingleString("SELECT link FROM config WHERE guild=?;", args);
    }

    public String getPrefix(String guild) {
        String[] args = {guild};
        return getSingleString("SELECT prefix FROM config WHERE guild=?;", args);
    }

    public String getMessage(String guild, String channel) {
        String[] args = {guild, channel};
        return getSingleString("SELECT message FROM messages WHERE guild=? AND channel =?;", args);
    }

    public String getLeader(String guild, String channel) {
        String[] args = {guild, channel};
        return getSingleString("SELECT leader FROM channel WHERE guild=? AND channel=?;", args);
    }

    public String hasPerms(String guild) {
        String[] args = {guild};
        return getSingleString("SELECT IF(category IS NULL, 0, 1) FROM config WHERE guild=?;", args);
    }

    /**
     * Get VoiceChannel, Category, TextChannel from Database Table
     * @param guild
     * @return List: 1. VoiceChannelID, 2. CategoryID, 3. TextChannelID
     */
    public String[] getVCT(String guild) {
        String query = "SELECT voiceChannel,category,textChannel FROM config WHERE guild=?;";
        String[] res = new String[3];
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, guild);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res[0] = rs.getString(1);
                res[1] = rs.getString(2);
                res[2] = rs.getString(3);
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    public ArrayList<String> getTextChannels() {
        String query = "SELECT textChannel FROM config;";
        ArrayList<String> res= new ArrayList<>();
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.add(rs.getString(1));
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }


    public boolean hasJoinTimer(String guild, String user) {
        String query = "SELECT IF((SELECT TIMESTAMPDIFF(SECOND, (SELECT time FROM join_timer WHERE guildId=? AND userId=? ORDER BY time DESC LIMIT 1), CURRENT_TIMESTAMP())) < 60, 1, 0);";
        boolean res = false;
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, guild);
            ps.setString(2, user);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = rs.getInt(1) == 1;
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    private String getSingleString(String query, String[] args) {
        String res = null;
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            for(int i = 0; i < args.length; i++) {
                ps.setString(i+1, args[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                res = rs.getString(1);
            }
            rs.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return res;
        }
    }

    /*
     * INSERT / UPDATE / DELETE
     */

    public void newGuild(String guild) {
        String[] args = {guild};
        updateQuery("INSERT INTO config (guild) VALUES (?);", args);
    }

    public void setCategory(String guild, String category) {
        String[] args = {category, guild};
        updateQuery("UPDATE config SET category=? WHERE guild=?;", args);
    }

    public void setChannel(String guild, String voiceChannel, String textChannel, String link) {
        String[] args = {voiceChannel, textChannel, link, guild};
        updateQuery("UPDATE config SET voiceChannel=?, textChannel=?, link=? WHERE guild=?;", args);
    }

    public void updateChannel(String guild, String category, String voiceChannel, String textChannel, String link) {
        String[] args = {category, voiceChannel, textChannel, link, guild};
        updateQuery("UPDATE config SET category=?, voiceChannel=?, textChannel=?, link=? WHERE guild=?;", args);
    }

    public void addChannel(String guild, String channel, String leader) {
        String[] args = {guild, channel, leader};
        updateQuery("INSERT INTO channel (guild, channel, leader) VALUES (?, ?, ?);", args);
    }

    public void newMessage(String guild, String channel, String message) {
        String[] args = {guild, channel,message};
        updateQuery("INSERT INTO messages (guild, channel, message) VALUES (?,?,?)", args);
    }

    public void updateMessageId(String guild, String channel, String message) {
        String[] args = {message, guild, channel};
        updateQuery("UPDATE messages SET message=? WHERE guild=? AND channel=?;", args);
    }

    public void updateMessage(String guild, String channel, String message) {
        String[] args = {message, channel,  guild};
        updateQuery("UPDATE messages SET message=?, channel=? WHERE guild=?;", args);
    }

    public void updatePrefix(String guild, String prefix) {
        String[] args = {prefix, guild};
        updateQuery("UPDATE config SET prefix=? WHERE guild=?;", args);
    }

    public void updateLanguage(String guild, String lang) {
        String[] args = {lang, guild};
        updateQuery("UPDATE config SET language=? WHERE guild=?;", args);
    }

    public void updateLink(String guild, String link) {
        String[] args = {link, guild};
        updateQuery("UPDATE config SET link=? WHERE guild=?;", args);
    }

    public void newChannelLeader(String guild, String channel, String leader) {
        String[] args = {leader, guild, channel};
        updateQuery("UPDATE channel SET leader=? WHERE guild=? AND channel=?;", args);
    }

    public void deleteGuild(String guild) {
        String[] args = {guild};
        updateQuery("DELETE FROM config WHERE guild=?", args);
        updateQuery("DELETE FROM messages WHERE guild=?", args);
    }

    public void addJoinTimeUser(String guild, String user) {
        String[] args = {guild, user};
        updateQuery("INSERT INTO join_timer (guildId, userId) VALUES (?, ?);", args);
    }

    private void updateQuery(String query, String[] args) {
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            for(int i = 0; i < args.length; i++) {
                ps.setString(i+1, args[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    * Miscellaneous
    */

    public boolean channelExists(String guild, String channel) {
        String query = String.format("SELECT IF(EXISTS ( SELECT 1 FROM channel WHERE guild='%s' AND channel='%s'), 1, 0)", guild, channel);
        int res  = 0;
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res = rs.getInt(1);
            }
            return res == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteChannel(String guild, String channel) {
        String query = String.format("DELETE FROM channel WHERE guild='%s' AND channel='%s'", guild, channel);
        try (Connection conn = mysqlDataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
