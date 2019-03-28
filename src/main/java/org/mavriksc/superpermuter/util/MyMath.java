package org.mavriksc.superpermuter.util;

public class MyMath {
    public static int factorial(int n){
        int fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }
}
