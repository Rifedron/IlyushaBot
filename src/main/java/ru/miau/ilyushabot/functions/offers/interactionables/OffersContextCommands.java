package ru.miau.ilyushabot.functions.offers.interactionables;


import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import ru.miau.ilyushabot.annotations.Context;
import ru.miau.ilyushabot.functions.offers.Offers;
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;

public class OffersContextCommands {
    private Offers config = new Offers();
    @Context(name = "Ответить на предложение", type = Command.Type.MESSAGE)
    void replyOffer(MessageContextInteraction interaction) {
        if (isOfferReplyValid(interaction)) {
            interaction.reply("Выберите опцию")
                    .addActionRow(newOfferStatusSelectMenu(interaction.getTarget()))
                    .setEphemeral(true)
                    .queue();
        }
    }

    private SelectMenu newOfferStatusSelectMenu(Message message) {
        StringSelectMenu.Builder selectMenu = StringSelectMenu.create("offerReplyMenu");
        for (OfferStatus status : OfferStatus.values()) {
            if (status != OfferStatus.IGNORED) {
                selectMenu.addOption(status.displayName, String.format("%s|%s", status.name(), message.getId()) , null, status.getEmoji());
            }
        }
        selectMenu.addOption("Изменить комментарий", String.format("%s|%s", "editFeedback", message.getId()), null, Emoji.fromUnicode("\uD83D\uDCDD"));
        selectMenu.addOption("Удалить предложение", String.format("%s|%s", "deleteOffer", message.getId()), null, Emoji.fromUnicode("\uD83D\uDDD1"));
        return selectMenu.build();
    }

    private boolean isOfferReplyValid(MessageContextInteraction interaction) {
        if (!config.hasReplierRights(interaction.getMember())) {
            interaction.reply("У вас нет прав на рассмотрение предложений")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        Message message = interaction.getTarget();
        if (Offers.offerDAO.getOfferByMessageId(message.getId()) == null) {
            interaction.reply("Сообщение не является предложением")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }
}
