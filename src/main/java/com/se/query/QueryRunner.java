package com.se.query;

import java.util.Collections;
import java.util.List;

import com.se.algorithm.CosineCalculator;
import com.se.algorithm.ScoringAlgorithm;
import com.se.algorithm.TagWeightCalculator;
import com.se.algorithm.TfIdfCalculator;
import com.se.data.ScoreType;
import com.se.data.SearchResult;
import com.se.db.DatabaseUtil;

public class QueryRunner {

	public List<SearchResult> search(String query) {
		ScoringAlgorithm tfIdfCalculator = new TfIdfCalculator();
		List<SearchResult> results = tfIdfCalculator.calculate(query);
		ScoringAlgorithm cosineCalculator = new CosineCalculator(
				tfIdfCalculator.getSearchResults());
		results = cosineCalculator.calculate(query);
		ScoringAlgorithm tagWeightCalculator = new TagWeightCalculator(
				cosineCalculator.getSearchResults());
		results = tagWeightCalculator.calculate(query);

		Collections.sort(results);
		int NUMBER_OF_SEARCH_RESULTS = results.size();

		if (results.size() > 50) {
			NUMBER_OF_SEARCH_RESULTS = 50;
		}

		List<SearchResult> topKresults = results.subList(0,
				NUMBER_OF_SEARCH_RESULTS);

		DatabaseUtil databaseUtil = DatabaseUtil.create();
		for (SearchResult result: topKresults) {
			result.addScore(ScoreType.PAGERANK, databaseUtil.getPagerank(result.getDocId()));
		}
		
		if (topKresults.size() > 10) {
			NUMBER_OF_SEARCH_RESULTS = 10;
		}

		Collections.sort(topKresults);
		List<SearchResult> finalTopK = topKresults.subList(0,
				NUMBER_OF_SEARCH_RESULTS);

		for (int i = 0; i < NUMBER_OF_SEARCH_RESULTS; i++) {
			SearchResult result = finalTopK.get(i);
			result.setSnippet(Snippet.generate(result.getDocument(),
					result.getPositions()));
		}
		return finalTopK;
	}

	public static void main(String[] args) {
		QueryRunner queryRunner = new QueryRunner();
		for (SearchResult result : queryRunner.search("crista lopes")) {
			System.out.println(result);
		}
	}
}
