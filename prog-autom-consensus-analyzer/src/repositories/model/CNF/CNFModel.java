package repositories.model.CNF;

import java.util.ArrayList;
import java.util.List;

public class CNFModel {
    private List<String> organizations;

    private List<LiteralModel> literals;

    public CNFModel(){

    }

    public List<LiteralModel> getLiterals() {return literals;}

    public void setLiterals(List<LiteralModel> literals) {
        this.literals = literals;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public void addModel(LiteralModel literalModel) {
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
