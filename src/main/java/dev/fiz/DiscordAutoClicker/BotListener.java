package dev.fiz.DiscordAutoClicker;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

public class BotListener extends ListenerAdapter {

    private Message message;
    private final Clicker clicker;

    public BotListener(Clicker clicker) {
        this.clicker = clicker;
    }

    public void sendInitialMessage(TextChannel channel) {
        MessageCreateData messageData = new MessageCreateBuilder()
                .setEmbeds(buildEmbed())
                .addActionRow(
                        Button.danger("exit", "\uD83D\uDCA3"),
                        Button.primary("increase", "⬆️"),
                        Button.primary("decrease", "⬇️"),
                        Button.success("enable", "🟢"),
                        Button.danger("disable", "🔴")
                ).build();

        this.message = channel.sendMessage(messageData).complete();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        User user = e.getUser();

        if (user.isBot()) return;
        if (e.getMessageIdLong() != message.getIdLong()) return;

        switch (e.getComponentId()) {
            case "exit":
                message.delete().queue($ -> {
                    System.exit(0);
                });
                break;
            case "increase":
                clicker.increaseCPS();
                e.editMessageEmbeds(buildEmbed()).queue();
                break;
            case "decrease":
                clicker.decreaseCPS();
                e.editMessageEmbeds(buildEmbed()).queue();
                break;
            case "enable":
                clicker.enable();
                e.editMessageEmbeds(buildEmbed()).queue();
                break;
            case "disable":
                clicker.disable();
                e.editMessageEmbeds(buildEmbed()).queue();
                break;
        }
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
                        + "Enabled: " + (clicker.isEnabled() ? "🟢" : "🔴") + "\n"
                        + "CPS: " + clicker.getCPS())
                .build();
    }
}
