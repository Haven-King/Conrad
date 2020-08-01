package dev.hephaestus.conrad.impl.client;

public interface WrappedNumberType<T extends Number & Comparable<T>> {
	WrappedNumberType<Integer> INTEGER = new WrappedIntegerType();
	WrappedNumberType<Long> LONG = new WrappedLongType();
	WrappedNumberType<Float> FLOAT = new WrappedFloatType();
	WrappedNumberType<Double> DOUBLE = new WrappedDoubleType();
	WrappedNumberType<Integer> HEX_INTEGER = new WrappedHexIntType();
	WrappedNumberType<Short> SHORT = new WrappedShortType();
	WrappedNumberType<Byte> BYTE = new WrappedByteType();

	default boolean canParse(String string) {
		try {
			this.parse(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	default String toString(T value) {
		return String.valueOf(value);
	}

	T parse(String string);
	T minValue();
	T maxValue();

	class WrappedIntegerType implements WrappedNumberType<Integer> {
		@Override
		public Integer parse(String string) {
			return Integer.parseInt(string);
		}

		@Override
		public Integer minValue() {
			return Integer.MIN_VALUE;
		}

		@Override
		public Integer maxValue() {
			return Integer.MAX_VALUE;
		}
	}

	class WrappedLongType implements WrappedNumberType<Long> {
		@Override
		public Long parse(String string) {
			return Long.parseLong(string);
		}

		@Override
		public Long minValue() {
			return Long.MIN_VALUE;
		}

		@Override
		public Long maxValue() {
			return Long.MAX_VALUE;
		}
	}

	class WrappedFloatType implements WrappedNumberType<Float> {
		@Override
		public Float parse(String string) {
			return Float.parseFloat(string);
		}

		@Override
		public Float minValue() {
			return Float.MIN_VALUE;
		}

		@Override
		public Float maxValue() {
			return Float.MAX_VALUE;
		}
	}

	class WrappedDoubleType implements WrappedNumberType<Double> {
		@Override
		public Double parse(String string) {
			return Double.parseDouble(string);
		}

		@Override
		public Double minValue() {
			return Double.MIN_VALUE;
		}

		@Override
		public Double maxValue() {
			return Double.MAX_VALUE;
		}
	}

	class WrappedShortType implements WrappedNumberType<Short> {
		@Override
		public Short parse(String string) {
			return Short.parseShort(string);
		}

		@Override
		public Short minValue() {
			return Short.MIN_VALUE;
		}

		@Override
		public Short maxValue() {
			return Short.MAX_VALUE;
		}
	}

	class WrappedByteType implements WrappedNumberType<Byte> {
		@Override
		public Byte parse(String string) {
			return Byte.parseByte(string);
		}

		@Override
		public Byte minValue() {
			return Byte.MIN_VALUE;
		}

		@Override
		public Byte maxValue() {
			return Byte.MAX_VALUE;
		}
	}

	class WrappedHexIntType implements WrappedNumberType<Integer> {
		@Override
		public String toString(Integer value) {
			return "0x" + Integer.toHexString(value);
		}

		@Override
		public Integer parse(String string) {
			return Integer.parseInt(string.substring(2), 16);
		}

		@Override
		public Integer minValue() {
			return Integer.MIN_VALUE;
		}

		@Override
		public Integer maxValue() {
			return Integer.MAX_VALUE;
		}
	}
}
