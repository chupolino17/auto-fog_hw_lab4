package repositories.model.CNF_negation;

import java.util.ArrayList;
import java.util.List;


public class LiteralNegationModel {
    public LiteralNegationModel(List<LiteralMemberNegationModel> members) {this.literalMembers = members;}

    private List<LiteralMemberNegationModel> literalMembers;

    public LiteralNegationModel(){

    }

    public List<LiteralMemberNegationModel> getLiteralMembers() {return literalMembers;}

    public void setLiteralMembers(List<LiteralMemberNegationModel> literalMembers) {
        this.literalMembers = literalMembers;
    }

    public void setLiteral(LiteralMemberNegationModel newLiteral, int index) {
        literalMembers.set(index, newLiteral);
    }

    public void addMember(LiteralMemberNegationModel newMember) {
        if (literalMembers == null) {
            literalMembers = new ArrayList<>();
        }

        literalMembers.add(newMember);
    }

}
