package com.se.data;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("InvertedIndex")
public class InvertedIndex {
	@Id
	private String term;
	private List<Posting> postings;
	private int docFrq;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public List<Posting> getPostings() {
		return postings;
	}

	public void setPostings(List<Posting> postings) {
		this.postings = postings;
	}

	public int getDocFrq() {
		return docFrq;
	}

	public void setDocFrq(int docFrq) {
		this.docFrq = docFrq;
	}

	@Override
	public String toString() {
		return "InvertedIndex [term=" + term + ", postings=" + postings
				+ ", docFrq=" + docFrq + "]";
	}

}
