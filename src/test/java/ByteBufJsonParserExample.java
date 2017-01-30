import com.github.subalakr.yasjl.ByteBufJsonParser;
import com.github.subalakr.yasjl.JsonPointer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ByteBufJsonParserExample {
	static ByteBufJsonParser parser;


	private static String getResource(String path) {
		InputStream stream =  ByteBufJsonParserExample.class.getResourceAsStream(path);
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static void main(String[] args) {
		parser = new ByteBufJsonParser();
		try {
			simpleTest();
			System.out.println("====Empty results====");
			parseChunked("empty_results.json");
			System.out.println("====Raw Success====");
			parseChunked("raw_success.json");
			System.out.println("====No pretty====");
			parseChunked("no_pretty.json");
			System.out.println("====escaped quotes====");
			parseChunked("escaped_quotes.json");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void simpleTest() {
		ByteBuf buf = Unpooled.buffer();
		try {
			JsonPointer[] jsonPointers = {
					new JsonPointer("/foo/0/first"),
			};
			parser.initialize(buf, jsonPointers);
			buf.writeBytes(("{\"foo\"").getBytes());
			buf.writeBytes((":[").getBytes());

			try {
				parser.parse();
			} catch(EOFException ex) {
			}

			buf.writeBytes(("{\"first\": \"second\"} ],").getBytes());

			try {
				parser.parse();
			} catch(EOFException ex) {
			}
			buf.writeBytes(("\"t\" : [null],\"bar\":null}").getBytes());
			parser.parse();

		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			buf.release();
		}
	}

	public static void parseChunked(String path) throws Exception {

		BehaviorSubject<ByteBuf> results = BehaviorSubject.create();

		JsonPointer[] jsonPointers = {
					new JsonPointer("/clientContextID"),
				    new JsonPointer("/metrics/elapsedTime"),
				    new JsonPointer("/results/-", results),
					new JsonPointer("/status")
		};

		results.subscribe(new Action1<ByteBuf>(){
			public void call(ByteBuf buf) {
				System.out.println("got next val: " + buf.toString(Charset.defaultCharset()));
			}
		});

		String response = getResource(path);
		ByteBuf buf = Unpooled.buffer();

		parser.initialize(buf, jsonPointers);

		int mid = response.length()/2;
		int quarter = mid/2;
		buf.writeBytes(response.substring(0, quarter).getBytes());
		try {
			parser.parse();
		} catch(EOFException ex) {
		}

		buf.writeBytes(response.substring(quarter, mid).getBytes());
		try {
			parser.parse();
		} catch(EOFException ex) {
		}

		buf.writeBytes(response.substring(mid, response.length()).getBytes());
		parser.parse();
		buf.release();
	}

}
