package ru.miau.ilyushabot.functions.offers.interactionables;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import ru.miau.ilyushabot.annotations.SelectMenu;
import ru.miau.ilyushabot.functions.offers.Offers;
import ru.miau.ilyushabot.functions.offers.objects.Offer;
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;

public class OffersSelectMenus {
    private final Offers offers = new Offers();

    @SelectMenu
    void offerReplyMenu(StringSelectInteraction interaction) {
        if (offers.hasReplierRights(interaction.getMember())) {
            String[] selectedValue = interaction.getValues().get(0).split("\\|");
            String option = selectedValue[0];
            String offerMessageId = selectedValue[1];
            Offer offer = Offers.offerDAO.getOfferByMessageId(offerMessageId);
            switch (option) {
                case "editFeedback" -> interaction.replyModal(feedbackModal(offerMessageId))
                        .queue();
                case "deleteOffer" -> interaction.replyModal(deleteReasonModal(offerMessageId))
                        .queue();
                default -> {
                    OfferStatus newStatus = OfferStatus.valueOf(option);
                    offer.setStatus(newStatus);
                    offerReply(interaction, offer, newStatus);
                }
            }
        } else {
            interaction.reply("У вас нет прав на рассмотрение предложений")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void offerReply(StringSelectInteraction interaction,Offer offer, OfferStatus status) {
        offers.editOfferMessage(interaction.getMember() ,offer, status).queue();
        offers.offerStatusNotification(interaction.getMember() ,offer, status.displayName);
        Offers.offerDAO.updateStatusById(offer.getMessageId(), status);

        interaction.replyModal(feedbackModal(offer.getMessageId()))
                .queue();
    }

    private static Modal feedbackModal(String messageId) {
        return Modal.create("offerFeedback|"+messageId, "Комментарий к предложению")
                .addActionRow(
                        TextInput.create("offerFeedbackMessage", "Текст ответа", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Причина ступид")
                                .build()
                ).build();
    }
    private static Modal deleteReasonModal(String messageId) {
        return Modal.create("deleteOffer|"+messageId, "Комментарий к предложению")
                .addActionRow(
                        TextInput.create("deleteReason", "Причина ответа", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Причина мегаступид")
                                .build()
                ).build();
    }
}
