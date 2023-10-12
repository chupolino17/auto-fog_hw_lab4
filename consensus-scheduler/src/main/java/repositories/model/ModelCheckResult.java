package repositories.model;
import java.util.*;

public class ModelCheckResult {
    List<String> organizations;
    private Set<int[]> backwardTransitions;
    private double probability;
    private double expectedMessages;
    private int id;
    private long epochTimestamp;

    public ModelCheckResult(){

    }

    public ModelCheckResult(List<String> organizations){
        this.organizations = organizations;
    }

    public double getExpectedMessages() {
        return expectedMessages;
    }

    public double getProbability() {
        return probability;
    }

    public Set<int[]>  getBackwardTransitions() {
        return backwardTransitions;
    }

    public int getId() {
        return id;
    }

    public long getEpochTimestamp() {
        return epochTimestamp;
    }

    public void setBackwardTransitions(Set<int[]>  backwardTransitions) {
        this.backwardTransitions = backwardTransitions;
    }

    public void setExpectedMessages(double expectedMessages) {
        this.expectedMessages = expectedMessages;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEpochTimestamp(long epochTimestamp) {
        this.epochTimestamp = epochTimestamp;
    }

    @Override
    public String toString() {
        StringBuilder backwards = new StringBuilder();
        backwards.append("[");
        backwardTransitions.forEach(bl-> {
            backwards.append("{");
            Arrays.stream(bl).forEach(b->{
                backwards.append(b);
                backwards.append(" ");
            });
            backwards.append("} ");
        });
        backwards.append("]");
        return "ModelCheckResult{" +
                "backwardTransitions=" + backwards +
                ", probability=" + probability +
                ", expectedMessages=" + expectedMessages +
                '}';
    }
}
