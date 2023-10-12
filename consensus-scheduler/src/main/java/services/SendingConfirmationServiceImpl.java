package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import repositories.model.ModelCheckResult;
import repositories.model.ModelCheckerResultWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendingConfirmationServiceImpl implements SendingConfirmationService {
    List<String> cashedOrganizations;

    Logger logger = Logger.getLogger("SchedulerLog");
    FileHandler fh;


    public SendingConfirmationServiceImpl() {

    }

    @Override
    public boolean sendForConfirmationCustomValues(SenderService senderService, String analyticsPath,
                                                   double probability, double messages) {
        return senderService.getResponse(probability);
    }

    @Override
    public boolean sendForConfirmationCustomId(SenderService senderService, String analyticsPath, int modelId) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ModelCheckerResultWrapper modelCheckerResultWrapper = objectMapper.readValue(new File(analyticsPath),
                    ModelCheckerResultWrapper.class);

            ModelCheckResult result = modelCheckerResultWrapper.getModelCheckResultList().stream()
                    .filter(r -> r.getId() == modelId)
                    .findAny()
                    .orElseThrow();

            return sendForConfirmation(senderService, result, modelCheckerResultWrapper, senderService.getLogPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // in case of error
        return false;
    }

    @Override
    public boolean sendForConfirmationMinMessages(SenderService senderService, String analyticsPath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ModelCheckerResultWrapper modelCheckerResultWrapper = objectMapper.readValue(new File(analyticsPath),
                    ModelCheckerResultWrapper.class);

            // TODO: if multiple results, then get with max probability
            ModelCheckResult result = modelCheckerResultWrapper.getModelCheckResultList().stream()
                    .min(Comparator.comparing(ModelCheckResult::getExpectedMessages))
                    .orElseThrow();

            return sendForConfirmation(senderService, result, modelCheckerResultWrapper, senderService.getLogPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // in case of error
        return false;
    }

    @Override
    public boolean sendForConfirmationMaxProbability(SenderService senderService, String analyticsPath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ModelCheckerResultWrapper modelCheckerResultWrapper = objectMapper.readValue(new File(analyticsPath),
                    ModelCheckerResultWrapper.class);

            // TODO: if multiple results, then get with min messages
            ModelCheckResult result = modelCheckerResultWrapper.getModelCheckResultList().stream()
                    .max(Comparator.comparing(ModelCheckResult::getProbability))
                    .orElseThrow();

            return sendForConfirmation(senderService, result, modelCheckerResultWrapper, senderService.getLogPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // in case of error
        return false;
    }

    private boolean sendForConfirmation(SenderService senderService, ModelCheckResult result,
                                        ModelCheckerResultWrapper modelCheckerResultWrapper, String logPath)
            throws IOException {
        fh = new FileHandler(logPath);
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        this.cashedOrganizations = modelCheckerResultWrapper.getOrganizations();

        boolean[] responses  = new boolean[cashedOrganizations.size()];


        Set<int[]> backwards = result.getBackwardTransitions();
        Set<int[]> specs = modelCheckerResultWrapper.getSpecification();

        Map<String, Integer> numberOfRequestsPerOrganization = new HashMap<>();
        cashedOrganizations.forEach(o -> numberOfRequestsPerOrganization.put(o, 0));

        return sendForConfirmationToOrganization(senderService, cashedOrganizations, responses, 0,
                0, specs, backwards, numberOfRequestsPerOrganization, result.getProbability());
    }

    private boolean sendForConfirmationToOrganization(SenderService senderService, List<String> organizations,
                                                      boolean[] responses, int responseIndex, int messagesCnt,
                                                      Set<int[]> spec, Set<int[]> backwards,
                                                      Map<String, Integer> numberOfRequestsPerOrganization,
                                                      double probability) {
        String orgToSend = organizations.get(0);

        int requestsNumToOrg = numberOfRequestsPerOrganization.get(orgToSend);

        numberOfRequestsPerOrganization.put(orgToSend, requestsNumToOrg + 1);

        boolean reply = senderService.getResponse(probability);
        logger.info(String.format("Send for confirmation to %s finished with response %b", orgToSend, reply));

        responses[responseIndex] = reply;
        return reply;

    }
}
