package dev.inkwell.conrad.impl;

public class TestDataClass {
    public int test1 = 10;
    private int test2 = 30;
    private String test3 = "horizons";
    private Inner test4 = new Inner();

    public static class Inner {
        public int test1 = 1;
        public double test3 = 30;
    }
}
