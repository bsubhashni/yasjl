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

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.atomic.AtomicLong;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;

/**
 * @author Subhashni Balakrishnan
 */
public class Bench {

	private String inJson;
	private long iterations;
	private ByteBufJsonParser parser;
	private long totalDuration;
	private long totalBytesRead;
	private final AtomicLong rowsEmitted;
	private long inJsonSz;

	static {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
	}

	public Bench(File file, long iterations) throws Exception {
		FileInputStream stream = new FileInputStream(file);
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		this.inJson = s.hasNext() ? s.next() : "";
		parser = new ByteBufJsonParser();
		this.iterations = iterations;
		this.rowsEmitted = new AtomicLong();
		this.inJsonSz = inJson.length();
	}

	public void run() throws Exception {
		for (int i = 0; i < iterations; i++) {
			ByteBuf inBuf = Unpooled.buffer();
			JsonPointer[] jsonPointers = {
					new JsonPointer("/metrics/resultCount", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							buf.release();
						}
					}),
					new JsonPointer("/metrics/warningCount", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							buf.release();
						}
					}),
					new JsonPointer("/metrics/errorCount", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							buf.release();
						}
					}),
					new JsonPointer("/results/-", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							rowsEmitted.getAndIncrement();
							buf.release();
						}
					}),
					new JsonPointer("/errors/-", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							buf.release();
						}
					}),
					new JsonPointer("/warnings/-", new JsonPointerCB1() {
						public void call(ByteBuf buf) {
							buf.release();
						}
					}),
			};
			parser.initialize(inBuf, jsonPointers);
			inBuf.writeBytes(inJson.getBytes());
			long start = System.currentTimeMillis();
			parser.parse();
			long currentRun = System.currentTimeMillis() - start;
			totalDuration += currentRun;
			totalBytesRead += inJsonSz;
			inBuf.discardReadBytes();
			inBuf.release();
		}
	}

	public void printResults() {
		double totalDurationInSecs = (double) totalDuration / 1000;
		double rate = totalBytesRead / (totalDurationInSecs * 1000 * 1000); //convert to MB/s
		System.out.println("Rate: " + String.format("%.2f", rate) + " MB/s");
		System.out.println("Rows emitted: " + rowsEmitted.get());
	}

	static void printUsage() {
		System.out.println("Works for valid query responses");
		System.out.println("usage: bench <file.json> <iterations>");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("No enough arguments.");
			printUsage();
			System.exit(1);
		}

		String filePath = args[0];
		File file = new File(filePath);
		if (!file.exists() || file.isDirectory()) {
			System.err.println("Invalid json file");
			printUsage();
			System.exit(1);
		}

		long iterations = 0;

		try {
			iterations = Long.parseLong(args[1]);
		} catch (NumberFormatException ex) {
			System.err.println("Incorrect format for number of iterations.");
			printUsage();
			System.exit(1);
		}

		try {
			Bench bench = new Bench(file, iterations);
			bench.run();
			bench.printResults();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
