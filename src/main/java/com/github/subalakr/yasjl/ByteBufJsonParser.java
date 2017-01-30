/*
 * Copyright (c) 2017 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.subalakr.yasjl;

import java.nio.charset.Charset;
import java.util.Stack;
import static com.github.subalakr.yasjl.JsonParserUtils.*;
import static com.github.subalakr.yasjl.JsonParserUtils.Mode.JSON_NUMBER_VALUE;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB;
import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import com.github.subalakr.yasjl.Callbacks.JsonPointerCB2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import java.io.EOFException;

/**
 * Query value for json pointers in a ByteBuf
 * Strictly works on utf8. Not a json validator, parses upto json pointer paths and return value
 *
 * Not thread safe
 * @author Subhashni Balakrishnan
 */
public class ByteBufJsonParser {
	private ByteBuf content;
	private JsonPointerTree tree;
	private Stack<JsonLevel> levelStack;
	private JsonLevel currentLevel;
	private JsonWhiteSpaceByteBufProcessor wsProcessor;
	private JsonStringByteBufProcessor stProcessor;
	private JsonArrayByteBufProcessor arProcessor;
	private JsonObjectByteBufProcessor obProcessor;
	private JsonNullByteBufProcessor nullProcessor;
	private JsonBOMByteBufProcessor bomProcessor;
	private JsonNumberByteBufProcessor numProcessor;
	private JsonBooleanTrueByteBufProcessor trueProcessor;
	private JsonBooleanFalseByteBufProcessor falseProcessor;
	private byte currentChar;
	private boolean startedStreaming;

	public ByteBufJsonParser() {
		wsProcessor = new JsonWhiteSpaceByteBufProcessor();
		stProcessor =  new JsonStringByteBufProcessor();
		arProcessor = new JsonArrayByteBufProcessor(stProcessor);
		obProcessor = new JsonObjectByteBufProcessor(stProcessor);
		nullProcessor = new JsonNullByteBufProcessor();
		bomProcessor = new JsonBOMByteBufProcessor();
		numProcessor = new JsonNumberByteBufProcessor();
		trueProcessor = new JsonBooleanTrueByteBufProcessor();
		falseProcessor = new JsonBooleanFalseByteBufProcessor();
	}

	public void initialize(ByteBuf content, JsonPointer[] jsonPointers) throws Exception {
		this.levelStack = new Stack<JsonLevel>();
		this.tree = new JsonPointerTree();

		for (JsonPointer jp: jsonPointers) {
			tree.addJsonPointer(jp);
		}

		this.content = content;
		this.startedStreaming = false;
	}

	public void parse() throws Exception {
		if (levelStack.empty() && !startedStreaming) {
			switch(this.content.readByte()) {
				case (byte)0xEF:
					pushLevel(Mode.BOM);
					break;
				case O_CURLY:
					pushLevel(Mode.JSON_OBJECT);
					break;
				case O_SQUARE:
					pushLevel(Mode.JSON_ARRAY);
					break;
				default:
					throw new IllegalStateException("Only UTF-8 is supported");
			}
			startedStreaming = true;
		}

		while(true) {
			if (levelStack.empty()) {
				return; //nothing more to do
			}
			currentLevel = levelStack.peek();
				Mode currentMode = currentLevel.peekMode();
				switch (currentMode) {
					case BOM:
						readBOM();
						break;
					case JSON_OBJECT:
						readObject(currentLevel);
						break;
					case JSON_ARRAY:
						readArray(currentLevel);
						break;
					case JSON_OBJECT_VALUE:
					case JSON_ARRAY_VALUE:
					case JSON_STRING_HASH_KEY:
					case JSON_STRING_VALUE:
					case JSON_BOOLEAN_TRUE_VALUE:
					case JSON_BOOLEAN_FALSE_VALUE:
					case JSON_NUMBER_VALUE:
					case JSON_NULL_VALUE:
						readValue(currentLevel);
						break;
				}
			}
	}

	private void pushLevel(Mode mode) throws Exception {
		JsonLevel newJsonLevel = null;
		if (mode == Mode.BOM) {
			newJsonLevel = new JsonLevel(mode, new JsonPointer()); //not a valid nesting level
		}
		if (mode == Mode.JSON_OBJECT) {
			if (levelStack.size() > 0) {
				JsonLevel current = levelStack.peek();
				newJsonLevel = new JsonLevel(mode, new JsonPointer(current.jsonPointer().refTokens()));
			} else {
				newJsonLevel = new JsonLevel(mode, new JsonPointer());
			}
		}
		if (mode == Mode.JSON_ARRAY) {
			if (levelStack.size() > 0) {
				JsonLevel current = levelStack.peek();
				newJsonLevel = new JsonLevel(mode, new JsonPointer(current.jsonPointer().refTokens()));
			} else {
				newJsonLevel = new JsonLevel(mode, new JsonPointer());
			}
			newJsonLevel.isArray(true);
			newJsonLevel.setArrayIndexOnJsonPointer();

		}
		levelStack.push(newJsonLevel);
	}

