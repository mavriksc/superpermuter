package org.mavriksc.superpermuter;

import java.util.Arrays;
import java.util.List;

public class CheckSuper {


    public static void main (String[] args){
        String thing = "ABCDEABCDAEBCDABECDABCEDABCADEBCADBECADBCEADBCAEDBCABDECABDCEABDCAEBDCABEDCABADCEBADCBEADCBAEDCBADECBADACBEDACBDEACBDAECBDACEBDACDBEACDBAECDBACEDBACDEBACD";
        System.out.println(isSuperPermutation(SuperPermuter.returnPermutations(thing.substring(0,5)),thing));

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

}
