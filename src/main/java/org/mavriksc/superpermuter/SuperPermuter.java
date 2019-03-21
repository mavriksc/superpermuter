package org.mavriksc.superpermuter;

import org.mavriksc.superpermuter.type.RowCol;

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
    private static Map<String, RowCol> lookup = new HashMap<>();
    private static int COLS = 0;
    private static String[][] permutations = permutationsViaRotation(source);


    public static void main(String[] args) {
        outputPerm2DArray(permutations);
        String  one = stringToAppendColAtRowWithOverlap(new RowCol(0,0),0);
        System.out.println(one);
        String next = one.substring(one.length()-N);
        next = next.substring(N-(N-2))+reverse(next.substring(0,2));
        one += stringToAppendColAtRowWithOverlap(lookup.get(next),N-2);
        System.out.println(one);
    }

    private static String reverse(String s){
        return new StringBuilder(s).reverse().toString();
    }

    private static void generateSuperPermutations(String[][] permutations) {


    }

    private static String stringToAppendColAtRowWithOverlap(RowCol rc, int overlap) {
        return (permutations[rc.getRow()][rc.getCol()] + permutations[(rc.getRow() + 1) % N][rc.getCol()].substring(1)).substring(overlap);
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
                lookup.put(perms[i][j], new RowCol(i, j));
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

    private static String rotateStringRight(String s) {
        if (s == null || s.length() == 1) return s;
        else {
            return s.substring(s.length() - 1) + s.substring(0, s.length() - 1);
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
        return isSuperPermutation(Arrays.asList(symbols.split("")), guess);
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

    private static void printLocationMap() {
        StringBuilder sb = new StringBuilder();
        lookup.forEach((k, v) -> sb.append(k).append("\t").append(v).append("\n"));
        System.out.println(sb.toString());
    }
}
