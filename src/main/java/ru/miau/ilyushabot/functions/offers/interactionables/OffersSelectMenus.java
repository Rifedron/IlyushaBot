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
            if (option.equals("editFeedback")) {
                interaction.replyModal(feedbackModal(offerMessageId))
                        .queue();
            } else {
                OfferStatus newStatus = OfferStatus.valueOf(option);
                offer.setStatus(newStatus);
                offerReply(interaction, offer, newStatus);
            }
        } else {
            interaction.reply("У вас нет прав на рассмотрение предложений")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void offerReply(StringSelectInteraction interaction,Offer offer, OfferStatus status) {
        if (status.equals(OfferStatus.ALREADY_OFFERED)) {
            interaction.replyModal(alreadyExistingOfferModal(offer.getMessageId()))
                    .queue();
            return;
        }
        offers.editOfferMessage(interaction.getMember() ,offer, status).queue();
        offers.offerStatusNotification(interaction.getMember() ,offer, status.displayName);
        Offers.offerDAO.updateStatusById(offer.getMessageId(), status);

        interaction.replyModal(feedbackModal(offer.getMessageId()))
                .queue();
    }

    private static Modal feedbackModal(String messageId) {
        return Modal.create("offerFeedback", "Комментарий к предложению")
                .addActionRow(
                        TextInput.create("offerId", "ID предложения", TextInputStyle.SHORT)
                                .setValue(messageId)
                                .build()
                )
                .addActionRow(
                        TextInput.create("offerFeedbackMessage", "Текст ответа", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Причина ступид")
                                .build()
                ).build();
    }
    private static Modal alreadyExistingOfferModal(String messageId) {
        return Modal.create("alreadyExistingOffer", "Ответ на повторяющееся предложение")
                .addActionRow(
                        TextInput.create("currentOfferId", "ID повторного предложения", TextInputStyle.SHORT)
                                .setValue(messageId)
                                .build()
                )
                .addActionRow(
                        TextInput.create("originalOfferId", "ID оригинального предложения", TextInputStyle.SHORT)
                                .setPlaceholder("ID")
                                .build()
                ).build();
    }
}
