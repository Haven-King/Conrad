package dev.hephaestus.conrad.api.serialization;

import org.jetbrains.annotations.Nullable;
import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;
import dev.hephaestus.conrad.impl.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class ConfigSerializer<E, O extends E> {
	private final HashMap<Class<?>, ValueSerializer<E, ?, ?>> serializableTypes = new HashMap<>();
	private final HashMap<Class<? extends E>, Class<?>> valueSerializers = new HashMap<>();

	protected final void addSerializer(Class<?> valueClass, Class<? extends E> representationClass,  ValueSerializer<E, ?, ?> valueSerializer) {
		this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

		valueClass = ReflectionUtil.getClass(valueClass);

		for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
			this.serializableTypes.put(clazz, valueSerializer);
		}

		this.valueSerializers.put(representationClass, valueClass);
	}

	protected final boolean canSerialize(Class<?> valueClass) {
		return this.serializableTypes.containsKey(valueClass);
	}

	protected final ValueSerializer<E, ?, ?> getSerializer(Class<?> clazz) {
		return serializableTypes.containsKey(clazz) ? serializableTypes.get(clazz) : serializableTypes.get(valueSerializers.get(clazz));
	}

	public final O serialize(Config config) {
		O object = this.start(config);

		for (Method method : this.getMethods(config.getClass())) {
			E value = null;
			try {
				Class<?> type = method.getReturnType();
				if (Config.class.isAssignableFrom(type)) {
					value = this.serialize((Config) method.invoke(config));
				} else if (this.canSerialize(type)) {
					value = this.getSerializer(type).serializeValue(method.invoke(config));
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}

			StringBuilder builder = new StringBuilder();

			ConradUtil.getTooltips(method, text -> {
				if (builder.length() > 0) {
					builder.append('\n');
				}

				String string = text instanceof TranslatableText ? Translator.translate((TranslatableText) text) : text.asString();

				builder.append(string);
			});

			this.add(object, KeyRing.methodName(method), value, builder.toString());
		}

		return object;
	}

	@SuppressWarnings("unchecked")
	public final void deserialize(Config config, Object object) {
		Config.SaveType saveType = ReflectionUtil.getRoot(config.getClass()).getAnnotation(Config.Options.class).type();

		if (saveType == Config.SaveType.USER && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			return;
		}

		for (Method method : this.getMethods(config.getClass())) {
			Class<?> type = method.getReturnType();
			try {
				if (Config.class.isAssignableFrom(type)) {
					Object value = this.get((O) object, KeyRing.methodName(method));

					if (value != null) {
						this.deserialize((Config) KeyRing.get(KeyRing.get(ReflectionUtil.getDeclared(method))).invoke(config), value);
					}
				} else if (this.canSerialize(type)) {
					Object result = this.getSerializer(type).deserialize(this.get((O) object, KeyRing.methodName(method)));

					if (result == null) {
						result = ReflectionUtil.invokeDefault(
								KeyRing.get(KeyRing.get(method))
						);
					}

					if (result != null && !Config.class.isAssignableFrom(result.getClass())) {
						ValueContainer.getInstance().put(KeyRing.get(method), result, false);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public final void writeValue(Object object, OutputStream out) throws IOException {
		this.write((O) object, out);
	}

	public abstract O start(Config config);
	protected abstract <R extends E> void add(O object, String key, R representation, @Nullable String comment);
	public abstract <V> V get(O object, String key);

	public abstract O read(InputStream in) throws IOException;
	protected abstract void write(O object, OutputStream out) throws IOException;

	public abstract String fileExtension();

	private Collection<Method> getMethods(Class<?> configClass) {
		TreeSet<Method> primitiveMethods = new TreeSet<>(Comparator.comparing(Method::getName));
		TreeSet<Method> compoundMethods = new TreeSet<>(Comparator.comparing(Method::getName));

		for (Method method : configClass.getDeclaredMethods()) {
			if (ConradUtil.methodType(method) == Config.Value.MethodType.GETTER) {

				Class<?> returnType = method.getReturnType();

				if (this.canSerialize(returnType)) {
					ValueSerializer<?, ?, ?> valueSerializer = this.getSerializer(returnType);
					if (valueSerializer.isCompound()) {
						compoundMethods.add(method);
					} else {
						primitiveMethods.add(method);
					}
				} else if (Config.class.isAssignableFrom(returnType)) {
					compoundMethods.add(method);
				}
			}
		}

		ArrayList<Method> methods = new ArrayList<>();

		methods.addAll(primitiveMethods);
		methods.addAll(compoundMethods);

		return methods;
	}
}
