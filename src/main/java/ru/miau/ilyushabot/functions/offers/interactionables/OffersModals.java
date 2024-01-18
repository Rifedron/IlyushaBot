package ru.miau.ilyushabot.functions.offers.interactionables;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.annotations.Modal;
import ru.miau.ilyushabot.functions.offers.Offers;
import ru.miau.ilyushabot.functions.offers.objects.Offer;
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;

import static ru.miau.ilyushabot.IlyushaBot.jda;
import static ru.miau.ilyushabot.functions.offers.Offers.offerDAO;

public class OffersModals {
    private final Offers offers = new Offers();
    private final Long deletedOffersChannelId = (Long) IlyushaBot.config.get(YamlKeys.DELETED_OFFERS_CHANNEL_ID);
    @Modal
    void offerFeedback(ModalInteraction interaction) {
        String offerMessageId = interaction.getModalId().split("\\|")[1];
        Offer offer = offerDAO.get(offerMessageId);
        if (offer == null) {
            interaction.reply("Предложение не найдено")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String offerAuthorId = offer.getAuthorId();
        String feedBackMessage = interaction.getValue("offerFeedbackMessage").getAsString();
        interaction.getChannel().retrieveMessageById(offerMessageId)
                .queue(message -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.copyFrom(message.getEmbeds().get(0));
                    message.editMessageEmbeds(
                            embedBuilder
                                    .clearFields()
                                    .addField("Комментарий", feedBackMessage, false)
                                    .build()
                            )
                            .queue();
                });

        User author = jda.getUserById(offerAuthorId);
        String messageURL = String.format(Message.JUMP_URL,
                interaction.getGuild().getId(),
                interaction.getChannel().getId(),
                offerMessageId);
        interaction.reply("Комментарий отправлен")
                .setEphemeral(true)
                .queue();
        offerDAO.update();
        author.openPrivateChannel().complete()
                .sendMessageFormat("К вашему предложению %s добавлен комментарий от модератора %s",
                        messageURL, interaction.getMember().getAsMention())
                .queue();
    }

    @Modal
    void denyOffer(ModalInteraction interaction) {
        String deletedOfferId = interaction.getModalId().split("\\|")[1];
        Offer deniedOffer = offerDAO.get(deletedOfferId);
        Member replier = interaction.getMember();

        deniedOffer.setStatus(OfferStatus.DENIED);

        MessageEditAction messageEditAction = offers
                .editOfferMessage(replier, deniedOffer, OfferStatus.DENIED);
        messageEditAction.setEmbeds(new EmbedBuilder(messageEditAction.getEmbeds().get(0))
                        .clearFields()
                        .addField("Причина отказа", interaction.getValue("denyReason").getAsString(), false)
                        .build()
        ).queue();
        offers.offerStatusNotification(replier, deniedOffer, OfferStatus.DENIED.displayName);
        interaction.reply("Вы отказали предложению")
                .setEphemeral(true)
                .queue();

        offerDAO.update();

    }
    @Modal
    void deleteOffer(ModalInteraction interaction) {
        String deletedOfferId = interaction.getModalId().split("\\|")[1];
        Offer deletedOffer = offerDAO.get(deletedOfferId);
        Message messageToDelete = interaction.getChannel().retrieveMessageById(deletedOfferId).complete();
        interaction.reply("Вы удалили предложение")
                .setEphemeral(true)
                .queue();
        messageToDelete.delete().queue();

        offerDAO.remove(deletedOffer);

        MessageEmbed deletedEmbed = messageToDelete.getEmbeds().get(0);
        String replierMention = interaction.getMember().getAsMention();
        String deleteReason = interaction.getValue("deleteReason").getAsString();

        jda.getTextChannelById(deletedOffersChannelId)
                .sendMessageFormat("Удалено модератором %s\n"+
                        "**Причина:** ``%s``", replierMention, deleteReason)
                .addEmbeds(deletedEmbed)
                .queue();

        try {
            deletedOffer.getAuthor().openPrivateChannel().complete()
                    .sendMessageFormat("Ваше предложение было удалено модератором %s\n" +
                                    "**Причина: ``%s``**\n" +
                                    "Удалённое предложение:",
                             replierMention, deleteReason
                            )
                    .addEmbeds(deletedEmbed)
                    .queue();
        }catch (Throwable ignore) {}
    }
}
