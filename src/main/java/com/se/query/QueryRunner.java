package com.se.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.se.algorithm.AnchorTextCalculator;
import com.se.algorithm.CosineCalculator;
import com.se.algorithm.ScoringAlgorithm;
import com.se.algorithm.TagWeightCalculator;
import com.se.algorithm.TfIdfCalculator;
import com.se.data.Document;
import com.se.data.InvertedIndex;
import com.se.data.ScoreType;
import com.se.data.SearchResult;
import com.se.db.DatabaseUtil;
import com.se.file.FileHandler;
import com.se.index.WordsTokenizer;
import com.se.util.NDCG;

public class QueryRunner {
	private static final Logger logger = LoggerFactory
			.getLogger(QueryRunner.class);
	private static final StopWatch watch = new Slf4JStopWatch(logger);

	public List<SearchResult> search(String query) {
		Map<Integer, SearchResult> searchResults;
		DatabaseUtil databaseUtil = DatabaseUtil.create();
		Map<String, InvertedIndex> tokenToInvertedIndex = databaseUtil
				.getInvertedIndexRows(query);
		watch.lap("Fetched from database");

		ScoringAlgorithm tfIdfCalculator = new TfIdfCalculator(
				tokenToInvertedIndex);
		searchResults = tfIdfCalculator.calculate(query);
		watch.lap("TFIDFCalculator");

		ScoringAlgorithm cosineCalculator = new CosineCalculator(searchResults,
				tokenToInvertedIndex);
		searchResults = cosineCalculator.calculate(query);
		watch.lap("CosineCalculator");

		ScoringAlgorithm anchorTextCalculator = new AnchorTextCalculator(
				searchResults);
		searchResults = anchorTextCalculator.calculate(query);
		watch.lap("AnchorTextCalculator");

		ScoringAlgorithm tagWeightCalculator = new TagWeightCalculator(
				searchResults, tokenToInvertedIndex);
		searchResults = tagWeightCalculator.calculate(query);
		watch.lap("TagWeightCalculator");

		List<SearchResult> results = new ArrayList<>(searchResults.values());
		results = filterSearchResults(results, 100);

		for (SearchResult result : results) {
			result.addScore(ScoreType.PAGERANK,
					databaseUtil.getPagerank(result.getDocId()));
		}

		normalizeScores(results);

		results = filterSearchResults(results, 10);
		for (int i = 0; i < 10; i++) {
			SearchResult result = results.get(i);
			Document document = result.getDocument();
			String snippet = Snippet.generate(document, result.getPositions());
			result.setSnippet(highlight(snippet, query));
			result.setTitle(FileHandler.getTitle(document.getfilePath(),
					document.getUrl()));
		}
		databaseUtil.close();
		watch.stop("PageRank");

		return results;
	}

	private List<SearchResult> filterSearchResults(List<SearchResult> results,
			int limit) {
		int size = results.size();
		if (results.size() > limit) {
			size = limit;
		}
		Collections.sort(results);
		return results.subList(0, size);
	}

	private void normalizeScores(List<SearchResult> results) {
		Map<ScoreType, Double> maximums = new HashMap<ScoreType, Double>();
		for (ScoreType scoreType : ScoreType.values()) {
			maximums.put(scoreType, 0.0);
		}

		for (SearchResult result : results) {
			for (Entry<ScoreType, Double> entry : result.getScores().entrySet()) {
				ScoreType scoreType = entry.getKey();
				maximums.put(scoreType,
						Math.max(maximums.get(scoreType), entry.getValue()));
			}
		}

		for (ScoreType scoreType : ScoreType.values()) {
			Double maximum = maximums.get(scoreType);
			if (maximum == 0.0) {
				continue;
			}
			for (SearchResult result : results) {
				result.setScore(scoreType, result.getScore(scoreType)
						/ maximums.get(scoreType));
			}
		}
	}

	private String highlight(String snippet, String query) {
		List<String> tokensList = WordsTokenizer.tokenize(query);
		Set<String> queryTokens = new HashSet<>();
		for (String string : tokensList) {
			queryTokens.add(string);
		}
		List<String> snippetTokensList = WordsTokenizer.tokenize(snippet);
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : snippetTokensList) {
			if (queryTokens.contains(string)) {
				stringBuilder.append("<b>" + string + "</b>");
			} else {
				stringBuilder.append(string);
			}
			stringBuilder.append(" ");
		}

		return stringBuilder.toString();
	}

	public static void main(String[] args) {
		String query = "software engineering";
		QueryRunner queryRunner = new QueryRunner();
		List<SearchResult> searchResults = queryRunner.search(query);
		for (SearchResult result : searchResults) {
			System.out.println(result);
		}
		NDCG ndcg = new NDCG();
		ndcg.findNDCG(query, searchResults);
	}
}
