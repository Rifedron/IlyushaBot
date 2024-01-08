package ru.miau.ilyushabot.functions.offers.objects;

import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.*;

public enum OfferStatus {

    IMPLEMENTED(Color.decode("#59ffac"), "Введено", "✨"),
    ACCEPTED(Color.decode("#07f71f"), "Одобрено", "✅"),
    DENIED(Color.RED, "Отклонено", "❌"),
    UNDER_CONSIDERATION(Color.decode("#ebc000"), "На рассмотрении", "\uD83D\uDD0D"),
    ALREADY_OFFERED(Color.decode("#cc6e16"), "Уже предложено", "\uD83D\uDC6C"),
    ALREADY_EXISTS(Color.decode("#5e825b"), "Уже реализовано ранее", "\uD83D\uDE10"),
    IGNORED(Color.WHITE, "В ожидании", "✨");

    public final Color color;
    public final String displayName;
    private final String emojiUnicode;

    public Emoji getEmoji() {
        return Emoji.fromUnicode(emojiUnicode);
    }

    OfferStatus(Color color, String displayName, String emojiUnicode) {
        this.color = color;
        this.displayName = displayName;
        this.emojiUnicode = emojiUnicode;
    }
    //    @Context(name = "Одобрить", type = Command.Type.MESSAGE)
//    void accept(MessageContextInteraction interaction) {
//        offerReply(interaction, Color.decode("#07f71f"), "Одобрено");
//    }
//    @Context(name = "Отклонить", type = Command.Type.MESSAGE)
//    void deny(MessageContextInteraction interaction) {
//        offerReply(interaction, Color.RED, "Отклонено");
//    }
//    @Context(name = "На рассмотрение", type = Command.Type.MESSAGE)
//    void underConsideration(MessageContextInteraction interaction) {
//        offerReply(interaction, Color.decode("#ebc000"), "На рассмотрении");
//    }
//    @Context(name = "Введено", type = Command.Type.MESSAGE)
//    void implemented(MessageContextInteraction interaction) {
//        offerReply(interaction, Color.decode("#59ffac"), "Введено");
//    }
//
//    @Context(name = "Установить комментарий", type = Command.Type.MESSAGE)
//    void setFeedback(MessageContextInteraction interaction) {
//        if (hasReplierRights(interaction.getMember())) {
//            interaction.replyModal(feedbackModal(interaction.getTarget().getId()))
//                    .queue();
//        } else interaction.reply("У вас нет прав на комментирование предложений")
//                .setEphemeral(true)
//                .queue();
//    }
}
