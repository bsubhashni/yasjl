package com.github.subalakr.yasjl;

import static com.github.subalakr.yasjl.JsonParserUtils.*;

import io.netty.buffer.ByteBufProcessor;

/**
 * Process JSON number
 *
 * @author Subhashni Balakrishnan
 */
public class JsonNumberByteBufProcessor  implements ByteBufProcessor {

	public JsonNumberByteBufProcessor() {
	}

	//not verifying if valid
	public boolean process(byte value) throws Exception {
		if (value == (byte)'e' || value == (byte)'E') {
			return true;
		}
		if (value >= (byte)'0' && value <= (byte)'9') {
			return true;
		}
		if (value == JSON_MINUS || value == JSON_PLUS) {
			return true;
		}
		if (value == (byte)'.') {
			return true;
		}
		return false;
	}
}
