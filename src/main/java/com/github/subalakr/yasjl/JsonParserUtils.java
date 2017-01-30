package com.github.subalakr.yasjl;

/**
 * @author Subhashni Balakrishnan
 */
public class JsonParserUtils {

	protected enum Mode {
		JSON_OBJECT,
		JSON_OBJECT_VALUE,
		JSON_ARRAY,
		JSON_ARRAY_VALUE,
		JSON_STRING_HASH_KEY,
		JSON_STRING_VALUE,
		JSON_BOOLEAN_VALUE,
		JSON_NUMBER_VALUE,
		JSON_NULL_VALUE,
		BOM
	}

	protected static final byte O_CURLY = (byte)'{';
	protected static final byte C_CURLY = (byte)'}';
	protected static final byte O_SQUARE = (byte)'[';
	protected static final byte C_SQUARE = (byte)']';

	protected static final byte JSON_ST = (byte)'"';
	protected static final byte JSON_T = (byte)'t';
	protected static final byte JSON_F = (byte)'f';
	protected static final byte JSON_N = (byte)'n';
	protected static final byte JSON_ES = (byte)'\\';
	protected static final byte JSON_COLON = (byte)':';
	protected static final byte JSON_COMMA = (byte)',';

	protected static final byte JSON_MINUS = (byte)'-';
	protected static final byte JSON_PLUS = (byte)'+';
	protected static final byte JSON_ZERO = (byte)'0';

	protected static boolean isNumber(byte value) {
		switch(value) {
			case JSON_MINUS:
			case JSON_ZERO:
			case JSON_PLUS:
				return true;
			default:
				if (value >= (byte)'1' && value <= (byte)'9') {
					return true;
				} else {
					return false;
				}
		}
	}
}
