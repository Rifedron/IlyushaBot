package ru.miau.ilyushabot.functions.offers.objects;

public class Voter {
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
}
