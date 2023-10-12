package repositories.model;


import java.util.List;

public class ModelCheckerResultWrapper {
    List<ModelCheckResult> modelCheckResultList;
    List<int[]> specification;
    List<String> organizations;

    public List<int[]>  getSpecification() {
        return specification;
    }

    public List<ModelCheckResult> getModelCheckResultList() {
        return modelCheckResultList;
    }

    public void setModelCheckResultList(List<ModelCheckResult> modelCheckResultList) {
        this.modelCheckResultList = modelCheckResultList;
    }

    public void setSpecification(List<int[]> specification) {
        this.specification = specification;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }
}
