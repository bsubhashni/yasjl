package com.github.subalakr.yasjl;

import io.netty.buffer.ByteBufProcessor;

/**
 * Processes JSON object value
 *
 * @author Subhashni Balakrishnan
 */
public class JsonObjectByteBufProcessor implements ByteBufProcessor {
	private final byte openingChar = (byte)'{';
	private final byte closingChar = (byte)'}';
	private final byte JSON_ST = (byte)'"';

	private boolean isString;
	private int count;
	private JsonStringByteBufProcessor stProcessor;


	public JsonObjectByteBufProcessor(JsonStringByteBufProcessor stProcessor) {
		this.count = 1;
		this.stProcessor = stProcessor;
	}

	public void reset() {
		this.count = 1;
		this.isString = false;
	}

	public boolean process(byte value) throws Exception {
		if (this.isString) {
			this.isString = this.stProcessor.process(value);
			return true;
		} else {
			switch (value) {
				case openingChar:
					this.count++;
					return true;
				case closingChar:
					this.count--;
					if (count == 0) {
						return false;
					} else {
						return true;
					}
				case JSON_ST:
					this.isString = true;
					return true;
				default:
					return true;
			}
		}
	}
}