package services;

public interface EPVerifier {

    /**
     *
     * @param orgName
     * @return 1 if orgName accepted transactio, 0 if rejected
     */
    int getReply(String orgName, int transactionId);
}

