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

package dev.monarkhes.conrad.json;

import dev.monarkhes.conrad.json.api.SyntaxError;
import dev.monarkhes.conrad.json.impl.AnnotatedElement;
import dev.monarkhes.conrad.json.impl.ElementParserContext;
import dev.monarkhes.conrad.json.impl.ObjectParserContext;
import dev.monarkhes.conrad.json.impl.ParserContext;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;


public class Jankson {
	private Deque<ParserFrame<?>> contextStack = new ArrayDeque<>();
	private JsonObject root;
	private int line = 0;
	private int column = 0;
	private int withheldCodePoint = -1;
	@SuppressWarnings("deprecation")
	private boolean allowBareRootObject = false;
	
	private int retries = 0;
	private SyntaxError delayedError = null;

	@NotNull
	public JsonObject load(String s) throws SyntaxError {
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8")));
		try {
			return load(in);
		} catch (IOException ex) {
			throw new RuntimeException(ex); //ByteArrayInputStream never throws
		}
	}
	
	@NotNull
	public JsonObject load(File f) throws IOException, SyntaxError {
		try(InputStream in = new FileInputStream(f)) {
			return load(in);
		}
	}
	
	private static boolean isLowSurrogate(int i) {
		return (i & 0b1100_0000) == 0b1000_0000;
	}
	
	private static final int BAD_CHARACTER = 0xFFFD;
	public int getCodePoint(InputStream in) throws IOException {
		int i = in.read();
		if (i==-1) return -1;
		if ((i & 0b10000000)==0) return i; // \u0000..\u00FF is easy
		
		if ((i & 0b1111_1000) == 0b1111_0000) { //Character is 4 UTF-8 code points
			int codePoint = i & 0b111;
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			return codePoint;
		} else if ((i & 0b1111_0000) == 0b1110_0000) { //Character is 4 UTF-8 code points
			int codePoint = i & 0b1111;
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			return codePoint;
		} else if ((i & 0b1110_0000) == 0b1100_0000) { //Character is 4 UTF-8 code points
			int codePoint = i & 0b1111;
			
			i = in.read();
			if (i==-1) return -1;
			if (!isLowSurrogate(i)) return BAD_CHARACTER;
			codePoint <<= 6;
			codePoint |= (i & 0b0011_1111);
			
			return codePoint;
		}
		
		//we know it's 0b10xx_xxxx down here, so it's an orphaned low surrogate.
		return BAD_CHARACTER;
	}
	
	@NotNull
	public JsonObject load(InputStream in) throws IOException, SyntaxError {
		withheldCodePoint = -1;
		root = null;
		
		push(new ObjectParserContext(allowBareRootObject), (it)->{
			root = it;
		});
		
		//int codePoint = 0;
		while (root==null) {
			if (delayedError!=null) {
				throw delayedError;
			}
			
			if (withheldCodePoint!=-1) {
				retries++;
				if (retries>25) throw new IOException("Parser got stuck near line "+line+" column "+column);
				processCodePoint(withheldCodePoint);
			} else {
				int inByte = getCodePoint(in);
				if (inByte==-1) {
					//Walk up the stack sending EOF to things until either an error occurs or the stack completes
					while(!contextStack.isEmpty()) {
						ParserFrame<?> frame = contextStack.pop();
						try {
							frame.context.eof();
							if (frame.context.isComplete()) {
								frame.supply();
							}
						} catch (SyntaxError error) {
							error.setStartParsing(frame.startLine, frame.startCol);
							error.setEndParsing(line, column);
							throw error;
						}
					}
					if (root==null) {
						root = new JsonObject();
					}
					return root;
				}
				processCodePoint(inByte);
			}
		}
		
		return root;
	}
	
