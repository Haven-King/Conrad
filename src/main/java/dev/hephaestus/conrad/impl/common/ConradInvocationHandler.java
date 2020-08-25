package dev.hephaestus.conrad.impl.common;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.config.ValueContainer;
import dev.hephaestus.conrad.impl.common.config.ValueContainerProvider;
import dev.hephaestus.conrad.impl.common.keys.KeyRing;
import dev.hephaestus.conrad.impl.common.keys.ValueKey;
import dev.hephaestus.conrad.impl.common.util.ConradException;
import dev.hephaestus.conrad.impl.common.util.ConradUtil;
import dev.hephaestus.conrad.impl.common.util.ReflectionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;

public class ConradInvocationHandler implements InvocationHandler {
	public static final InvocationHandler INSTANCE = new ConradInvocationHandler();

	private final ValueContainer valueContainer;

	public ConradInvocationHandler(ValueContainer valueContainer) {
		this.valueContainer = valueContainer;
	}

	public ConradInvocationHandler() {
		this(null);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Config.Entry.MethodType methodType = ConradUtil.methodType(method);
		if (methodType == Config.Entry.MethodType.UTIL) return ReflectionUtil.invokeDefault(proxy, method, args);

		ValueKey key = KeyRing.get(method);
		Config.SaveType.Type saveType = KeyRing.get(key.getConfig().root()).getAnnotation(Config.SaveType.class).value();
		ValueContainer valueContainer = ConradUtil.either(this.valueContainer, ValueContainerProvider.getInstance(saveType).getValueContainer());

		if (methodType == Config.Entry.MethodType.SETTER) {
			throw new ConradException(method.getName());
		} else {
			if (!valueContainer.containsDefault(key)) {
				if (method.isDefault()) {
					valueContainer.put(key, ReflectionUtil.invokeDefault(proxy, method, args));
				} else if (Config.class.isAssignableFrom(method.getReturnType())) {
					valueContainer.put(key, Proxy.newProxyInstance(method.getReturnType().getClassLoader(), new Class[] {method.getReturnType()}, this));
				} else {
					throw new ConradException("Method '" + method.getName() + "' must be default or return an object that extends Config!");
				}
			}

			return valueContainer.get(key);
		}
	}

	private static Class<?>[] of(Collection<Class<? extends Config>> collection) {
		Class<?>[] array = new Class[collection.size()];
		int i = 0;
		for (Class<? extends Config> item : collection) {
			array[i++] = item;
		}

		return array;
	}
}
