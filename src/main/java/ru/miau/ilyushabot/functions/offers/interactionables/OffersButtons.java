package ru.miau.ilyushabot.functions.offers.interactionables;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import ru.miau.ilyushabot.functions.offers.Offers;
import ru.miau.ilyushabot.functions.offers.objects.Offer;
import ru.miau.ilyushabot.functions.offers.objects.VoteChangeType;
import ru.miau.ilyushabot.functions.offers.objects.VoteType;

import java.util.List;

import static ru.miau.ilyushabot.functions.offers.Offers.offerDAO;

public class OffersButtons {

    @ru.miau.ilyushabot.annotations.Button
    void halal(ButtonInteraction interaction) {
        vote(interaction, VoteType.HALAL);
    }

    @ru.miau.ilyushabot.annotations.Button
    void haram(ButtonInteraction interaction) {
        vote(interaction, VoteType.HARAM);
    }


    private void vote(ButtonInteraction interaction, VoteType voteType) {
        String messageId = interaction.getMessageId();
        String memberId = interaction.getMember().getId();
        if (offerDAO.get(messageId).getAuthorId().equals(memberId)) {
            interaction.reply("Ты не можешь голосовать за собственное предложение")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        VoteChangeType changeType = offerDAO.get(messageId).vote(memberId, voteType);
        interaction.reply(switch (changeType) {
                    case CANCEL -> "Ваш голос отменён";
                    case CHANGE -> "Вы поменяли свой голос";
                    case FIRST -> "Ваш голос учтён";
                })
        .setEphemeral(true)
                .queue();
        updateButtons(interaction.getMessage());
        offerDAO.update();
    }
    private void updateButtons(Message message) {
        List<Button> buttons = message.getButtons();
        Button halalButton = buttons.get(0);
        Button haramButton = buttons.get(1);
        Offer offer = offerDAO.get(message.getId());

        buttons.set(0, Button.of(
                halalButton.getStyle(),
                halalButton.getId(),
                String.valueOf(offer.getVotersByType(VoteType.HALAL).size()),
                halalButton.getEmoji()
        ));
        buttons.set(1, Button.of(
                haramButton.getStyle(),
                haramButton.getId(),
                String.valueOf(offer.getVotersByType(VoteType.HARAM).size()),
                haramButton.getEmoji()
        ));
        message.editMessage(MessageEditData.fromMessage(message))
                .setActionRow(buttons)
                .queue();
    }
}

