package ru.miau.ilyushabot;

public interface YamlKeys {

    //config.yml
    String GUILD_ID = "guildId";
    String OFFERS_CHANNEL_ID = "channelId";
    String OFFERS_REPLIER_ROLE_ID = "replierRoleId";
    String DELETED_OFFERS_CHANNEL_ID = "deletedOffersChannelId";
    String PRIVATE_VC_FABRIC_ID = "privateVcFabricId";

    //offers.yml
    String VOTERS_LIST = "voters";
    String OFFER_MESSAGE_ID = "messageId";
    String OFFER_AUTHOR_ID = "authorId";
    String OFFER_STATUS = "offerStatus";
    String OFFER_EMBED_DATA = "embedData";
    String OFFER_VOTE_TYPE = "voteType";
    String OFFER_VOTER_ID = "id";

    //privateVcs.yml
    String PRIVATE_VC_FIRST_CREATOR_ID = "firstOwnerId";
    String PRIVATE_VC_CURRENT_OWNER_ID = "currentOwnerId";
    String PRIVATE_VC_CHANNEL_ID = "channelId";


}
