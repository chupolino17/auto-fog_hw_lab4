package repositories.model.CNF_negation;

import java.util.ArrayList;
import java.util.List;

public class CNFNegationModel {
    private List<String> organizations;

    private List<LiteralNegationModel> literals;

    public CNFNegationModel(){
    }

    public List<LiteralNegationModel> getLiterals() {return literals;}

    public void setLiterals(List<LiteralNegationModel> literals) {
        this.literals = literals;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public void addModel(LiteralNegationModel literalModel) {
        if (literals == null) {
            literals = new ArrayList<>();
        }

        literals.add(literalModel);
    }

    public void addOrganization(String organization) {
        if (organizations == null) {
            organizations = new ArrayList<>();
        }

        organizations.add(organization);
    }

}
