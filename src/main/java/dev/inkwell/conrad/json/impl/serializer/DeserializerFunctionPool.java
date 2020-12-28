/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Falkreon (Isaac Ellingson)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.inkwell.conrad.json.impl.serializer;

import dev.inkwell.conrad.json.JsonGrammar;
import dev.inkwell.conrad.json.JsonObject;
import dev.inkwell.conrad.json.api.DeserializationException;

import java.util.HashMap;

/**
 * Stores deserializer functions that produce objects of type &lt;B&gt;
 */
public class DeserializerFunctionPool<B> {
	private Class<B> targetClass;
	private HashMap<Class<?>, InternalDeserializerFunction<B>> values = new HashMap<>();
	
	public DeserializerFunctionPool(Class<B> targetClass) {
		this.targetClass = targetClass;
	}
	
	public void registerUnsafe(Class<?> sourceClass, InternalDeserializerFunction<B> function) {
		values.put(sourceClass, function);
	}
	
	public InternalDeserializerFunction<B> getFunction(Class<?> sourceClass) {
		return (InternalDeserializerFunction<B>)values.get(sourceClass);
	}
	
	public B apply(dev.inkwell.conrad.json.JsonElement elem) throws DeserializationException, FunctionMatchFailedException {
		InternalDeserializerFunction<B> selected = null;
		
		//This whole block is pretty ugly but there's a very particular selection order
		if (elem instanceof dev.inkwell.conrad.json.JsonPrimitive) {
			//1. Unwrapped primitive class
			Object obj = ((dev.inkwell.conrad.json.JsonPrimitive) elem).getValue();
			selected = values.get(obj.getClass());
			if (selected!=null) return selected.deserialize(obj);
			
			//2. JsonPrimitive
			selected = values.get(dev.inkwell.conrad.json.JsonPrimitive.class);
			if (selected!=null) return selected.deserialize((dev.inkwell.conrad.json.JsonPrimitive)elem);
		} else if (elem instanceof JsonObject) {
			//2. JsonObject
			selected = values.get(JsonObject.class);
			if (selected!=null) return selected.deserialize((JsonObject)elem);
		} else if (elem instanceof dev.inkwell.conrad.json.JsonArray) {
			//2. JsonArray
			selected = values.get(dev.inkwell.conrad.json.JsonArray.class);
			if (selected!=null) return selected.deserialize((dev.inkwell.conrad.json.JsonArray)elem);
		}
		
		//3. JsonElement
		selected = values.get(dev.inkwell.conrad.json.JsonElement.class);
		if (selected!=null) return selected.deserialize((dev.inkwell.conrad.json.JsonElement)elem);
		
		//We can't just return null, *because null might be the intended output of one of the functions above!*
		throw new FunctionMatchFailedException("Couldn't find a deserializer in class '"+targetClass.getCanonicalName()+"' to unpack element '"+elem.toJson(JsonGrammar.JSON5)+"'.");
	}
	
	public static class FunctionMatchFailedException extends Exception {
		private static final long serialVersionUID = -7909332778483440658L;
		public FunctionMatchFailedException(String message) { super(message); }
	}
}
