package utils;

import exceptions.IncorrectCNFException;
import repositories.model.CNF.CNFModel;
import repositories.model.CNF.LiteralModel;
import repositories.model.CNF_negation.CNFNegationModel;
import repositories.model.CNF_negation.LiteralMemberNegationModel;
import repositories.model.CNF_negation.LiteralNegationModel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import prism.PrismException;
import parser.ast.Expression;
import parser.PrismParser;
import parser.BooleanUtils;
import prism.Prism;

public class ModelParser {
    public CNFNegationModel parseCNFNegationModel(File configProbabilitiesFile, File specificationFile) throws IncorrectCNFException, IOException {
        CNFNegationModel cnfNeg = parseCNFNegationModel(specificationFile);

        Map<String, Double> probabilities = parseAcceptanceProbabilities(configProbabilitiesFile);
        List<String> orgs = new ArrayList<>(probabilities.keySet());

        for (LiteralNegationModel LNM : cnfNeg.getLiterals()) {
            LNM.getLiteralMembers().sort(Comparator.comparing(item -> orgs.indexOf(item.getMemberName())));
        }

        BufferedReader lineReader = new BufferedReader(new FileReader(specificationFile));
        String lineText;
        int index = 0;

        while ((lineText = lineReader.readLine()) != null) {
            if (lineText.startsWith("    Rule:")) {
                String realLiterals = parseAndConvertToDNF(lineText.substring(11, lineText.length() - 1));

                for (int i = 1; i < realLiterals.length(); ++i) {
                    boolean negFlag = false;
                    StringBuilder negative = null;
                    int spaceCount = 0;

                    if (realLiterals.charAt(i - 1) == '(') {
                        while (realLiterals.charAt(i) != ')') {

                            if (realLiterals.charAt(i) == '!') {
                                negative = new StringBuilder();
                                negFlag = true;
                            }

                            if (negFlag) {
                                negative.append(realLiterals.charAt(i));

                                if (realLiterals.charAt(i) == ' ') {
                                    ++spaceCount;
                                }
                            }

                            if (negFlag && (realLiterals.charAt(i + 1) == ')' || realLiterals.charAt(i + 1) == ' ') && spaceCount > 0) {
                                for (LiteralMemberNegationModel LMNM : cnfNeg.getLiterals().get(index).getLiteralMembers()) {
                                    if (Objects.equals(negative.substring(2), LMNM.getMemberName())) {
                                        LMNM.setMemberName(negative.toString());
                                        LMNM.setNegation(true);
                                    }
                                }

                                negFlag = false;
                                spaceCount = 0;
                            }

                            ++i;
                        }

                        ++ index;
                    }
                }
            }
        }

        for (String org : probabilities.keySet()) {
            cnfNeg.addOrganization(org);
        }

        return cnfNeg;
    }

    public CNFNegationModel parseCNFNegationModel(File configFile) throws IncorrectCNFException, IOException {
        CNFNegationModel cnfNeg = new CNFNegationModel();
        CNFModel cnf = parseCNFModel(configFile);

        cnfNeg.setOrganizations(cnf.getOrganizations());

        for (LiteralModel literalModel : cnf.getLiterals()) {
            LiteralNegationModel literalNegationModel = new LiteralNegationModel();

            for (String str : literalModel.getLiteralMembers()) {
                LiteralMemberNegationModel memberNegationModel = new LiteralMemberNegationModel();
                if (str.startsWith("!")) {
                    memberNegationModel.setNegation(true);
                    memberNegationModel.setMemberName(str.substring(2));
                } else {
                    memberNegationModel.setNegation(false);
                    memberNegationModel.setMemberName(str);
                }

                literalNegationModel.addMember(memberNegationModel);
            }

            cnfNeg.addModel(literalNegationModel);
        }

        return cnfNeg;
    }

    public CNFModel parseCNFModel(File configFile) throws IOException, IncorrectCNFException {
        CNFModel cnf = new CNFModel();

        BufferedReader lineReader = new BufferedReader(new FileReader(configFile));
        String lineText;

        while ((lineText = lineReader.readLine()) != null) {
            if (lineText.startsWith("    Name:")) {
                cnf.addOrganization(lineText.substring(10));
            }

            if (lineText.startsWith("    Rule:")) {
                String realLiterals = parseAndConvertToDNF(lineText.substring(11, lineText.length() - 1));
                StringBuilder str = new StringBuilder();

                System.out.println("Specification DNF form: " + realLiterals);

                for (int i = 1; i < realLiterals.length(); ++i) {
                    if (realLiterals.charAt(i - 1) == '(') {
                        while (realLiterals.charAt(i) != ')') {
                            str.append(realLiterals.charAt(i));
                            ++i;
                        }

                        final Set<String> setToReturn = new LinkedHashSet<>();
                        final Set<String> tempSet = new HashSet<>();

                        for (String tempStr : str.toString().split("&")) {
                            String realLiteral = tempStr;

                            if (tempStr.contains(" ")) {
                                tempStr = tempStr.substring(tempStr.lastIndexOf(" ") + 1);
                            }

                            if (tempSet.add(tempStr)) {
                                setToReturn.add(realLiteral);
                            } else {
                                throw new IncorrectCNFException("Invalid CNF formula");
                            }
                        }

                        LiteralModel literals = new LiteralModel(new ArrayList<>(tempSet));

                        if (cnf.getOrganizations() != null) {
                            literals.getLiteralMembers().sort(Comparator.comparing(item -> cnf.getOrganizations().indexOf(item)));

                            for (int j = 0; j < literals.getLiteralMembers().size(); j++) {
                                if (!setToReturn.contains(literals.getLiteralMembers().get(j))) {
                                    StringBuilder tempStr = new StringBuilder(literals.getLiteralMembers().get(j));
                                    literals.setLiteral(tempStr.insert(0, "! ").toString(), j);
                                }
                            }
                        }

                        cnf.addModel(literals);
                        str.setLength(0);
                    }
                }
            }
        }

        return cnf;
    }

