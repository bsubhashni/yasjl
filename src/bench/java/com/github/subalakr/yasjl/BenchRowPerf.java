/*
 * Copyright (c) 2017 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.subalakr.yasjl;

import java.io.InputStream;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;

/**
 * Row emission benchmark performance
 *
 * @author Subhashni Balakrishnan
 */
public class BenchRowPerf {

    ByteBufJsonParser parser;

    static {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    public void start() {
        parser = new ByteBufJsonParser();
        try {
            String response = getResource("sample.json");
        } catch (Exception ex) {
            System.err.println("Decompress samples directory in resources folder before running this");
            return;
        }
        try {
            parseLarge();
            parseObjectish();
            parseAuction();
            parseAuction();
            parseJamendoArray();
            parseJsonExCvg();
            parseJsonExGlossary();
            parseJsonExMenu();
            parseJsonExServlet();
            parseJsonExServletPretty();
            parseJsonExWidget();
            parseNyTimesApi();
            parseYahooApi();
            parseYahoo2Api();
            parseYelp();
            parseYouTube();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getResource(String filename) {
        String path = "/samples/" + filename;
        InputStream stream = getClass().getResourceAsStream(path);
        java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    //source SO :)
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void parseResults(String file, String path) throws Exception {
        String response = getResource(file);

        StringBuilder sb = new StringBuilder();
        sb.append("size: " + humanReadableByteCount(response.length(), true) + "\t\t");
        sb.append("level-depth: " + path.split("/").length + "\t");
        sb.append("time: ");

        long start = System.currentTimeMillis();
        JsonPointer[] jsonPointers = {
                new JsonPointer(path, new JsonPointerCB1() {
                    public void call(ByteBuf buf) {
                        buf.release();
                    }
                })
        };
        ByteBuf buf = Unpooled.buffer();
        parser.initialize(buf, jsonPointers);
        buf.writeBytes(response.getBytes());
        parser.parse();
        long end = System.currentTimeMillis();
        sb.append((end - start) + "ms");
        System.out.println(sb.toString());
    }

    public void parseLarge() throws Exception {
        parseResults("large.json", "/-");
    }

    public void parseObjectish() throws Exception {
        parseResults("objectish.json", "/databaseReference/-/objectId");
    }


    public void parseAuction() throws Exception {
        parseResults("auction", "/realm/alliance/auctions/-");
    }


    public void parseJamendoArray() throws Exception {
        parseResults("jamendo_array", "/-");
    }

    public void parseJsonExCvg() throws Exception {
        parseResults("json.org.example.cvg", "/menu/items/-");
    }

    public void parseJsonExGlossary() throws Exception {
        parseResults("json.org.example.glossary", "/glossary/GlossDiv/GlossList/GlossDef");
    }

    public void parseJsonExMenu() throws Exception {
        parseResults("json.org.example.menu", "/menu/popup/menuitem");
    }

    public void parseJsonExServlet() throws Exception {
        parseResults("json.org.example.servlet", "/web-app/servlet/-");
    }

    public void parseJsonExServletPretty() throws Exception {
        parseResults("json.org.example.servlet_pretty", "/web-app/servlet/-");
    }

    public void parseJsonExWidget() throws Exception {
        parseResults("json.org.example.widget", "/widget/text/onMouseUp");
    }

    public void parseNyTimesApi() throws Exception {
        parseResults("nytimes.api", "/results/0/members/-/api_uri");
    }

    public void parseYahooApi() throws Exception {
        parseResults("yahoo_api", "/ResultSet/Result/-/Url");
    }

    public void parseYahoo2Api() throws Exception {
        parseResults("yahoo2.json", "/ResultSet/Result/-/Url");
    }

    public void parseYelp() throws Exception {
        parseResults("yelp.json", "/businesses/-/avg_rating");
    }

    public void parseYouTube() throws Exception {
        parseResults("youtube_api", "/data/items/-/tags/-");
    }

    public static void main(String[] args) {
        BenchRowPerf bench = new BenchRowPerf();
        bench.start();
    }
}
