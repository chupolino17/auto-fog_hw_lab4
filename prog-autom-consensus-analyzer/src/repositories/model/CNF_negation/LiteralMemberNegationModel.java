package repositories.model.CNF_negation;

public class LiteralMemberNegationModel {
    private String memberName;
    private boolean isNegation;

    public String getMemberName() {
        return memberName;
    }

    public boolean isNegation() {
        return isNegation;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setNegation(boolean negation) {
        isNegation = negation;
    }
}
