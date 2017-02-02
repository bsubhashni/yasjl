package com.github.subalakr.yasjl;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import java.io.EOFException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Subhashni Balakrishnan
 */
public class SimpleParserTest {
    ByteBufJsonParser parser;

    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    @Before
    public void setup() {
        parser = new ByteBufJsonParser();
    }

    public Map<String, Object> writeSimpleJsonAndParse(String path) throws Exception {
        ByteBuf buf = Unpooled.buffer();

        final Map<String, Object> results = new HashMap<String, Object>();
        final int[] parseCount = new int[1];
        parseCount[0] = 0;
        JsonPointer[] jp = { new JsonPointer(path, new JsonPointerCB1() {
            public void call(ByteBuf buf) {
                results.put("value", buf.toString(Charset.defaultCharset()));
                results.put("parseCount", parseCount[0]);
                buf.release();
            }
        })};
        parser.initialize(buf, jp);

        try {
            buf.writeBytes("{\"foo\": [\"bar\", \"baz\"],".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"\":0,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"a/b\": 1,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"c%d\": 2,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"e^f\": 3,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"g|h\": 4,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\"i\\j\": 5,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }


        try {
            buf.writeBytes("\"k\\\"l\": 6,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {
        }

        try {
            buf.writeBytes("\" \": 7,".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {

        }

        try {
            buf.writeBytes("\"m~n\": 8}".getBytes());
            parseCount[0]++;
            parser.parse();
        } catch(EOFException ex) {

        }

        return results;
    }

    @Test
    public void testJsonArrayPointerValue() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/foo");
        assertEquals("[\"bar\", \"baz\"]", results.get("value").toString());
        assertEquals(1, results.get("parseCount"));
    }

    @Test
    public void testJsonArrayElementPointerValue() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/foo/0");
        assertEquals("\"bar\"", results.get("value").toString());
        assertEquals(1, results.get("parseCount"));
    }

    @Test
    public void testEscapedPathValue() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/a~1b");
        assertEquals("1", results.get("value").toString());
        assertEquals(3, results.get("parseCount"));
    }

    @Test
    public void testSpecialCharPathValue_1() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/c%d");
        assertEquals("2", results.get("value").toString());
        assertEquals(4, results.get("parseCount"));
    }

    @Test
    public void testSpecialCharPathValue_2() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/e^f");
        assertEquals("3", results.get("value").toString());
        assertEquals(5, results.get("parseCount"));
    }


    @Test
    public void testSpecialCharPathValue_3() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/g|h");
        assertEquals("4", results.get("value").toString());
        assertEquals(6, results.get("parseCount"));
    }

    @Test
    public void testSpecialCharPathValue_4() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/i\\j");
        assertEquals("5", results.get("value").toString());
        assertEquals(7, results.get("parseCount"));
    }

    @Test
    public void testSpecialCharPathValue_5() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/k\\\"l");
        assertEquals("6", results.get("value").toString());
        assertEquals(8, results.get("parseCount"));
    }

    @Test
    public void testSpecialCharPathValue_6() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/ ");
        assertEquals("7", results.get("value").toString());
        assertEquals(9, results.get("parseCount"));
    }


    @Test
    public void testSpecialCharPathValue_7() throws Exception {
        Map<String, Object> results = writeSimpleJsonAndParse("/m~n");
        assertEquals("8", results.get("value").toString());
        assertEquals(10, results.get("parseCount"));
    }

}
