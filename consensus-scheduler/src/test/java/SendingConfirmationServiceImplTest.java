import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import services.SenderService;
import services.SendingConfirmationService;
import services.SendingConfirmationServiceImpl;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SendingConfirmationServiceImplTest {

    static SendingConfirmationService confirmationService;


    public SendingConfirmationServiceImplTest() {

    }

    @BeforeAll
    static void init() {
        confirmationService = new SendingConfirmationServiceImpl();
    }

    @Test
    public void sendCustomIdTest() {
        boolean consensusReached = confirmationService.sendForConfirmationCustomId(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json",
                0);

        assertTrue(consensusReached);
    }

    @Test
    public void sendCustomValuesTest() {
        boolean consensusReached = confirmationService.sendForConfirmationCustomValues(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json",
                1.0,
                3.0919090686339907);

        assertTrue(consensusReached);
    }

    @Test
    public void sendMinMessagesTest() {
        boolean consensusReached = confirmationService.sendForConfirmationMinMessages(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json");

        assertTrue(consensusReached);
    }

    @Test
    public void sendMaxProbabilityTest() {
        boolean consensusReached = confirmationService.sendForConfirmationMaxProbability(new SenderServiceImpl(),
                "src/test/resources/results_backwards.json");

        assertTrue(consensusReached);
    }

    static class SenderServiceImpl implements SenderService {
        @Override
        public int getReply(String orgName, int transactionId) {
            // random either accepts or rejects a transaction
            return ThreadLocalRandom.current().nextInt(0, 2);
        }

        @Override
        public boolean getResponse(double p) {
            double a = (new Random()).nextInt(10000) % 10000 + 1;
            return a <= p * 10000;
        }

        @Override
        public int getMaxRequestNum() {
            return 10;
        }

        @Override
        public int getMaxRequestTotalNum() {
            return 50;
        }

        @Override
        public long getTimeoutSec() {
            return 0;
        }

        @Override
        public long getWaitingTimeSec() {
            return 0;
        }

        @Override
        public String getLogPath() {
            return "src/test/resources/runTest.log";
        }
    }
}
