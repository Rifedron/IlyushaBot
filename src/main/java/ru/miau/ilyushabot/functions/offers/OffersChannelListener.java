package ru.miau.ilyushabot.functions.offers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.functions.offers.objects.Offer;

import java.awt.*;

public class OffersChannelListener extends ListenerAdapter {
    private final Long offersChannelId = (Long) IlyushaBot.config.get("channelId");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getChannel().getIdLong() == offersChannelId && !event.getAuthor().isBot()) {
            Message message = event.getMessage();
            String messageContent = message.getContentRaw();
            if (!messageContent.startsWith("-")) {
                message.delete().queue();
                event.getChannel().sendMessageEmbeds(offerEmbed(event.getMember(), messageContent))
                        .addActionRow(
                                Button.of(ButtonStyle.SUCCESS, "halal", Emoji.fromUnicode("\uD83D\uDC4D")),
                                Button.of(ButtonStyle.DANGER, "haram", Emoji.fromUnicode("\uD83D\uDC4E"))
                        )
                        .queue(message1 ->
                            Offers.offerDAO.add(new Offer(
                                    message.getAuthor().getId(),
                                    message1.getId(),
                                    message.getContentRaw()
                            ))
                        );
            }

        }
    }
    private MessageEmbed offerEmbed(Member member, String content) {
        return new EmbedBuilder()
                .setColor(Color.WHITE)
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatar().getUrl())
                .setTitle("Предложение")
                .setDescription(content)
                .build();
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Offer offer = Offers.offerDAO.getOfferByMessageId(event.getMessageId());
        if (offer != null) {
            Offers.offerDAO.removeOffer(offer);
            System.out.println("Предложение \""+offer.getOfferText()+"\" удалено");
        }
    }
}
