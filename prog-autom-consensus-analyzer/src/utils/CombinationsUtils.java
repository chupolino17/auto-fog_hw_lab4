package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.pow;

public class CombinationsUtils {
    public List<Set<int[]>> getBackwardTransitionsCombinations(int n) {
        List<Set<int[]>> output = new ArrayList<>();
        List<int[]> permutations = new ArrayList<>();
        int index;

        for (int i = 1; i <= n; i++) {

            for (int j = 0; j < pow(2, n - i); j++) {
                int[] tempList = new int[n];
                index = n - 1;

                for (int k = 0; k < i; k++) {
                    tempList[index] = -1;
                    --index;
                }

                if (i != n) {
                    String binary = Integer.toBinaryString(j);

                    for (int k = 0; k < n - i - binary.length(); k++) {
                        tempList[index] = 0;
                        --index;
                    }

                    for (int k = 0; k < binary.length(); k++) {
                        tempList[index] = (Character.getNumericValue(binary.charAt(k)));
                        --index;
                    }
                }

                permutations.add(tempList);
            }
        }

        output.add(new HashSet<>());

        for (int i = 1; i <= permutations.size(); i++) {
            Set<int[]> temp = new HashSet<>();

            int counter = 0;
            List<List<Integer>> combinations = Combinations.makeCombination(permutations.size(), i);

            for (List<Integer> lst : combinations) {
                for (Integer num : lst) {
                    temp.add(permutations.get(num - 1));
                    ++counter;
                    if (counter % i == 0) {
                        output.add(temp);
                        temp = new HashSet<>();
                        counter = 0;
                    }
                }
            }
        }

        return output;
    }

    static class Combinations {
        static private void makeCombinationUtil(int n, int left, int k, List<Integer> tmp, List<List<Integer>> ans) {
            if (k == 0) {
                ans.add(new ArrayList<>(tmp));
                return;
            }

            for (int i = left; i <= n; ++i) {
                tmp.add(i);
                makeCombinationUtil(n, i + 1, k - 1, tmp, ans);

                tmp.remove(tmp.size() - 1);
            }
        }

        static List<List<Integer>> makeCombination(int n, int k) {
            List<List<Integer>> ans = new ArrayList<>();
            List<Integer> tmp = new ArrayList<>();
            makeCombinationUtil(n, 1, k, tmp, ans);
            return ans;
        }
    }
}
