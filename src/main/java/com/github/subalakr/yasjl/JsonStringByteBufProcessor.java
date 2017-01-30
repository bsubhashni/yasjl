package com.github.subalakr.yasjl;


import io.netty.buffer.ByteBufProcessor;

/**
 * Created by subhashni on 1/29/17.
 */
public class JsonStringByteBufProcessor implements ByteBufProcessor {
	private final byte closingChar = (byte)'"';
	private final byte escape = (byte)'\\';
	private State currentState;

	private enum State {
		UNESCAPED, ESCAPED
	}

	public JsonStringByteBufProcessor() {
		this.currentState = State.UNESCAPED;
	}

	public void reset(){
		this.currentState = State.UNESCAPED;
	}

	public boolean process(byte value) throws Exception {
		switch(value) {
			case escape:
				if (this.currentState == State.UNESCAPED) {
					this.currentState = State.ESCAPED;
				} else {
					this.currentState = State.UNESCAPED;
				}
				return true;
			case closingChar:
				if (this.currentState == State.ESCAPED) {
					this.currentState = State.UNESCAPED;
					return true;
				} else {
					reset();
					return false;
				}
			default:
				return true;
		}
	}

}
