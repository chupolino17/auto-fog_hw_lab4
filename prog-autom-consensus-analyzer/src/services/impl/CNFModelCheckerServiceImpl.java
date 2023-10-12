package services.impl;

import exceptions.IncorrectCNFException;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import repositories.model.CNF_negation.CNFNegationModel;
import repositories.model.ModelCheckResult;
import services.CNFModelCheckerService;
import utils.CombinationsUtils;
import utils.ModelParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CNFModelCheckerServiceImpl implements CNFModelCheckerService {

    private CombinationsUtils combinationsUtils = new CombinationsUtils();

    private List<int[]> spec;

    private List<String> organizations;

    @Override
    public List<ModelCheckResult> getModelCheckingResult(String configProbabilityPath, String configSpecificationPath, String outputPath, boolean computeBackwards) {
        List<ModelCheckResult> result = new ArrayList<>();
        ModelParser modelParser = new ModelParser();

        try {
            CNFNegationModel cnfModel;


            File configFile = new File(configProbabilityPath);


            if (configSpecificationPath == null) {
                cnfModel = modelParser.parseCNFNegationModel(configFile);
            } else {
                File configSpecificationFile = new File(configSpecificationPath);

                cnfModel = modelParser.parseCNFNegationModel(configFile, configSpecificationFile);
            }

            Map<String, Double> probabilitiesMap = modelParser.parseAcceptanceProbabilities(configFile);

            List<Double> probabilities = new ArrayList<>(cnfModel.getOrganizations().size());

            organizations = cnfModel.getOrganizations();

            for (String orgName :
                    cnfModel.getOrganizations()) {
                probabilities.add(probabilitiesMap.get(orgName));
            }

            List<Set<int[]>> backwardTransitionsCombinations = new ArrayList<>();

            if (computeBackwards) {
                backwardTransitionsCombinations = combinationsUtils
                        .getBackwardTransitionsCombinations(cnfModel.getOrganizations().size());
            } else {
                Set<int[]> zeroBackwards = new HashSet<>(1);
                zeroBackwards.add(new int[]{});
                backwardTransitionsCombinations.add(zeroBackwards);
            }

            spec = modelParser.getSortedSpecifications(cnfModel);

            long epochTime = Instant.now().toEpochMilli();
            ;

            int folderCnt = 0;
            for (Set<int[]> backwardTransitions :
                    backwardTransitionsCombinations) {
                int modelId = backwardTransitions.hashCode();
                CNFModelGeneratorServiceImpl modelGenerator = new CNFModelGeneratorServiceImpl(spec, probabilities,
                        backwardTransitions);
                ModelCheckResult modelCheckResult = new ModelCheckResult(cnfModel.getOrganizations());
                modelCheckResult.setBackwardTransitions(backwardTransitions);
                modelCheckResult.setId(modelId);
                double probability = 0;
                double expectedMessages = 0;

                try {
                    // Create a log for PRISM output (hidden or stdout)
                    PrismLog mainLog = new PrismDevNullLog();
                    //PrismLog mainLog = new PrismFileLog("stdout");

                    // Initialise PRISM engine
                    Prism prism = new Prism(mainLog);
                    prism.initialise();

                    String filePath = String.format("%s/models/%d", outputPath, folderCnt);
                    String filePathName = filePath + "/dtmc.dot";
                    new File(filePath).mkdirs();

                    Path file = Paths.get(filePathName);
                    Files.write(file, Collections.emptyList(), StandardCharsets.UTF_8);


                    // Load the model generator into PRISM,
                    // export the model to a dot file (which triggers its construction)
                    prism.loadModelGenerator(modelGenerator);
                    prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File(filePathName));

                    // Then do some model checking and print the result
                    String[] props = new String[]{
                            "P=?[F \"goal\"]",
                            "R=?[F \"end\"]"
                    };

                    probability = Double.parseDouble(prism.modelCheck(props[0]).getResult().toString());
                    expectedMessages = Double.parseDouble(prism.modelCheck(props[1]).getResult().toString());

                    new File(filePath).renameTo(new File(String.format("%s/models/%s", outputPath, modelId)));

                    // Close down PRISM
                    prism.closeDown();

                    folderCnt++;


                } catch (FileNotFoundException | PrismException e) {
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                }

                modelCheckResult.setProbability(probability);
                modelCheckResult.setExpectedMessages(expectedMessages);
                modelCheckResult.setEpochTimestamp(epochTime);
                result.add(modelCheckResult);
            }
        } catch (IOException | IncorrectCNFException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<int[]> getSpec() {
        return spec;
    }

    public List<String> getOrganizations() {
        return organizations;
    }
}
