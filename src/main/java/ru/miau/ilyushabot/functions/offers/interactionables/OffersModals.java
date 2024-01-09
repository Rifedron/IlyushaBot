package ru.miau.ilyushabot.functions.offers.interactionables;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.annotations.Modal;
import ru.miau.ilyushabot.functions.offers.Offers;
import ru.miau.ilyushabot.functions.offers.objects.Offer;
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;

public class OffersModals {
    private Offers config = new Offers();
    private Offers offers = new Offers();
    @Modal
    void offerFeedback(ModalInteraction interaction) {
        String offerMessageId = interaction.getModalId().split("\\|")[1];
        Offer offer = Offers.offerDAO.getOfferByMessageId(offerMessageId);
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

        User author = IlyushaBot.jda.getUserById(offerAuthorId);
        String messageURL = String.format(Message.JUMP_URL,
                interaction.getGuild().getId(),
                interaction.getChannel().getId(),
                offerMessageId);
        author.openPrivateChannel().complete()
                .sendMessageFormat("К вашему предложению %s добавлен комментарий от модератора %s",
                        messageURL, interaction.getMember().getAsMention())
                .queue();
        interaction.reply("Комментарий отправлен")
                .setEphemeral(true)
                .queue();
    }

    @Modal
    void alreadyExistingOffer(ModalInteraction interaction) {
        String offerMessageId = interaction.getValue("currentOfferId").getAsString();
        String originalOfferMessageId = interaction.getValue("originalOfferId").getAsString();
        Offer offer = Offers.offerDAO.getOfferByMessageId(offerMessageId);
        Offer originalOffer = Offers.offerDAO.getOfferByMessageId(originalOfferMessageId);
        if (originalOfferMessageId.equals(offerMessageId)) {
            interaction.reply("Указано одно и то же предложение")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (offer == null) {
            interaction.reply("Предложение не найдено")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (originalOffer == null) {
            interaction.reply("Оригинальное предложение не указано")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        MessageEditAction editAction = offers.editOfferMessage(interaction.getMember() ,offer, OfferStatus.ALREADY_OFFERED);
        editAction.setEmbeds(EmbedBuilder.fromData(editAction.getEmbeds().get(0).toData())
                .clearFields()
                .addField("Оригинальное предложение", String.format(Message.JUMP_URL,
                        interaction.getGuild().getId(),
                        interaction.getChannel().getId(),
                        originalOfferMessageId), true)
                        .build())
                .queue();
        offers.offerStatusNotification(interaction.getMember() ,offer, OfferStatus.ALREADY_OFFERED.displayName);
        Offers.offerDAO.updateStatusById(offerMessageId, OfferStatus.ALREADY_OFFERED);
        interaction.reply("Статус изменён")
                .setEphemeral(true)
                .queue();
    }
    @Modal
    void deleteOffer(ModalInteraction interaction) {
        String deletedOfferId = interaction.getModalId().split("\\|")[1];
        Offer deletedOffer = Offers.offerDAO.getOfferByMessageId(deletedOfferId);
        Message messageToDelete = interaction.getChannel().retrieveMessageById(deletedOfferId).complete();
        deletedOffer.getAuthor().openPrivateChannel().complete()
                .sendMessageFormat("Ваше предложение было удалено модератором %s\n" +
                                "**Причина: ```%s```**" +
                                "Удалённое предложение:",
                        interaction.getMember().getAsMention(),
                        interaction.getValue("deleteReason").getAsString())
                .addEmbeds(messageToDelete.getEmbeds().get(0))
                .queue();
        interaction.reply("Вы удалили предложение")
                .setEphemeral(true)
                .queue();
        messageToDelete.delete().queue();
        Offers.offerDAO.removeOffer(deletedOffer);
    }
}
