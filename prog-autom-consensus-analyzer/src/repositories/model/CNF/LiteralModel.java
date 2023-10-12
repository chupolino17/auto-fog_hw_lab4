package repositories.model.CNF;

import java.util.ArrayList;
import java.util.List;


public class LiteralModel {
    public LiteralModel(List<String> members) {this.literalMembers = members;}

    private List<String> literalMembers;

    public LiteralModel(){

    }

    public List<String> getLiteralMembers() {return literalMembers;}

    public void setLiteralMembers(List<String> literalMembers) {
        this.literalMembers = literalMembers;
    }

    public void setLiteral(String newLiteral, int index) {
        literalMembers.set(index, newLiteral);
    }

    public void addMember(String newMember) {
        if (literalMembers == null) {
            literalMembers = new ArrayList<>();
        }

        literalMembers.add(newMember);
    }

}
