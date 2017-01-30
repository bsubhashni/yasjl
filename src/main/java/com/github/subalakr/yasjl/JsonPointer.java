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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.subalakr.yasjl.Callbacks.JsonPointerCB;
import com.github.subalakr.yasjl.Callbacks.JsonPointerCB1;
import com.github.subalakr.yasjl.Callbacks.JsonPointerCB2;

/**
 * Represents a pointer
 *
 * @author Subhashni Balakrishnan
 */
public class JsonPointer {
	private List<String> refTokens;
	private JsonPointerCB jsonPointerCB;

	protected JsonPointer(){
		this.refTokens = new ArrayList<String>();
		this.addToken(""); //token for root
	}

	public JsonPointer(final List<String> refTokens) {
		this.refTokens = new ArrayList(refTokens);
	}

	public JsonPointer(final String path) {
		parseComponents(path);
	}

	public JsonPointer(final String path, JsonPointerCB jsonPointerCB) {
		parseComponents(path);
		this.jsonPointerCB = jsonPointerCB;
	}

	public void parseComponents(String path) {
		this.refTokens = new ArrayList<String>();
		//split by path each separated by "/"
		String[] splitted = path.split("/");
		if (splitted.length > 31) {
			throw new IllegalArgumentException("path contains too many levels of nesting");
		}
		//TODO: Need to verify if valid
		this.refTokens.addAll(Arrays.asList(splitted));
	}

	protected void addToken(String token) {
		this.refTokens.add(token);
	}

	protected void removeToken() {
		this.refTokens.remove(this.refTokens.size()-1);
	}

	protected List<String> refTokens() {
		return this.refTokens;
	}

	protected JsonPointerCB jsonPointerCB() {
		return this.jsonPointerCB;
	}

	protected void jsonPointerCB(JsonPointerCB jsonPointerCB) {
		this.jsonPointerCB = jsonPointerCB;
	}

	private String getPath() {
		StringBuilder sb = new StringBuilder();
		for(String refToken:this.refTokens) {
			sb.append("/");
			sb.append(refToken);
		}
		return sb.substring(1).toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("JsonPointer{path=");
		sb.append(getPath());
		sb.append("}");
		return sb.toString();
	}

}
