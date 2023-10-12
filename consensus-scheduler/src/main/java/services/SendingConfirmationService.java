package services;

public interface SendingConfirmationService {
    boolean sendForConfirmationCustomValues(SenderService senderService, String analyticsPath,
                                            double probability, double messages);

    boolean sendForConfirmationCustomId(SenderService senderService, String analyticsPath, int modelId);

    boolean sendForConfirmationMinMessages(SenderService senderService, String analyticsPath);

    boolean sendForConfirmationMaxProbability(SenderService senderService, String analyticsPath);
}
