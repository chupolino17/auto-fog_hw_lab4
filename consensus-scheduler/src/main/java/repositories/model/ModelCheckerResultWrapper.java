package repositories.model;

import java.util.*;

public class ModelCheckerResultWrapper {
    List<ModelCheckResult> modelCheckResultList;
    Set<int[]> specification;
    List<String> organizations;

    public ModelCheckerResultWrapper(){
    }

    public Set<int[]>  getSpecification() {
        return specification;
    }

    public List<ModelCheckResult> getModelCheckResultList() {
        return modelCheckResultList;
    }

    public void setModelCheckResultList(List<ModelCheckResult> modelCheckResultList) {
        this.modelCheckResultList = modelCheckResultList;
    }

    public void setSpecification(Set<int[]> specification) {
        this.specification = specification;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }
}
