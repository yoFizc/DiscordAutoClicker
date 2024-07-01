package dev.fiz.DiscordAutoClicker;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import org.jnativehook.GlobalScreen;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordAutoClicker {

    private static final String CONFIG_FILE = "src/main/resources/Config.yml";

    private String TOKEN;
    private String GUILD_ID;
    private String CHANNEL_ID;

    private boolean state = false;
    private boolean holding = false;
    private boolean skipNextPress = false;
    private boolean skipNextRelease = false;
    private boolean chill = false;
    private int cps = 10;

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
        JDA jda = JDABuilder.createDefault(TOKEN)
                .build()
                .awaitReady();

        Guild guild = jda.getGuildById(GUILD_ID);

        if (guild == null) {
            System.out.println("Could not find guild with ID "+GUILD_ID);
            System.exit(0);
            return;
        }

        TextChannel channel = guild.getTextChannelById(CHANNEL_ID);

        if (channel == null) {
            System.out.println("Could not find channel with ID " + CHANNEL_ID);
            System.exit(0);
            return;
        }

        MessageCreateData messageData = new MessageCreateBuilder()
                .setEmbeds(buildEmbed())
                .addActionRow(
                        Button.danger("exit", "\uD83D\uDCA3"),
                        Button.primary("increase", "⬆️"),
                        Button.primary("decrease", "⬇️"),
                        Button.success("enable", "🟢"),
                        Button.danger("disable", "🔴")
                ).build();

        Message message = channel.sendMessage(messageData).complete();

        jda.addEventListener(new ListenerAdapter() {

            @Override
            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                User user = event.getUser();

                if (user.isBot()) return;
                if (event.getMessageIdLong() != message.getIdLong()) return;

                switch (event.getComponentId()) {
                    case "exit":
                        message.delete().queue($ -> {
                            System.exit(0);
                        });
                        break;
                    case "increase":
                        cps = Math.min(30, cps + 1);
                        event.editMessageEmbeds(buildEmbed()).queue();
                        break;
                    case "decrease":
                        cps = Math.max(5, cps - 1);
                        event.editMessageEmbeds(buildEmbed()).queue();
                        break;
                    case "enable":
                        state = true;
                        event.editMessageEmbeds(buildEmbed()).queue();
                        break;
                    case "disable":
                        state = false;
                        event.editMessageEmbeds(buildEmbed()).queue();
                        break;
                }
            }
        });

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        GlobalScreen.registerNativeHook();

        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent event) {
            }

            @Override
            public void nativeMousePressed(NativeMouseEvent event) {
                if (event.getButton() == NativeMouseEvent.BUTTON1) {
                    if (skipNextPress) {
                        skipNextPress = false;
                    } else {
                        holding = true;
                        chill = true;
                    }
                }
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent event) {
                if (event.getButton() == NativeMouseEvent.BUTTON1) {
                    if (skipNextRelease) {
                        skipNextRelease = false;
                    } else {
                        holding = false;
                    }
                }
            }
        });

        Robot robot = new Robot();
        Random random = new Random();

        while (true) {
            if (state && holding) {
                if (!chill) {
                    skipNextPress = true;
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                }

                long ms = calculateRandomInterval(cps, random);

                Thread.sleep(ms / 2);

                if (!chill) {
                    skipNextRelease = true;
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }

                Thread.sleep(ms / 2);

                chill = false;
            }

            Thread.sleep(1);
        }
    }

    private long calculateRandomInterval(int cps, Random random) {
        double baseInterval = 1000.0 / cps;
        double randomFactor = 0.5 + (1.5 * random.nextDouble());
        return (long) (baseInterval * randomFactor);
    }



    private MessageEmbed buildEmbed() {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Press the buttons below to control the auto clicker\n"
                        + "\uD83D\uDCA3 Exit the auto clicker\n"
                        + "⬆️ Increase your CPS\n"
                        + "⬇️ Decrease your CPS\n"
                        + "🟢 Enable the auto clicker\n"
                        + "🔴 Disable the auto clicker\n"
                        + "\n"
                        + "Enabled: " + (state ? "🟢" : "🔴") + "\n"
                        + "CPS: " + cps)
                .build();
    }
}
