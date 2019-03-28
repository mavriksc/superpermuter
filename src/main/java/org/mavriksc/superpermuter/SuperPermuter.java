package org.mavriksc.superpermuter;

import org.mavriksc.superpermuter.type.Move;
import org.mavriksc.superpermuter.type.RowCol;
import org.mavriksc.superpermuter.util.FileWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class SuperPermuter {

    private static List<String> source = asList("A", "B", "C", "D", "E"/*, "F"*/);
    private static int N = source.size();
    private static Map<String, RowCol> lookup = new HashMap<>();
    private static Map<String, Set<Integer>> comboLookup = new HashMap<>();
    private static Map<Integer, List<String>> permuterLookup = new HashMap<>();
    private static int COLS = 0;
    private static String[][] permutations = permutationsViaRotation(source);
    private static int maxOverlap = Math.max(N - 2, 1);
    private static int minSuperLen = Integer.MAX_VALUE;


    public static void main(String[] args) {
        outputPerm2DArray(permutations);
        Set<Integer> usedCols = new HashSet<>();
        recursePathFindOptimizedMaxMinMerge("", new Move(new RowCol(0, 0), 0, 0), usedCols, 0);
    }


    private static void recursePathFind(String soFar, Move m, Set<Integer> usedCols) {
        usedCols.add(m.getRowCol().getCol());
        soFar += stringToAppendColAtRowWithOverlap(m.getRowCol(), m.getOverlap());
        if (usedCols.size() < permutations[0].length) {
            //still more work
            for (int i = maxOverlap; i > 0; i--) {
                String finalSoFar = soFar;
                getMoves(soFar, i, usedCols)
                        .parallelStream()
                        .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                        .forEach(move -> recursePathFind(finalSoFar, move, new HashSet<>(usedCols)));
            }
        } else {
            outputNewSuperOrSkip(soFar);
        }
    }

    private static void recursePathFindOptimizedMaxMinMerge(String soFar, Move m, Set<Integer> usedCols, int minMergeCount) {
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
                    getMoves(soFar, i, usedCols)
                            .parallelStream()
                            .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                            .forEach(move -> recursePathFindOptimizedMaxMinMerge(finalSoFar, move, new HashSet<>(usedCols), finalMinMergeCount));
                }
            } else {
                outputNewSuperOrSkip(soFar);
            }
        }
    }

    private static void recursePathFindOptimized(String soFar, Move m, Set<Integer> usedCols) {
        usedCols.add(m.getRowCol().getCol());
        soFar += stringToAppendColAtRowWithOverlap(m.getRowCol(), m.getOverlap());
        if (soFar.length() + maxPackedRemainingLength(usedCols) <= minSuperLen) {
            //possible to still beat current record.
            //else : do nothing quit wasting time end search on this branch.
            if (usedCols.size() < permutations[0].length) {
                //still more work
                for (int i = maxOverlap; i > 0; i--) {
                    String finalSoFar = soFar;
                    getMoves(soFar, i, usedCols)
                            .parallelStream()
                            .filter(m1 -> !usedCols.contains(m1.getRowCol().getCol()))
                            .forEach(move -> recursePathFindOptimized(finalSoFar, move, new HashSet<>(usedCols)));
                }
            } else {
                outputNewSuperOrSkip(soFar);
            }
        }
    }

    private static void outputNewSuperOrSkip(String soFar) {
        if (soFar.length() < minSuperLen) {
            System.out.println("Found new BEST !!!\n\t" + soFar.length() + " - " + soFar);
            minSuperLen = soFar.length();
            saveSuper(soFar);
        } else if (soFar.length() <= minSuperLen) {
            System.out.println("Found new TIE!!!\n\t" + soFar.length() + " - " + soFar);
            saveSuper(soFar);
        }
    }

    private static int maxPackedRemainingLength(Set<Integer> usedCols) {
        int unusedCols = permutations[0].length - usedCols.size();
        int maxPackColLen = N + N - 1;
        // try to set bounds but this is fuzzy
        int overlapGuestimate = maxOverlap < 2 ? maxOverlap : 2;
        int overlapOffset = 15;
        return (maxPackColLen + (maxPackColLen - overlapGuestimate) * (unusedCols - 1)) - overlapOffset;
    }

    private static void saveSuper(String superPermutationMF) {

        String path = "./" + N;
        String fileName = "" + superPermutationMF.length() + "-" + UUID.randomUUID() + ".txt";
        FileWriter.write(path, fileName, Collections.singletonList(superPermutationMF));

    }

    private static List<Move> getMoves(String current, int overlap, Set<Integer> usedCols) {
        String end = current.substring(current.length() - N);
        String pre = end.substring(N - overlap);
        String tail = end.substring(0, N - overlap);
        return returnPermutations(tail)
                .stream()
                .filter(s -> !s.equals(tail))
                .map(ts -> new Move(lookup.get(pre + ts), overlap, calcComboFactor(pre + ts, overlap, usedCols)))
                //.sorted(Comparator.comparingInt(Move::getFreshComboFactor).reversed())
                .collect(Collectors.toList());
    }

    private static int calcComboFactor(String s, int overlap, Set<Integer> usedCols) {
        if (overlap == maxOverlap) {
            return 999;
        } else if (overlap == 1) {
            return 1;
        } else {
            String key = s.substring(0, overlap);
            Set<Integer> homes = new HashSet<>(comboLookup.get(key));
            homes.removeAll(usedCols);
            return homes.size();
        }
    }


    private static String stringToAppendColAtRowWithOverlap(RowCol rc, int overlap) {
        return (permutations[rc.getRow()][rc.getCol()] + permutations[(rc.getRow() + 1) % N][rc.getCol()].substring(1)).substring(overlap);
    }

    static List<String> returnPermutations(String s) {
        return returnPermutations(Arrays.asList(s.split("")), s.length(), 0);
    }

    private static List<String> returnPermutations(List<String> source, int choose, int len) {
        int key = comboValue(source);
        if (!permuterLookup.containsKey(key)) {
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
                permuterLookup.put(key, result);
            }
        }
        return permuterLookup.get(key);
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
                putStuffInComboMap(perms[i][j], j);
            }
        }
        return perms;
    }

    private static void putStuffInComboMap(String s, Integer col) {
        for (int i = 2; i <= N - 3; i++) {
            String key = s.substring(0, i);
            if (!comboLookup.containsKey(key)) {
                comboLookup.put(key, new HashSet<>());
            }
            comboLookup.get(key).add(col);
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

    private static int comboValue(List<String> s) {
        return s.stream().mapToInt(SuperPermuter::twoPowerLetterHash).sum();
    }

    private static int twoPowerLetterHash(String s) {
        return (int) Math.pow(2, source.indexOf(s));
    }
}
