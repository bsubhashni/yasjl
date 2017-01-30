package com.github.subalakr.yasjl;

import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import org.junit.Before;
import org.junit.Test;

public class QueryResponseByteBufJsonParserTest {
	ByteBufJsonParser parser;


	static {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
	}

	@Before
	public void setup() {
		parser = new ByteBufJsonParser();
	}

	private String getResource(String filename) {
		String path = "/" + filename;
		InputStream stream =  getClass().getResourceAsStream(path);
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private boolean parse(String file) throws Exception {
		final Map<String, Integer> results = new HashMap<String, Integer>();
		JsonPointer[] jsonPointers = {
				new JsonPointer("/metrics/resultCount", new JsonPointerCB1() {
					public void call(ByteBuf buf) {
						String val =  buf.toString(Charset.defaultCharset());
						if (val.length() >0) {
							results.put("resultCount", Integer.parseInt(val));
						}
						buf.release();
					}
				}),
				new JsonPointer("/metrics/warningCount", new JsonPointerCB1() {
				public void call(ByteBuf buf) {
					String val =  buf.toString(Charset.defaultCharset());
					if (val.length() >0) {
						results.put("warningCount", Integer.parseInt(val));
					}
					buf.release();
				}
				}),
				new JsonPointer("/metrics/errorCount", new JsonPointerCB1() {
					public void call(ByteBuf buf) {
						String val = buf.toString(Charset.defaultCharset());
						if (val.length() > 0) {
							results.put("errorCount", Integer.parseInt(val));
						}
						buf.release();
					}
				}),
				new JsonPointer("/results/-", new JsonPointerCB1() {
					public void call(ByteBuf buf) {
						if (results.containsKey("resultRows")) {
							results.put("resultRows", results.get("resultRows")+1);
						} else {
							results.put("resultRows", 1);
						}

						buf.release();
					}
				}),
				new JsonPointer("/errors/-", new JsonPointerCB1() {
					public void call(ByteBuf buf) {
						if (results.containsKey("errorRows")) {
							results.put("errorRows", results.get("errorRows")+1);
						} else {
							results.put("errorRows", 1);
						}

						buf.release();
					}
				}),
				new JsonPointer("/warnings/-", new JsonPointerCB1() {
					public void call(ByteBuf buf) {
						if (results.containsKey("warningRows")) {
							results.put("warningRows", results.get("warningRows")+1);
						} else {
							results.put("warningRows", 1);
						}

						buf.release();
					}
				}),

		};

		String response = getResource(file);
		ByteBuf buf = Unpooled.buffer();
		parser.initialize(buf, jsonPointers);

		int mid = response.length() / 2;
		int quarter = mid / 2;
		buf.writeBytes(response.substring(0, quarter).getBytes());
		try {
			parser.parse();
		} catch (EOFException ex) {
			//ignore EOF
		}

		buf.writeBytes(response.substring(quarter, mid).getBytes());
		try {
			parser.parse();
		} catch (EOFException ex) {
		}

		buf.writeBytes(response.substring(mid, response.length()).getBytes());
		parser.parse();


		if (results.containsKey("resultCount") && results.get("resultCount") > 0) {
			if (results.get("resultRows") != results.get("resultCount")) {
				return false;
			}
		}

		if (results.containsKey("errorCount") && results.get("errorCount") > 0) {
			if (results.get("errorRows") != results.get("errorCount")) {
				return false;
			}
		}

		if (results.containsKey("warningCount") && results.get("warningCount") > 0) {
			if (results.get("warningRows") != results.get("warningCount")) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void testEmptyResults() throws Exception {
		assertTrue(parse("empty_results.json"));
	}

	@Test
	public void testChunked() throws Exception {
		assertTrue(parse("chunked.json"));
	}

	@Test
	public void testErrorsAndWarnings() throws Exception {
		assertTrue(parse("error_and_warnings.json"));
	}

	@Test
	public void testNoClientId() throws Exception {
		assertTrue(parse("no_client_id.json"));
	}

	@Test
	public void testNoPretty() throws Exception {
		assertTrue(parse("no_pretty.json"));
	}

	@Test
	public void testRawSuccess1() throws Exception {
		assertTrue(parse("raw_success_1.json"));
	}

	@Test
	public void testRawSuccess8() throws Exception {
		assertTrue(parse("raw_success_8.json"));
	}

	@Test
	public void testShortClientId() throws Exception {
		assertTrue(parse("short_client_id.json"));
	}

	@Test
	public void testSignatureArray() throws Exception {
		assertTrue(parse("signature_array.json"));
	}

	@Test
	public void testSignatureNull() throws Exception {
		assertTrue(parse("signature_null.json"));
	}

	@Test
	public void testSignatureScalar() throws Exception {
		assertTrue(parse("signature_scalar.json"));
	}

	@Test
	public void testSuccess0() throws Exception {
		assertTrue(parse("success_0.json"));
	}

	@Test
	public void testSuccess1() throws Exception {
		assertTrue(parse("success_1.json"));
	}


	@Test
	public void testSuccess5() throws Exception {
		assertTrue(parse("success_5.json"));
	}

	@Test
	public void testSuccessNoMetrics() throws Exception {
		assertTrue(parse("success_no_metrics.json"));
	}

	@Test
	public void testEscapedQuotes() throws Exception {
		assertTrue(parse("with_escaped_quotes.json"));
	}
}
