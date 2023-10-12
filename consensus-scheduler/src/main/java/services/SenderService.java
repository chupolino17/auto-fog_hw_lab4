package services;

public interface SenderService {

    /**
     *
     * @param orgName to which one sends request for a transaction
     * @return 1 if orgName accepted transaction, 0 if rejected
     */
    int getReply(String orgName, int transactionId);

    boolean getResponse(double probability);

    /**
     *
     * @return max number of requests to the same organization. If scheduler sends this number of requests to the same,
     * before reaching the agreement, then scheduler stops and returns false.
     */
    int getMaxRequestNum();

    /**
     *
     * @return max number of requests in total. If scheduler sends this number of requests in total, then scheduler
     * stops and returns false.
     */
    int getMaxRequestTotalNum();

    /**
     * TODO: implement it.
     * @return max time in seconds which scheduler works. If timeout has been reached before reaching the agreement,
     * then scheduler stops and returns false.
     */
    long getTimeoutSec();

    /**
     * TODO: implement it.
     * @return time in seconds which scheduler waits until sending confirmation to the same organization.
     */
    long getWaitingTimeSec();

    /**
     *
     * @return path to log file where sending events with replies will be written.
     */
    String getLogPath();
}
