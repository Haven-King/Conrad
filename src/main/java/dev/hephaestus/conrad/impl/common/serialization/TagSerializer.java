package dev.hephaestus.conrad.impl.common.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.api.serialization.ConfigSerializer;
import dev.hephaestus.conrad.api.serialization.ValueSerializer;
import net.minecraft.nbt.*;
import net.minecraft.util.JsonHelper;

import java.io.*;

public class TagSerializer extends ConfigSerializer<Tag, CompoundTag> {
	public static final TagSerializer INSTANCE = new TagSerializer();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public TagSerializer() {
		this.addSerializer(Boolean.class, ByteTag.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, IntTag.class, IntSerializer.INSTANCE);
		this.addSerializer(String.class, StringTag.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, FloatTag.class, FloatSerializer.INSTANCE);
	}

	@Override
	public CompoundTag start(Config config) {
		CompoundTag tag = new CompoundTag();
		tag.putString("version", config.version().toString());
		return tag;
	}

	@Override
	protected <R extends Tag> void add(CompoundTag object, String key, R representation) {
		object.put(key, representation);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V get(CompoundTag object, String key) {
		return (V) object.get(key);
	}

	@Override
	public CompoundTag read(InputStream in) {
		return (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, JsonHelper.deserialize(new InputStreamReader(in)));
	}

	@Override
	protected void write(CompoundTag object, OutputStream out) throws IOException {
		Writer writer = new OutputStreamWriter(out);
		GSON.toJson(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, object), writer);
		writer.flush();
		writer.close();
	}

	@Override
	public String fileExtension() {
		return "json";
	}

	private interface TagValueSerializer<R extends Tag, V> extends ValueSerializer<Tag, R, V> {
	}

	private static class BooleanSerializer implements TagValueSerializer<ByteTag, Boolean> {
		static BooleanSerializer INSTANCE = new BooleanSerializer();

		@Override
		public ByteTag serialize(Boolean value) {
			return ByteTag.of(value);
		}

		@Override
		public Boolean deserialize(Tag representation) {
			return ((ByteTag) representation).getByte() == 1;
		}
	}

	private static class IntSerializer implements TagValueSerializer<IntTag, Integer> {
		static IntSerializer INSTANCE = new IntSerializer();

		@Override
		public IntTag serialize(Integer value) {
			return IntTag.of(value);
		}

		@Override
		public Integer deserialize(Tag representation) {
			return ((IntTag) representation).getInt();
		}
	}

	private static class StringSerializer implements TagValueSerializer<StringTag, String> {
		static StringSerializer INSTANCE = new StringSerializer();

		@Override
		public StringTag serialize(String value) {
			return StringTag.of(value);
		}

		@Override
		public String deserialize(Tag representation) {
			return representation.asString();
		}
	}

	private static class FloatSerializer implements TagValueSerializer<FloatTag, Float> {
		public static final FloatSerializer INSTANCE = new FloatSerializer();

		@Override
		public FloatTag serialize(Float value) {
			return FloatTag.of(value);
		}

		@Override
		public Float deserialize(Tag representation) {
			return ((FloatTag) representation).getFloat();
		}
	}
}
