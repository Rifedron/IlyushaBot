package ru.miau.ilyushabot.functions.offers.objects;

import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.data_storing.SavableObject;

import java.util.HashMap;
import java.util.Map;

public class Voter implements SavableObject {
    private final String id;

    public String getId() {
        return id;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }

    private VoteType voteType;

    public Voter(String id, VoteType voteType) {
        this.id = id;
        this.voteType = voteType;
    }

    @Override
    public String getKey() {
        return id;
    }

    @Override
    public Map<String, Object> toSavableMap() {
        return new HashMap<>() {{
            put(YamlKeys.OFFER_VOTER_ID, id);
            put(YamlKeys.OFFER_VOTE_TYPE, voteType.name());
        }};
    }
    public static Voter fromMap(Map<String, Object> map) {
        return new Voter((String) map.get(
                YamlKeys.OFFER_VOTER_ID),
                VoteType.valueOf((String) map.get(YamlKeys.OFFER_VOTE_TYPE))
        );
    }
}
