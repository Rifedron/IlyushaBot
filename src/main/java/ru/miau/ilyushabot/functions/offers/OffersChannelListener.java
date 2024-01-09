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
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;
import ru.miau.ilyushabot.functions.offers.objects.VoteType;

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
                MessageEmbed embed = offerEmbed(event.getMember(), messageContent).build();
                event.getChannel().sendMessageEmbeds(embed)
                        .addActionRow(
                                Button.of(ButtonStyle.SUCCESS, "halal", Emoji.fromUnicode("\uD83D\uDC4D")),
                                Button.of(ButtonStyle.DANGER, "haram", Emoji.fromUnicode("\uD83D\uDC4E"))
                        )
                        .queue(message1 ->
                            Offers.offerDAO.add(new Offer(
                                    message.getAuthor().getId(),
                                    message1.getId(),
                                    message.getContentRaw(),
                                    embed
                            ))

                        );
            }

        }
    }
    private EmbedBuilder offerEmbed(Member member, String content) {
        return new EmbedBuilder()
                .setColor(Color.WHITE)
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatar().getUrl())
                .setTitle("Предложение")
                .setDescription(content);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Offer offer = Offers.offerDAO.getOfferByMessageId(event.getMessageId());
        if (offer != null) {
            Message cloneOfDeletedMessage = event.getChannel().sendMessageEmbeds(offer.getOfferEmbed())
                    .setActionRow(
                            Button.of(ButtonStyle.SUCCESS, "halal",
                                    String.valueOf(offer.getVotersByType(VoteType.HALAL).size()), Emoji.fromUnicode("\uD83D\uDC4D")),
                            Button.of(ButtonStyle.DANGER, "haram",
                                    String.valueOf(offer.getVotersByType(VoteType.HARAM).size()), Emoji.fromUnicode("\uD83D\uDC4E"))
                    )
                    .complete();
            offer.setMessageId(cloneOfDeletedMessage.getId());
            Offers.offerDAO.updateOffers();
            offer.getAuthor()
                    .openPrivateChannel().complete()
                    .sendMessageFormat("Ваше предложение %s попытались удалить без причины\n" +
                            "Вы имеете право разобраться в ситуации", cloneOfDeletedMessage.getJumpUrl())
                    .queue();
        }
    }
}
