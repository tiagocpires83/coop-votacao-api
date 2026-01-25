package br.com.coop_votocao_api.client;

public interface CpfValidationClient {

    VotingStatus validate(String cpf);

    enum VotingStatus {
        ABLE_TO_VOTE,
        UNABLE_TO_VOTE
    }
}