	/** Experimental: Parses the supplied String as a JsonElement, which may or may not be an object at the root level */
	@NotNull
	public JsonElement loadElement(String s) throws SyntaxError {
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(Charset.forName("UTF-8")));
		try {
			return loadElement(in);
		} catch (IOException ex) {
			throw new RuntimeException(ex); //ByteArrayInputStream never throws
		}
	}
	
	/** Experimental: Parses the supplied File as a JsonElement, which may or may not be an object at the root level */
	@NotNull
	public JsonElement loadElement(File f) throws IOException, SyntaxError {
		try(InputStream in = new FileInputStream(f)) {
			return loadElement(in);
		}
	}
	
	private AnnotatedElement rootElement;
	/** Experimental: Parses the supplied InputStream as a JsonElement, which may or may not be an object at the root level */
	@NotNull
	public JsonElement loadElement(InputStream in) throws IOException, SyntaxError {
		withheldCodePoint = -1;
		rootElement = null;
		
		push(new ElementParserContext(), (it)->{
			rootElement = it;
		});
		
		//int codePoint = 0;
		while (rootElement==null) {
			if (delayedError!=null) {
				throw delayedError;
			}
			
			if (withheldCodePoint!=-1) {
				retries++;
				if (retries>25) throw new IOException("Parser got stuck near line "+line+" column "+column);
				processCodePoint(withheldCodePoint);
			} else {
				int inByte = getCodePoint(in);
				if (inByte==-1) {
					//Walk up the stack sending EOF to things until either an error occurs or the stack completes
					while(!contextStack.isEmpty()) {
						ParserFrame<?> frame = contextStack.pop();
						try {
							frame.context.eof();
							if (frame.context.isComplete()) {
								frame.supply();
							}
						} catch (SyntaxError error) {
							error.setStartParsing(frame.startLine, frame.startCol);
							error.setEndParsing(line, column);
							throw error;
						}
					}
					if (rootElement==null) {
						return JsonNull.INSTANCE;
					} else {
						return rootElement.getElement();
					}
				}
				processCodePoint(inByte);
			}
		}
		
		return rootElement.getElement();
	}
	

	private void processCodePoint(int codePoint) throws SyntaxError {
		ParserFrame<?> frame = contextStack.peek();
		if (frame==null) throw new IllegalStateException("Parser problem! The ParserContext stack underflowed! (line "+line+", col "+column+")");
		
		//Do a limited amount of tail call recursion
		try {
			if (frame.context().isComplete()) {
				contextStack.pop();
				frame.supply();
				frame = contextStack.peek();
			}
		} catch (SyntaxError error) {
			error.setStartParsing(frame.startLine, frame.startCol);
			error.setEndParsing(line, column);
			throw error;
		}
		
		try {
			boolean consumed = frame.context.consume(codePoint, this);
			if (frame.context.isComplete()) {
				contextStack.pop();
				frame.supply();
			}
			if (consumed) {
				withheldCodePoint = -1;
				retries=0;
			} else {
				withheldCodePoint = codePoint;
			}
			
		} catch (SyntaxError error) {
			error.setStartParsing(frame.startLine, frame.startCol);
			error.setEndParsing(line, column);
			throw error;
		}
		
		column++;
		if (codePoint=='\n') {
			line++;
			column = 0;
		}
	}
	
	
	/** Pushes a context onto the stack. MAY ONLY BE CALLED BY THE ACTIVE CONTEXT */
	public <T> void push(ParserContext<T> t, Consumer<T> consumer) {
		ParserFrame<T> frame = new ParserFrame<T>(t, consumer);
		frame.startLine = line;
		frame.startCol = column;
		contextStack.push(frame);
	}

	private static class ParserFrame<T> {
		private ParserContext<T> context;
		private Consumer<T> consumer;
		private int startLine = 0;
		private int startCol = 0;

		public ParserFrame(ParserContext<T> context, Consumer<T> consumer) {
			this.context = context;
			this.consumer = consumer;
		}
		
		public ParserContext<T> context() { return context; }
		//public Consumer<T> consumer() { return consumer; }
		
		/** Feed the result directly from the context at this entry to its corresponding consumer */
		public void supply() throws SyntaxError {
			consumer.accept(context.getResult());
		}
	}

	public void throwDelayed(SyntaxError syntaxError) {
		syntaxError.setEndParsing(line, column);
		delayedError = syntaxError;
	}
}
