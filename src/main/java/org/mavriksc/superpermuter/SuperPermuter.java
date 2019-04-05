package org.mavriksc.superpermuter;

import org.mavriksc.superpermuter.type.Move;
import org.mavriksc.superpermuter.type.RowCol;
import org.mavriksc.superpermuter.util.FileWriter;
import org.mavriksc.superpermuter.util.MyMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class SuperPermuter {

    private static List<String> source = asList("A", "B", "C", "D", "E");
    private static Map<String, Integer> indexMap = getIndexMap();
    private static Map<String, Integer> comboHash = new HashMap<>();
    private static Map<Integer, List<String>> permCache = new HashMap<>();
    private static int N = source.size();
    private static Map<String, RowCol> lookup = new HashMap<>();
    private static int COLS = 0;
    private static String[][] permutations = permutationsViaRotation(source);
    private static int maxOverlap = Math.max(N - 2, 1);
    private static int minSuperLen = maxLenGivenAlg();

    public static void main(String[] args) {
        outputPerm2DArray(permutations);
        System.out.println(maxPackedRemainingLength(Collections.emptyList()));
        System.out.println(minSuperLen);
        List<Integer> usedCols = new ArrayList<>();
        recursePathFindOptimized("", new Move(new RowCol(0, 0), 0), usedCols);
    }

    private static int maxLenGivenAlg() {
        int nMinusOneFact = MyMath.factorial(N - 1);
        return (2 * N - 1) + (2 * N - 2) * (nMinusOneFact - 1);
    }


    private static void recursePathFind(String soFar, Move m, List<Integer> usedCols) {
        usedCols.add(m.getRowCol().getCol());
        soFar += stringToAppendColAtRowWithOverlap(m.getRowCol(), m.getOverlap());
        if (usedCols.size() < permutations[0].length) {
            //still more work
            for (int i = maxOverlap; i > 0; i--) {
                String finalSoFar = soFar;
                getMoves(soFar, i)
                        .parallelStream()
                        .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                        .forEach(move -> recursePathFind(finalSoFar, move, new ArrayList<>(usedCols)));
            }
        } else {
            outputNewSuperOrSkip(soFar, usedCols);
        }
    }

    private static void recursePathFindOptimizedMaxMinMerge(String soFar, Move m, List<Integer> usedCols, int minMergeCount) {
        usedCols.add(m.getRowCol().getCol());
        soFar += stringToAppendColAtRowWithOverlap(m.getRowCol(), m.getOverlap());
        if (minMergeCount <= 3) {
            //possible to still beat current record.
            //else : do nothing quit wasting time end search on this branch.
            if (usedCols.size() < permutations[0].length) {
                //still more work
                for (int i = maxOverlap; i > 0; i--) {
                    if (i == 1) {
                        minMergeCount++;
                    }
                    String finalSoFar = soFar;
                    int finalMinMergeCount = minMergeCount;
                    getMoves(soFar, i)
                            .parallelStream()
                            .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                            .forEach(move -> recursePathFindOptimizedMaxMinMerge(finalSoFar, move, new ArrayList<>(usedCols), finalMinMergeCount));
                }
            } else {
                outputNewSuperOrSkip(soFar, usedCols);
            }
        }
    }

    private static void recursePathFindOptimized(String soFar, Move m, List<Integer> usedCols) {
        usedCols.add(m.getRowCol().getCol());
        soFar += stringToAppendColAtRowWithOverlap(m.getRowCol(), m.getOverlap());
        if (soFar.length() + maxPackedRemainingLength(usedCols) <= minSuperLen) {
            //possible to still beat current record.
            //else : do nothing quit wasting time end search on this branch.
            if (usedCols.size() < permutations[0].length) {
                //still more work
                for (int i = maxOverlap; i > 0; i--) {
                    String finalSoFar = soFar;
                    getMoves(soFar, i)
                            .parallelStream()
                            .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                            .forEach(move -> recursePathFindOptimized(finalSoFar, move, new ArrayList<>(usedCols)));
                }
            } else {
                outputNewSuperOrSkip(soFar, usedCols);
            }
        }
    }

    private static void outputNewSuperOrSkip(String soFar, List<Integer> usedCols) {
        if (soFar.length() <= minSuperLen) {
            if (soFar.length() < minSuperLen) {
                System.out.println("Found new BEST !!!\n\t" + soFar.length() + " - " + soFar);
                minSuperLen = soFar.length();
            } else {
                System.out.println("Found new TIE!!!\n\t" + soFar.length() + " - " + soFar);
            }
            StringBuilder sb = new StringBuilder();
            usedCols.forEach(i -> sb.append(i).append("-"));
            System.out.println(sb.toString());

            saveSuper(soFar, sb.toString());
        }
    }

    private static int maxPackedRemainingLength(List<Integer> usedCols) {
        int unusedCols = permutations[0].length - usedCols.size();
        int len = (N + 1) * (unusedCols - 1);
        if (usedCols.size() == 0)
            len = len + (2 * N - 1);
        return len;
    }

    private static void saveSuper(String superPermutationMF, String colPattern) {

        String path = "./" + N;
        String fileName = "" + superPermutationMF.length() + "-" + UUID.randomUUID() + ".txt";
        FileWriter.write(path, fileName, Arrays.asList(superPermutationMF, colPattern));

    }

    private static List<Move> getMoves(String current, int overlap) {
        String end = current.substring(current.length() - N);
        String pre = end.substring(N - overlap);
        String tail = end.substring(0, N - overlap);
        return permutationsViaRotation(tail).stream().filter(s -> !s.equals(tail)).map(ts -> new Move(lookup.get(pre + ts), overlap))
                .collect(Collectors.toList());
    }

    private static String stringToAppendColAtRowWithOverlap(RowCol rc, int overlap) {
        return (permutations[rc.getRow()][rc.getCol()] + permutations[(rc.getRow() + 1) % N][rc.getCol()].substring(1)).substring(overlap);
    }

    static List<String> permutationsViaRotation(String source) {
        return permCache.computeIfAbsent(stringHashVal(source), s -> {
            List<String> perms = new ArrayList<>();
            if (source.length() == 1) {
                perms.add(source);
                return perms;
            } else {
                String prefix = source.substring(0, 1);
                List<String> tails = permutationsViaRotation(source.substring(1, source.length()));
                tails.forEach(t -> {
                    String toRot = prefix + t;
                    perms.add(toRot);
                    for (int i = 0; i < source.length() - 1; i++) {
                        toRot = rotateStringRight(toRot);
                        perms.add(toRot);
                    }
                });
                return perms;
            }
        });
    }

    private static String charListToString(List<String> source) {
        StringBuilder sb = new StringBuilder();
        source.forEach(sb::append);
        return sb.toString();
    }

    private static String[][] permutationsViaRotation(List<String> source) {
        List<String> permuteThis = new ArrayList<>(source);
        String one = permuteThis.get(0);
        permuteThis.remove(one);
        List<String> row1 = permutationsViaRotation(charListToString(permuteThis)).stream().map(ts -> one + ts).collect(Collectors.toList());

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

    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    private static int stringHashVal(String s) {
        return comboHash.computeIfAbsent(s, string -> {
            int hash = 0;
            for (String s1 : s.split("")) {
                hash += (int) Math.pow(2, indexMap.get(s1));
            }
            return hash;
        });
    }

    private static Map<String, Integer> getIndexMap() {
        Map<String, Integer> map = new HashMap<>();
        source.forEach(s -> map.put(s, source.indexOf(s)));
        return map;
    }
}