    public Map<String, Double> parseAcceptanceProbabilities(File configFile) throws IOException, IncorrectCNFException {
        Map<String, Double> orgProbabilities = new TreeMap<>();
        BufferedReader lineReader = new BufferedReader(new FileReader(configFile));
        String lineText;
        String org = null;
        double probability;

        while ((lineText = lineReader.readLine()) != null) {
            if (lineText.startsWith("    Name:")) {
                org = lineText.substring(10);
            }

            if (lineText.startsWith("    Pr:")) {
                probability = Double.parseDouble(lineText.substring(8));
                orgProbabilities.put(org, probability);
            }
        }

        StringBuilder errorMessage = new StringBuilder();

        for (Map.Entry<String, Double> orgProbs : orgProbabilities.entrySet()) {
            if (orgProbs.getValue() > 1) {
                if (!errorMessage.toString().equals("")) {
                    errorMessage.append(", ");
                }

                errorMessage.append("probability of ").append(orgProbs.getKey()).append(" is greater than one");
            } else if (orgProbs.getValue() < 0) {
                if (!errorMessage.toString().equals("")) {
                    errorMessage.append(",");
                }

                errorMessage.append("probability of ").append(orgProbs.getKey()).append(" is less than zero");
            }
        }

        if (!errorMessage.toString().equals("")) {
            throw new IncorrectCNFException(errorMessage.toString());
        }

        return orgProbabilities;
    }

    // TODO: check that negation works correct on different examples
    public List<int[]> getSortedSpecifications(CNFNegationModel cnfModel) {
        List<int[]> spec = new ArrayList<>();
        List<String> orgNames = cnfModel.getOrganizations();

        for (LiteralNegationModel literal : cnfModel.getLiterals()) {
            int[] specLiteral = new int[cnfModel.getOrganizations().size()];
            Arrays.fill(specLiteral, 1);
            List<int[]> temp = new ArrayList<>();
            temp.add(specLiteral);

            for (int i = 0; i < orgNames.size(); i++) {
                String name = orgNames.get(i);
                Optional<LiteralMemberNegationModel> literalMemberCandidate = literal.getLiteralMembers().stream()
                        .filter(member -> member.getMemberName().equals(name))
                        .findAny();

                if (literalMemberCandidate.isEmpty() || literalMemberCandidate.get().isNegation()) {
                    List<int[]> tempClones = new ArrayList<>();
                    for (int[] t :
                            temp) {
                        int[] tClone = t.clone();
                        tClone[i] = 0;
                        tempClones.add(tClone);
                    }
                    temp.addAll(tempClones);
                }
            }

            spec.addAll(temp);
        }

        return spec;
    }

    public String parseAndConvertToDNF(String formulaToConvert) {
        StringBuilder sb = new StringBuilder();

        try {
            Expression expr = Prism.parseSingleExpressionString(formulaToConvert);
            String formulaDNFForm = BooleanUtils.convertToDNF(expr.deepCopy()).toString();

            sb.append("(");
            for (int i = 0; i < formulaDNFForm.length(); i++) {
                if (formulaDNFForm.charAt(i) == '|') {
                    sb.append(")");
                    sb.append("|");
                    sb.append("(");
                } else {
                    sb.append(formulaDNFForm.charAt(i));
                }
            }
            sb.append(")");

        } catch (PrismException  e) {
            System.out.println(String.format("Unable to convert formula *%s* to CNF format: ",
                    formulaToConvert) + e.getMessage());
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IncorrectCNFException, IOException {
        final String SIMPLE_SYSTEM_CONFIG_PATH = "../resources/cnf-1/cnf_1_properties.yaml";
        final String SIMPLE_SYSTEM_CONFIG_PATH2 = "../resources/cnf-1/cnf_1_spec.yaml";
        
        File file = new File(SIMPLE_SYSTEM_CONFIG_PATH);
        File file2 = new File(SIMPLE_SYSTEM_CONFIG_PATH2);

        CNFNegationModel cnfNeg = new ModelParser().parseCNFNegationModel(file, file2);

        for (LiteralNegationModel LNM : cnfNeg.getLiterals()) {
            System.out.println("Literal...");
            for (LiteralMemberNegationModel LMNM : LNM.getLiteralMembers()) {
                System.out.println(LMNM.isNegation() + " " + LMNM.getMemberName());
            }
        }

        System.out.println();

        for (String org : cnfNeg.getOrganizations()) {
            System.out.println(org);
        }
    }
}
