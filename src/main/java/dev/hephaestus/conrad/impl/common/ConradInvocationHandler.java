package dev.hephaestus.conrad.impl.common;

import dev.hephaestus.conrad.api.Config;
import dev.hephaestus.conrad.impl.common.config.*;
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
		Config.Value.MethodType methodType = ConradUtil.methodType(method);
		if (methodType == Config.Value.MethodType.UTIL) return ReflectionUtil.invokeDefault(proxy, method, args);

		ValueKey key = KeyRing.get(method);
		ConfigDefinition configDefinition = KeyRing.get(key.getConfigKey());
		Config.SaveType saveType = configDefinition.getSaveType();

		ValueContainer valueContainer = saveType == Config.SaveType.LEVEL
				? ConradUtil.either(this.valueContainer, ValueContainer.getInstance())
				: ValueContainer.ROOT;

		if (methodType == Config.Value.MethodType.GETTER) {
			if (!valueContainer.containsDefault(key)) {
				if (method.isDefault()) {
					valueContainer.put(key, ReflectionUtil.invokeDefault(proxy, method, args), false);
				} else if (Config.class.isAssignableFrom(method.getReturnType())) {
					valueContainer.put(key, Proxy.newProxyInstance(method.getReturnType().getClassLoader(), new Class[] {method.getReturnType()}, this), false);
				} else {
					throw new ConradException("Method '" + method.getName() + "' must be default or return an object that extends Config!");
				}
			}

			return valueContainer.get(key);
		}

		throw new ConradException("Unexpected method type: " + methodType);
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
