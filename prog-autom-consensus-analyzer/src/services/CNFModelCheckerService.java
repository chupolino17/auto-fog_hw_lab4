package services;

import repositories.model.ModelCheckResult;

import java.util.List;

public interface CNFModelCheckerService {
    List<ModelCheckResult> getModelCheckingResult(String configProbabilityPath, String configSpecificationPath,
                                                  String outputPath, boolean computeBackwards);

    public List<int[]> getSpec();
}
