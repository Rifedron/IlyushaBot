package ru.miau.ilyushabot.functions.offers.objects;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.data.DataObject;
import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.data_storing.AbstractDAO;

import java.util.List;
import java.util.Map;

public class OfferDAO extends AbstractDAO<Offer> {

    public OfferDAO() {
        super("offers.yml");
    }

    @Override
    public Offer fromMap(Map map) {
        return new Offer((String) map.get(YamlKeys.OFFER_MESSAGE_ID), (String) map.get(YamlKeys.OFFER_AUTHOR_ID),
                ((List<Map<String, Object>>) map.get(YamlKeys.VOTERS_LIST)).stream().map(map1 ->
                        new Voter((String)
                                map1.get(YamlKeys.OFFER_VOTER_ID),
                                VoteType.valueOf((String) map1.get(YamlKeys.OFFER_VOTE_TYPE)))
                ).filter(voter -> voter.getId() != null).toList(),
                OfferStatus.valueOf((String) map.get(YamlKeys.OFFER_STATUS)),
                EmbedBuilder.fromData(DataObject.fromJson((String) map.get(YamlKeys.OFFER_EMBED_DATA))).build()
        );
    }
}
