package ru.miau.ilyushabot.functions.offers.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.data_storing.SavableObject;
import ru.miau.ilyushabot.functions.offers.Offers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Offer implements SavableObject {
    private final String authorId;

    public String getMessageId() {
        return messageId;
    }

    private String messageId;

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    private OfferStatus status;
    private MessageEmbed offerEmbed;

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
    public User getAuthor() {
        return IlyushaBot.jda.getUserById(authorId);
    }
    public String getOfferText() {
        return offerEmbed.getDescription();
    }

    public Offer(String authorId, String messageId, MessageEmbed embed) {
        this.authorId = authorId;
        this.messageId = messageId;
        this.status = OfferStatus.IGNORED;
        if (embed != null) this.offerEmbed = embed;
    }
    public Offer(String authorId, String messageId, List<Voter> voters, OfferStatus status, MessageEmbed embed) {
        this(authorId, messageId, embed);
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

    public MessageEmbed getOfferEmbed() {
        return offerEmbed;
    }

    public void setOfferEmbed(MessageEmbed offerEmbed) {
        this.offerEmbed = offerEmbed;
    }

    @Override
    public String getKey() {
        return messageId;
    }
    @Override
    public Map<String, Object> toSavableMap() {
        return new HashMap<>() {{
            put(YamlKeys.OFFER_MESSAGE_ID, messageId);
            put(YamlKeys.OFFER_AUTHOR_ID, authorId);
            put(YamlKeys.VOTERS_LIST, voters.stream().map(voter -> voter.toSavableMap()).toList());
            put(YamlKeys.OFFER_STATUS, status.name());
            put(YamlKeys.OFFER_EMBED_DATA, offerEmbed.toData().toString());
        }};
    }
}
