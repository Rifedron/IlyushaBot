package ru.miau.ilyushabot.functions.offers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.functions.offers.objects.Offer;
import ru.miau.ilyushabot.functions.offers.objects.OfferDAO;
import ru.miau.ilyushabot.functions.offers.objects.OfferStatus;

public class Offers {
    public static OfferDAO offerDAO = new OfferDAO();
    public final Long replierRoleId = (Long) IlyushaBot.config.get("replierRoleId");
    public final Role replierRole = IlyushaBot.guild.getRoleById(replierRoleId);
    public final Long offersChannelId = (Long) IlyushaBot.config.get("channelId");
    public boolean hasReplierRights(Member member) {
        boolean allowed = false;
        for (Role role : member.getRoles()) {
            if (role.getPosition() >= replierRole.getPosition()) {
                allowed = true;
                break;
            }
        }
        return allowed;
    }
    public MessageEditAction editOfferMessage(Member replier, Offer offer, OfferStatus status) {
        Message message = IlyushaBot.jda.getTextChannelById(offersChannelId).retrieveMessageById(offer.getMessageId())
                .complete();
        EmbedBuilder embedBuilder = new EmbedBuilder(message.getEmbeds().get(0));
        embedBuilder
                .setTitle("Предложение | " + status.displayName)
                .setColor(status.color)
                .setFooter("Ответил "+replier.getEffectiveName(), replier.getEffectiveAvatarUrl());
        return message.editMessage(MessageEditData.fromMessage(message))
                .setEmbeds(embedBuilder.build())
                .setActionRow(message.getButtons());
    }
    public void offerStatusNotification(Member replier ,Offer offer, String status) {
        try {
            IlyushaBot.jda.getUserById(
                            Offers.offerDAO.getOfferByMessageId(offer.getMessageId()).getAuthorId()
                    ).openPrivateChannel().complete()
                    .sendMessageFormat(
                            "У вашего предложения %s обновлён статус на \"%s\" модератором %s",
                            offer.getOfferMessageUrl(), status, replier.getAsMention()
                    )
                    .queue();
        } catch (Throwable ignored) {}
    }
}
