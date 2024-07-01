package dev.fiz.DiscordAutoClicker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.cli.*;

public class DiscordAutoClicker {


    private String TOKEN;
    private String GUILD_ID;
    private String CHANNEL_ID;


    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("T", "token", true, "Bot Token");
        options.addOption("G", "guild", true, "Guild ID");
        options.addOption("C", "channel", true, "Channel ID");

        CommandLine cmd = parser.parse(options, args);

        String token = cmd.getOptionValue("T");
        String guildID = cmd.getOptionValue("G");
        String channelID = cmd.getOptionValue("C");

        if (token == null || guildID == null || channelID == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar DiscordAutoClicker.jar -T <Token> -G <Guild id> -C <Channel id>", options);
            return;
        }

        new DiscordAutoClicker(token, guildID, channelID).start();
    }

    public DiscordAutoClicker(String token, String guildID, String channelID) {
        this.TOKEN = token;
        this.GUILD_ID = guildID;
        this.CHANNEL_ID = channelID;
    }

    private void start() throws Exception {
        JDA jda = JDABuilder.createDefault(TOKEN).build().awaitReady();

        Guild guild = jda.getGuildById(GUILD_ID);

        if (guild == null) {
            System.out.println("Could not find guild with ID " + GUILD_ID);
            System.exit(0);
            return;
        }

        TextChannel channel = guild.getTextChannelById(CHANNEL_ID);

        if (channel == null) {
            System.out.println("Could not find channel with ID " + CHANNEL_ID);
            System.exit(0);
            return;
        }

        Clicker clicker = new Clicker();
        BotListener botListener = new BotListener(clicker);

        botListener.sendInitialMessage(channel);

        jda.addEventListener(botListener);

        clicker.start();
    }
}