package com.github.subalakr.yasjl;

import io.netty.buffer.ByteBufProcessor;

/**
 * Processes byte order mark. It supports only UTF-8.
 *
 * @author Subhashni Balakrishnan
 */
public class JsonBOMByteBufProcessor implements ByteBufProcessor {
	private final byte BOM1 = (byte)0xEF;
	private final byte BOM2 = (byte)0xBB;
	private final byte BOM3 = (byte)0xBF;
	private byte lastValue;

	public JsonBOMByteBufProcessor() {
		this.lastValue = BOM1;
	}

	private void reset() {
		this.lastValue = BOM1;
	}

	public boolean process(byte value) throws Exception {
		switch(value) {
			case BOM2:
				if (this.lastValue == BOM1) {
					this.lastValue = BOM2;
					return true;
				}
				break;
			case BOM3:
				if (this.lastValue == BOM2) {
					return false;
				}
				break;
		}
		throw new IllegalStateException("Invalid json");
	}
}
