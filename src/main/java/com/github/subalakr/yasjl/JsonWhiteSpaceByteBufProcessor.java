package com.github.subalakr.yasjl;

import io.netty.buffer.ByteBufProcessor;

/**
 * Created by subhashni on 1/29/17.
 */
public class JsonWhiteSpaceByteBufProcessor implements ByteBufProcessor {

	private final byte WS_SPACE = (byte)0x20;
	private final byte WS_TAB = (byte)0X09;
	private final byte WS_LF = (byte)0x0A;
	private final byte WS_CR = (byte)0x0D;

	public boolean process(byte value) throws Exception {
		switch(value) {
			case WS_SPACE:
			case WS_TAB:
			case WS_LF:
			case WS_CR:
				return true;
			default:
				return false;
		}
	}
}
