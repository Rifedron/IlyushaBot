package ru.miau.ilyushabot.functions.offers.objects;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.functions.offers.Offers;

import java.util.ArrayList;
import java.util.List;

public class Offer {
    private final String authorId;

    public String getMessageId() {
        return messageId;
    }

    private final String messageId;
    private final String offerText;
    private OfferStatus status;

    public List<Voter> getVoters() {
        return voters;
    }
    public List<Voter> getVotersByType(VoteType type) {
        return voters.stream().filter(voter -> voter.getVoteType().equals(type)).toList();
    }
    public VoteChangeType vote(String userId, VoteType voteType) {
        for (Voter voter : voters) {
            if (voter.getId().equals(userId)) {
                if (voter.getVoteType().equals(voteType)) {
                    voters.remove(voter);
                    return VoteChangeType.CANCEL;
                }
                else voter.setVoteType(voteType);
                return VoteChangeType.CHANGE;
            }
        }
        voters.add(new Voter(userId, voteType));
        return VoteChangeType.FIRST;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getOfferText() {
        return offerText;
    }

    public Offer(String authorId, String messageId, String offerText) {
        this.authorId = authorId;
        this.messageId = messageId;
        this.offerText = offerText;
        this.status = OfferStatus.IGNORED;
    }
    public Offer(String authorId, String messageId, String offerText, List<Voter> voters, OfferStatus status) {
        this(authorId, messageId, offerText);
        this.voters = new ArrayList<>(voters);
        if (status != null) this.status = status;
    }

    List<Voter> voters = new ArrayList<>();

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public String getOfferMessageUrl() {
        TextChannel channel = IlyushaBot.jda.getTextChannelById((Long) IlyushaBot.config.get("channelId"));
        return channel.getJumpUrl()+"/"+getMessageId();
    }
    public Member getMember() {
        return IlyushaBot.guild.getMemberById(getAuthorId());
    }
    public Message getMessage() {
        return IlyushaBot.jda.getTextChannelById(new Offers().offersChannelId).retrieveMessageById(messageId).complete();
    }
}