	private void readObject(JsonLevel level) throws Exception {
		while(true) {
			readNextChar(level);
			if (this.currentChar == JSON_ST) {
				if (!level.isHashValue()) {
					level.pushMode(Mode.JSON_STRING_HASH_KEY);
				} else {
					level.pushMode(Mode.JSON_STRING_VALUE);
				}
				readValue(level);
			}
			if (this.currentChar == JSON_COLON) {
				//look for value
				level.isHashValue(true);
			}
			if (this.currentChar == O_CURLY) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				if (this.tree.isIntermediaryPath(level.jsonPointer())) {
					this.pushLevel(Mode.JSON_OBJECT);
					level.removeLastTokenFromJsonPointer();
					return;
				}
				level.pushMode(Mode.JSON_OBJECT_VALUE);
				readValue(level);
			}
			if (this.currentChar == O_SQUARE) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				if (this.tree.isIntermediaryPath(level.jsonPointer())) {
					this.pushLevel(Mode.JSON_ARRAY);
					level.removeLastTokenFromJsonPointer();
					return;
				}
				level.pushMode(Mode.JSON_ARRAY_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_T) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				level.pushMode(Mode.JSON_BOOLEAN_TRUE_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_F) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				level.pushMode(Mode.JSON_BOOLEAN_FALSE_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_N) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				level.pushMode(Mode.JSON_NULL_VALUE);
				readValue(level);
			}
			if (isNumber(this.currentChar)) {
				if (!level.isHashValue()) {
					throw new IllegalStateException();
				}
				level.pushMode(JSON_NUMBER_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_COMMA) {
				level.isHashValue(false);
			}
			if (this.currentChar == C_CURLY) {
				this.levelStack.pop();
				return;
			}
		}
	}

	private void readArray(JsonLevel level) throws Exception {
		while(true) {
			readNextChar(level);
			if (this.currentChar == JSON_ST) {
				level.pushMode(Mode.JSON_STRING_VALUE);
				readValue(level);
			}
			if (this.currentChar == O_CURLY) {
				if (this.tree.isIntermediaryPath(level.jsonPointer())) {
					this.pushLevel(Mode.JSON_OBJECT);
					return;
				}
				level.pushMode(Mode.JSON_OBJECT_VALUE);
				readValue(level);
			}
			if (this.currentChar == O_SQUARE) {
				if (this.tree.isIntermediaryPath(level.jsonPointer())) {
					this.pushLevel(Mode.JSON_ARRAY);
					return;
				} else {
					level.pushMode(Mode.JSON_ARRAY_VALUE);
					readValue(level);
				}
			}
			if (this.currentChar == JSON_T) {
				level.pushMode(Mode.JSON_BOOLEAN_TRUE_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_F) {
				level.pushMode(Mode.JSON_BOOLEAN_FALSE_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_N) {
				level.pushMode(Mode.JSON_NULL_VALUE);
				readValue(level);
			}
			if (isNumber(this.currentChar)) {
				level.pushMode(JSON_NUMBER_VALUE);
				readValue(level);
			}
			if (this.currentChar == JSON_COMMA) {
				level.updateIndex();
				level.setArrayIndexOnJsonPointer();
			}
			if (this.currentChar == C_SQUARE) {
				this.levelStack.pop();
				return;
			}

		}
	}

	private void readValue(JsonLevel level) throws Exception {
		int readerIndex = this.content.readerIndex();
		ByteBufProcessor processor = null;
		Mode mode = level.peekMode();
		switch(mode) {
			case JSON_ARRAY_VALUE:
				arProcessor.reset();
				processor = arProcessor;
				break;
			case JSON_OBJECT_VALUE:
				obProcessor.reset();
				processor = obProcessor;

				break;
			case JSON_STRING_VALUE:
			case JSON_STRING_HASH_KEY:
				processor = stProcessor;
				break;
			case JSON_NULL_VALUE:
				nullProcessor.reset();
				processor = nullProcessor;
				break;
			case JSON_BOOLEAN_TRUE_VALUE:
				processor = trueProcessor;
				break;
			case JSON_BOOLEAN_FALSE_VALUE:
				processor = falseProcessor;
				break;
			case JSON_NUMBER_VALUE:
				processor = numProcessor;
				break;
		}
		int length;
		int lastValidIndex = this.content.forEachByte(processor);
		if (lastValidIndex == -1) {
			if(mode != JSON_NUMBER_VALUE) {
				throw new EOFException("Needs more input (Level: " + level.jsonPointer().toString() + ")");
			} else {
				length = 1;
				ByteBuf slice = this.content.slice(readerIndex - 1, length);
				level.setCurrentValue(slice.copy(), length);
				//no need to skip here
			}
		} else {
			if(mode == Mode.JSON_OBJECT_VALUE ||
					mode == Mode.JSON_ARRAY_VALUE ||
					mode == Mode.JSON_STRING_VALUE ||
					mode == Mode.JSON_STRING_HASH_KEY ||
					mode == Mode.JSON_NULL_VALUE ||
					mode == Mode.JSON_BOOLEAN_TRUE_VALUE ||
					mode == Mode.JSON_BOOLEAN_FALSE_VALUE) {
				length = lastValidIndex - readerIndex + 1;
				ByteBuf slice = this.content.slice(readerIndex - 1, length + 1);
				level.setCurrentValue(slice.copy(), length);
				this.content.skipBytes(length);
			} else {
				//special handling for number as they don't need structural tokens intact
				//and the processor returns only on an unacceptable value rather than a finite state automaton
				length = lastValidIndex - readerIndex;
				if (length > 0) {
					ByteBuf slice = this.content.slice(readerIndex - 1, length + 1);
					level.setCurrentValue(slice.copy(), length);
					this.content.skipBytes(length);
				} else {
					length = 1;
					ByteBuf slice = this.content.slice(readerIndex - 1, length);
					level.setCurrentValue(slice.copy(), length);
				}
			}
		}
		level.emitJsonPointerValue(this.tree.isTerminalPath(level.jsonPointer()));
		this.content.discardReadBytes();

		if (mode != Mode.JSON_STRING_HASH_KEY) {
			level.removeLastTokenFromJsonPointer();
		}
		level.popMode();
	}

	private void readBOM() throws Exception {
		int readerIndex = this.content.readerIndex();
		int lastBOMIndex = this.content.forEachByte(bomProcessor);
		if (lastBOMIndex == -1) {
			throw new EOFException("Need more input");
		}
		if (lastBOMIndex > readerIndex) {
			this.content.skipBytes(lastBOMIndex - readerIndex+1);
		}
		this.levelStack.pop();
		this.content.discardReadBytes();
	}

	private void readNextChar(JsonLevel level) throws Exception {
		int readerIndex = this.content.readerIndex();
		int lastWsIndex = this.content.forEachByte(wsProcessor);
		if (lastWsIndex == -1) {
			throw new EOFException("Needs more input (Level: " + level.jsonPointer().toString() + ")");
		}
		if (lastWsIndex > readerIndex) {
			this.content.skipBytes(lastWsIndex - readerIndex);
		}
		this.currentChar = this.content.readByte();
	}

	/**
	 * JsonLevel can be a nesting level of an json object or json array
	 *
	 */
	class JsonLevel {
		private Stack<Mode> modes;
		private ByteBuf currentValue;
		private JsonPointer jsonPointer;
		private boolean isHashValue;
		private boolean isArray;
		private int arrayIndex;

		public JsonLevel(Mode mode, JsonPointer jsonPointer) {
			this.modes = new Stack<Mode>();
			this.pushMode(mode);
			this.jsonPointer = jsonPointer;
		}

		public void isHashValue(boolean isHashValue) {
			this.isHashValue = isHashValue;
		}

		public boolean isHashValue() {
			return this.isHashValue;
		}

		public void isArray(boolean isArray) {
			this.isArray = isArray;
		}

		public void pushMode(Mode mode) {
			this.modes.push(mode);
		}

		public Mode peekMode() {
			return this.modes.peek();
		}

		public Mode popMode() {
			return this.modes.pop();
		}

		public JsonPointer jsonPointer() {
			return this.jsonPointer;
		}

		public void updateIndex() {
			this.arrayIndex++;
		}

		public void setCurrentValue(ByteBuf value, int length) {
			this.currentValue = value;
			if (peekMode() == Mode.JSON_STRING_HASH_KEY) {
				//strip the quotes
				this.jsonPointer.addToken(this.currentValue.toString(
						this.currentValue.readerIndex()+1, length-1, Charset.defaultCharset()));
			}
		}

		public void setArrayIndexOnJsonPointer() {
			this.jsonPointer.addToken(Integer.toString(this.arrayIndex));
		}

		public void removeLastTokenFromJsonPointer() {
			this.jsonPointer.removeToken();
		}

		public void emitJsonPointerValue(boolean shouldEmit){
			if (shouldEmit && (this.isHashValue || this.isArray)) {
				if (this.jsonPointer.jsonPointerCB() != null) {
					JsonPointerCB cb = this.jsonPointer.jsonPointerCB();
					if (cb instanceof JsonPointerCB1) {
						((JsonPointerCB1) cb).call(this.currentValue);
					} else if (cb instanceof JsonPointerCB2) {
						((JsonPointerCB2) cb).call(this.jsonPointer, this.currentValue);
					}
				} else {
					this.currentValue.release();
				}
			} else {
				this.currentValue.release();
			}
		}
	}
}
