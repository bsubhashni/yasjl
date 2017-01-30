package com.github.subalakr.yasjl;

import io.netty.buffer.ByteBufProcessor;

/**
 * Processes JSON null value
 *
 * @author Subhashni Balakrishnan
 */
public class JsonNullByteBufProcessor implements ByteBufProcessor {
	private final byte N1 = (byte)'n';
	private final byte N2 = (byte)'u';
	private final byte N3 = (byte)'l';
	private byte lastValue;

	public JsonNullByteBufProcessor() {
		this.lastValue = N1;
	}

	public void reset() {
		this.lastValue = N1;
	}

	public boolean process(byte value) throws Exception {
		switch (value) {
			case N2:
				if (this.lastValue == N1) {
					this.lastValue = N2;
					return true;
				}
				break;
			case N3:
				if (this.lastValue == N2) {
					this.lastValue = N3;
					return true;
				} else if (this.lastValue == N3) {
					return false;
				}
				break;
		}
		throw new IllegalStateException("Invalid json");
	}
}
