package com.se.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.se.data.AnchorPosting;
import com.se.data.AnchorTextToken;
import com.se.data.Document;
import com.se.data.ScoreType;
import com.se.data.SearchResult;
import com.se.db.DatabaseUtil;
import com.se.index.WordsTokenizer;

public class AnchorTextCalculator implements ScoringAlgorithm {
	private DatabaseUtil databaseUtil;
	private Map<Integer, SearchResult> searchResults;
	private static final int MAX_SEARCH_RESULTS_PER_TERM = 50;

	public AnchorTextCalculator() {
		this(new HashMap<Integer, SearchResult>());
	}

	public AnchorTextCalculator(Map<Integer, SearchResult> searchresults) {
		this.searchResults = searchresults;
		this.databaseUtil = DatabaseUtil.create();
	}

	@Override
	public Map<Integer, SearchResult> calculate(String query) {
		if (ScoreType.ANCHOR_TEXT.isDisabled()) {
			return searchResults;
		}

		for (String term : WordsTokenizer.tokenizeWithStemmingFilterStop(query
				.toLowerCase())) {
			AnchorTextToken anchorTextToken = databaseUtil
					.searchAnchorText(term);
			if (anchorTextToken == null) {
				continue;
			}
			List<AnchorPosting> postings = anchorTextToken.getTargetDocIds();

			int noOfSearchResults = Math.min(MAX_SEARCH_RESULTS_PER_TERM,
					postings.size());
			for (int i = 0; i < noOfSearchResults; i++) {
				AnchorPosting posting = postings.get(i);
				SearchResult searchResult;
				Integer docId = posting.getDocID();
				if (searchResults.containsKey(docId)) {
					searchResult = searchResults.get(docId);
				} else {
					searchResult = new SearchResult();
					Document document = databaseUtil.searchDocument(docId);
					searchResult.setDocument(document);
					searchResults.put(docId, searchResult);
				}
				searchResult.addScore(ScoreType.ANCHOR_TEXT, posting
						.getSourceDocIdsSize().doubleValue());
			}
		}

		return searchResults;
	}

}
