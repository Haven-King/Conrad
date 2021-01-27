package dev.monarkhes.conrad.test;

import net.fabricmc.loader.api.config.data.Constraint;

public abstract class Bounds<T extends Number> extends Constraint<T> {
    protected final T min;
    protected final T max;

    protected Bounds(String name, T min, T max) {
        super(name);
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "bounds[" + this.min + ", " + this.max + "]";
    }

    public T getMin() {
        return this.min;
    }

    public T getMax() {
        return this.max;
    }

    public static class Int extends Bounds<Integer> {
        public Int(Integer min, Integer max) {
            super("fabric:bounds/int", min, max);
        }

        @Override
        public boolean passes(Integer integer) {
            return integer >= this.min && integer <= this.max;
        }
    }
}
