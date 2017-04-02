package com.se.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SearchResult implements Comparable<SearchResult> {
	private Document document;
	private Map<ScoreType, Double> scores;
	private String snippet;
	private List<List<Integer>> positions;
	private String title;

	public SearchResult() {
		scores = new EnumMap<>(ScoreType.class);
		positions = new ArrayList<>();
	}

	public Double getScore(ScoreType scoreType) {
		if (scores.containsKey(scoreType)) {
			return scores.get(scoreType);
		}
		return 0.0;
	}

	public void setScore(ScoreType scoreType, Double score) {
		this.scores.put(scoreType, score);
	}

	public void addScore(ScoreType scoreType, Double addToScore) {
		setScore(scoreType, getScore(scoreType) + addToScore);
	}

	@Override
	public int compareTo(SearchResult arg0) {
		return Double.compare(arg0.getTotalScore(), this.getTotalScore());
	}

	public Double getTotalScore() {
		Double inverseSum = 0.0;
		for (Entry<ScoreType, Double> entry : scores.entrySet()) {
			inverseSum += (1 / (entry.getKey().getScoringWeight() * entry
					.getValue()));
		}
		return scores.size() / inverseSum;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void addPositions(List<Integer> position) {
		this.positions.add(position);
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public List<List<Integer>> getPositions() {
		return positions;
	}

	public void setPositions(List<List<Integer>> positions) {
		this.positions = positions;
	}

	public String getUrl() {
		return document.getUrl();
	}

	public Integer getDocId() {
		return document.getDocID();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "SearchResult [document=" + document + ", scores=" + scores
				+ ", snippet=" + snippet + ", positions=" + positions
				+ ", title=" + title + "]";
	}

	public Map<ScoreType, Double> getScores() {
		return scores;
	}

}
