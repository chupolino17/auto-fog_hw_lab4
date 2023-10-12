package services.impl;

import parser.State;
import parser.ast.DeclarationInt;
import parser.ast.DeclarationType;
import parser.ast.Expression;
import parser.type.Type;
import parser.type.TypeInt;
import prism.ModelGenerator;
import prism.ModelType;
import prism.PrismException;
import prism.RewardGenerator;
import repositories.model.CNF.CNFModel;
import repositories.model.CNF.LiteralModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CNFModelGeneratorServiceImpl implements ModelGenerator, RewardGenerator {
    // Size of the tree (state x is in [0,...,n])
    private int n;
    //binary representation of CNF
    List<int[]> scpec;
    //probability to accept. The order must be the same like in literals of specification
    List<Double> probabilities;
    // replied of organizations. 1 - confirmation, 0 - refusal
    public int responses[];
    Set<int[]> backwards;
    private State exploreState;
    // Current value of x
    private int x;

    public CNFModelGeneratorServiceImpl(List<int[]> spec, List<Double> probabilities, Set<int[]> backwards) {
        this.n = probabilities.size();
        this.probabilities = probabilities;
        this.scpec = spec;
        this.backwards = backwards;

        responses = new int[n];
    }

    // Methods for ModelInfo interface

    // The model is a discrete-time Markov chain (DTMC)

    @Override
    public ModelType getModelType() {
        return ModelType.DTMC;
    }

    @Override
    public List<String> getVarNames() {
        List<String> varNames = new ArrayList<>();
        varNames.add("x");
        for (int i = 0; i < n; i++) {
            varNames.add("o" + i);
        }
        return varNames;
    }

    @Override
    public List<Type> getVarTypes() {
        return Arrays.asList(TypeInt.getInstance());
    }

    @Override
    public DeclarationType getVarDeclarationType(int i) {
        return new DeclarationInt(Expression.Int(-1), Expression.Int(n));

    }

    @Override
    public List<String> getLabelNames() {
        List<String> names = new ArrayList<>(2);
        names.add("goal");
        names.add("end");
        return names;
    }

    // Methods for ModelGenerator interface (rather than superclass ModelInfo)

    @Override
    public State getInitialState() throws PrismException {
        State initialState = new State(n + 1);

        initialState.setValue(0, 0);

        for (int i = 1; i <= n; i++) {
            initialState.setValue(i, -1);
        }

        return initialState;
    }

    @Override
    public void exploreState(State exploreState) throws PrismException {
        // Store the state (for reference, and because will clone/copy it later)
        this.exploreState = exploreState;
        // Cache the value of x in this state for convenience
        x = (Integer) exploreState.varValues[0];

        for (int i = 0; i < n; i++) {
            responses[i] = (Integer) exploreState.varValues[i + 1];
        }
    }

    @Override
    public int getNumChoices() throws PrismException {
        // This is a DTMC so always exactly one nondeterministic choice (i.e. no nondeterminism)
        return 1;
    }

    @Override
    public int getNumTransitions(int i) throws PrismException {
        // End points have a self-loop, all other states have 2 transitions, left and right
        // (Note that i will always be 0 since this is a Markov chain)
        return (x == n) ? 1 : 2;
    }

    @Override
    public Object getTransitionAction(int i, int offset) throws PrismException {
        // No action labels in this model
        return null;
    }

    @Override
    public double getTransitionProbability(int i, int offset) throws PrismException {
        // End points have a self-loop (with probability 1)
        // All other states go left with probability 1-p and right with probability p
        // We assume that these are transitions 0 and 1, respectively
        // (Note that i will always be 0 since this is a Markov chain)

        if (x < n) {
            return offset == 0 ? 1 - probabilities.get(x) : probabilities.get(x);
        } else return 1;
    }

    @Override
    public State computeTransitionTarget(int i, int offset) throws PrismException {
        // End points have a self-loop (with probability 1)
        // All other states go left with probability 1-p and right with probability p
        // We assume that these are transitions 0 and 1, respectively
        // (Note that i will always be 0 since this is a Markov chain)
        State target = new State(exploreState);
        if (x == n) {
            // Self-loop
            return target;
        } else {
            boolean isBackwardTransition = false;

            for (int[] ints : backwards) {
                if (Arrays.equals(ints, responses)) {
                    isBackwardTransition = true;
                    break;
                }
            }

            if (isBackwardTransition && offset == 0) {
                // come back to the root
                target.setValue(0, 0);

                for (int j = 1; j <= n; j++) {
                    target.setValue(j, -1);
                }

                return target;
            }
            return target.setValue(0, x + 1).setValue(x + 1, offset == 0 ? 0 : 1);
        }
    }

    @Override
    public boolean isLabelTrue(int i) throws PrismException {
        switch (i) {
            // "goal"
            case 0:
                boolean containsOne = false;

                for (int[] ints : scpec) {
                    if (Arrays.equals(ints, responses)) {
                        containsOne = true;
                        break;
                    }
                }

                // If we traversed the whole tree and went through the path of the confirmation, then there is a desired state
                return containsOne;
            // "end"
            case 1:
                return (x == n);
        }
        // Should never happen
        return false;
    }

    // Methods for RewardGenerator interface (reward info stored separately from ModelInfo/ModelGenerator)

    // There is a single reward structure, r, which just assigns reward 1 to every state.
    // We can use this to reason about the expected number of steps that occur through the random walk.

    @Override
    public List<String> getRewardStructNames() {
        return Arrays.asList("r");
    }

    @Override
    public double getStateReward(int r, State state) throws PrismException {
        // No action rewards
        int[] responsesForReward = new int[n];
        for (int i = 0; i < n; i++) {
            responsesForReward[i] = (Integer) state.varValues[i + 1];
        }

        // starting from the root assign to the each state doubled award
        if (containsElement(responsesForReward, 0) || !containsElement(responsesForReward, -1)) {
            return 0.0;
        } else {
            int onesNum = Collections.frequency(Arrays.stream(responsesForReward)
                    .boxed()
                    .collect(Collectors.toList()), 1);


            double denominator = 1;
            for (int i = 0; i < onesNum; i++) {
                denominator = denominator * probabilities.get(i);
            }
            return 1 / denominator;
        }
    }

    private boolean containsElement(int[] arr, int el) {
        for (int i :
                arr) {
            if (i == el) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getStateActionReward(int r, State state, Object action) throws PrismException {
        return 0.0;
    }
}
