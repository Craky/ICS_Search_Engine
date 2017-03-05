package com.se.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yathish on 3/2/17.
 */

public class Posting implements Comparable<Posting> {
	private Integer docID;
	private Integer termFreq;
	private List<Integer> positions;

	public Posting(Integer docID, List<Integer> positions) {
		this.docID = docID;
		this.positions = positions;
		this.termFreq = positions.size();
	}
	
	public Posting(Integer docID, Integer position) {
		this.docID = docID;
		this.positions = new ArrayList<Integer>(position);
		this.termFreq = positions.size();
	}	
	
	private void incrementTermFreq(){
		termFreq++;
	}
	
	public void addPosition(Integer position){
		positions.add(position);
		incrementTermFreq();
	}

	public Integer getDocID() {
		return docID;
	}

	public void setDocID(Integer docID) {
		this.docID = docID;
	}

	public Integer getTermFreq() {
		return termFreq;
	}

	public void setTermFreq(Integer termFreq) {
		this.termFreq = termFreq;
	}

	public List<Integer> getPositions() {
		return positions;
	}

	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}

	public int compareTo(Posting arg0) {
		if (this.docID == arg0.docID) {
			return 0;
		}
		return this.docID > arg0.docID ? 1 : -1;
	}

}
