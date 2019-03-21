package org.mavriksc.superpermuter;

import org.mavriksc.superpermuter.type.XY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class SuperPermuter {

    private static List<String> source = asList("A", "B", "C", "D", "E");
    private static int N = source.size();
    static private Map<String, XY> lookup = new HashMap<>();



    public static void main(String[] args){
        String[][] permutations = permutationsViaRotation(source);

        outputPerm2DArray(permutations);

    }

    private static List<String> returnPermutations(List<String> source, int choose, int len) {
        List<String> result = new ArrayList<>();

        if (len + 1 == choose) {
            return source;
        } else {
            for (String s : source) {
                List<String> tail = new ArrayList<>(source);
                tail.remove(s);
                result.addAll(returnPermutations(tail, choose, len + 1).stream().map(ts -> s + ts)
                        .collect(Collectors.toList()));
            }
            return result;
        }
    }

    private static String[][] permutationsViaRotation(List<String> source) {
        List<String> permuteThis = new ArrayList<>(source);
        String one = permuteThis.get(0);
        permuteThis.remove(one);
        List<String> row1 = returnPermutations(permuteThis, N - 1, 0).stream().map(ts -> one + ts)
                .collect(Collectors.toList());

        String[][] perms = new String[N][row1.size()];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < row1.size(); j++) {
                perms[i][j] = i == 0 ? row1.get(j) : rotateStringRight(perms[i - 1][j]);
                lookup.put(perms[i][j],new XY(i,j));
            }
        }
        return perms;
    }

    private static String rotateStringLeft(String s) {
        if (s == null || s.length() == 1) return s;
        else {
            return s.substring(1) + s.substring(0, 1);
        }
    }

    private static String rotateStringRight(String s){
        if (s == null || s.length() == 1) return s;
        else {
            return s.substring(s.length()-1) + s.substring(0, s.length()-1);
        }
    }

    private static boolean isSuperPermutation(List<String> perms, String guess) {
        for (String s : perms) {
            if (!guess.contains(s)) {
                System.out.println(s);
                return false;
            }
        }
        return true;
    }

    private static boolean isSuperPermutation(String symbols, String guess) {
        return isSuperPermutation(Arrays.asList(symbols.split("")),guess);
    }

    private static void outputPerm2DArray(String[][] permutations) {
        StringBuilder sb = new StringBuilder();
        for (String[] permutation : permutations) {
            for (String s : permutation) {
                sb.append(s).append(" ");
            }
            sb.append("\n");
        }
        System.out.println(sb.toString());
    }
    
}
