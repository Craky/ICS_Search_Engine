package com.se.index;

/**
 * Created by Yathish on 3/2/17.
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import weka.core.Stopwords;

import com.se.data.Posting;
import com.se.data.parsedDocument;

public class Tokenizer {

	public static parsedDocument tokenize(File file, Integer docID,
			String url) {
		HashSet<String> set = new HashSet<>();
		set.add("style");
		set.add("script");
		set.add("document");
		Document doc;
		Map<String, Posting> postingMap = new HashMap<>();
		Integer dLen=0;
		parsedDocument pDoc = new parsedDocument(dLen, postingMap);
		try {
			doc = Jsoup.parse(file, "UTF-8", url);
			dLen = tokenize(docID, doc.text(), postingMap);
		} catch (IOException | NullPointerException | IllegalArgumentException e) {
			System.err.println(file);
			System.err.println("Error while parsing. " + e);
		}
		pDoc.setDocLength(dLen);
		pDoc.setPostingMap(postingMap);
		return pDoc;
	}

	private static Integer tokenize(Integer docID, String text,
			Map<String, Posting> postingMap) {
		text = text.toLowerCase();
		Matcher m = Pattern.compile("[^\\W_]+").matcher(text);
		Integer wordPosition = 0;
		while (m.find()) {
			String currentWord = m.group(0);
			wordPosition++;
			if (isStopWord(currentWord)) { continue; }
			currentWord = stem(currentWord);

			if (postingMap.containsKey(currentWord)) {
				Posting seenTerm = postingMap.get(currentWord);
				seenTerm.addPosition(wordPosition);
				postingMap.put(currentWord, seenTerm);
			} else {
				Posting newTerm = new Posting(docID, wordPosition);
				postingMap.put(currentWord, newTerm);
			}
		}
		return wordPosition;
	}

	private static String stem(String currentWord) {
		SnowballStemmer snowballStemmer = new englishStemmer();
		snowballStemmer.setCurrent(currentWord);
		snowballStemmer.stem();
		return snowballStemmer.getCurrent();
	}

	private static boolean isStopWord(String currentWord) {
		if (currentWord.length() < 3) {
			return true;
		}
		return Stopwords.isStopword(currentWord);
	}

}