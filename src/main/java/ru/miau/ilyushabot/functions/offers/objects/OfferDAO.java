package ru.miau.ilyushabot.functions.offers.objects;

import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfferDAO {
    private static final String OFFERS_FILE_NAME = "offers.yml";

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
        updateOffers();
    }
    private List<Offer> offers;

    public void add(Offer offer) {
        offers.add(offer);
        updateOffers();
    }
    public VoteChangeType vote(String messageToVoteId, String userId, VoteType voteType) {
        VoteChangeType changeType = getOfferByMessageId(messageToVoteId).vote(userId, voteType);
        updateOffers();
        return changeType;
    }
    public Offer getOfferByMessageId(String id) {
        for (Offer offer : offers) {
            if (offer.getMessageId().equals(id)) return offer;
        }
        return null;
    }
    public void removeOffer(Offer offer) {
        offers.remove(offer);
        updateOffers();
    }
    public void updateStatusById(String messageId, OfferStatus newStatus) {
        getOfferByMessageId(messageId).setStatus(newStatus);
        updateOffers();
    }

    public OfferDAO() {
        Yaml yaml = new Yaml();
        try {
            FileReader reader = new FileReader(OFFERS_FILE_NAME);
            this.offers = new ArrayList<>();
            List<Map<String, Object>> c = yaml.load(reader);
            if (c != null) {
                c.forEach(stringObjectMap -> {
                    String offerStatusName = (String)stringObjectMap.get("offerStatus");
                    OfferStatus status;
                    if (offerStatusName == null) status = OfferStatus.IGNORED;
                    else status = OfferStatus.valueOf(offerStatusName);
                    offers.add(new Offer(
                            (String) stringObjectMap.get("authorId"),
                            (String) stringObjectMap.get("messageId"),
                            (String) stringObjectMap.get("offerText"),
                            ((List<Map<String, Object>>) stringObjectMap.get("voters"))
                                    .stream().map(stringObjectMap1 -> new Voter(
                                            (String) stringObjectMap1.get("userId"),
                                            VoteType.valueOf(((String)stringObjectMap1.get("voteType")))
                                    )).toList(),
                            status
                            ));
                });
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateOffers() {
        Yaml yaml = new Yaml();
        try {
            FileWriter writer = new FileWriter(OFFERS_FILE_NAME);
            writer.write(yaml.dump(offers.stream().map(offer -> {
                Map<String, Object> offerMap = new HashMap();
                offerMap.put("messageId", offer.getMessageId());
                offerMap.put("authorId", offer.getAuthorId());
                offerMap.put("offerText", offer.getOfferText());
                offerMap.put("voters", offer.getVoters().stream().map(voter -> {
                    Map<String, Object> voterMap = new HashMap<>();
                    voterMap.put("userId", voter.getId());
                    voterMap.put("voteType", voter.getVoteType().name());
                    return voterMap;
                }).toList());
                offerMap.put("offerStatus", offer.getStatus().name());
                return offerMap;
            }).toList()));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
