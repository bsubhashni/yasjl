package com.github.subalakr.yasjl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rx.subjects.Subject;

/**
 * Represents a pointer
 *
 * @author Subhashni Balakrishnan
 */
public class JsonPointer {
	private List<String> refTokens;
	private Subject subject;

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

	public JsonPointer(final String path, Subject subject) {
		parseComponents(path);
		this.subject = subject;
	}

	public void parseComponents(String path) {
		this.refTokens = new ArrayList<String>();
		//split by path each separated by "\"
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

	protected Subject subject() {
		return this.subject;
	}

	protected void subject(Subject subject) {
		this.subject = subject;
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
