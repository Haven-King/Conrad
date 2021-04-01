package dev.inkwell.conrad.impl;

import dev.inkwell.conrad.api.value.util.Array;
import dev.inkwell.conrad.api.value.util.Table;

public class TestDataClass {
    public int test1 = 10;
    private int test2 = 30;
    private String test3 = "new horizons";
    public Table<Inner> testTable = new Table<>(Inner.class, Inner::new);

    public static class Inner {
        public int test1 = 1;
        public double test3 = 30;
        public Array<String> testArray = new Array<>(String.class, () -> "NEW", "ABC", "DEF", "GHI");
    }
}
