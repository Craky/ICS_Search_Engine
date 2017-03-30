package com.se.index;

/**
 * Created by Yathish on 3/2/17.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import weka.core.Stopwords;

public class WordsTokenizer {

	public static List<String> tokenizeWithStemmingFilterStop(String text) {
		List<String> strings = new ArrayList<>();
		Matcher m = Pattern.compile("[^\\W_]+").matcher(text.toLowerCase());
		while (m.find()) {
			String currentWord = m.group(0);
			if (isStopWord(currentWord)) {
				continue;
			}
			currentWord = stem(currentWord);
			strings.add(currentWord);
		}
		return strings;
	}
	
	public static List<String> tokenize(String text) {
		List<String> strings = new ArrayList<>();
		Matcher m = Pattern.compile("[^\\W_]+").matcher(text);
		while (m.find()) {
			String currentWord = m.group(0);
			strings.add(currentWord);
		}
		return strings;
	}

	private static String stem(String currentWord) {
		SnowballStemmer snowballStemmer = new englishStemmer();
		snowballStemmer.setCurrent(currentWord);
		snowballStemmer.stem();
		return snowballStemmer.getCurrent();
	}


	public static boolean isStopWord(String currentWord) {
		return currentWord.length() < 3 || Stopwords.isStopword(currentWord) || currentWord.matches("[0-9]+");
	}

}