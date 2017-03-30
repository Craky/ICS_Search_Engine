package com.se.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonSerializationException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.se.data.AnchorTextToken;
import com.se.data.Document;
import com.se.data.InvertedIndex;
import com.se.data.PageRankData;
import com.se.index.WordsTokenizer;

public class DatabaseUtil {
	private static final String DATABASE_NAME = "searchengine";
	private final Datastore datastore;
	private static DatabaseUtil databaseUtil = null;
	private MongoClient mongoClient;
	private static final String HOST = "mongodb://vivek:cs221@ds145780.mlab.com:45780/searchengine";

	private DatabaseUtil() {
		MongoClientURI uri  = new MongoClientURI(HOST);
		mongoClient = new MongoClient(uri);
		Morphia morphia = new Morphia();

		morphia.mapPackage("com.se.index");
		morphia.mapPackage("com.se.db");
		morphia.mapPackage("com.se.data");

		datastore = morphia.createDatastore(mongoClient, DATABASE_NAME);
		datastore.ensureIndexes();
	}

	public void insert(Collection<Object> objects) {
		datastore.save(objects);
	}

	public <T> T searchById(Class<T> cls, Object term) {
		return datastore.get(cls, term);
	}

	public InvertedIndex searchInvertedIndex(String term) {
		return searchById(InvertedIndex.class, term);
	}

	public Document searchDocument(Integer docID) {
		return searchById(Document.class, docID);
	}

	public void insert(Object object) {
		try {
			datastore.save(object);
		} catch (BsonSerializationException exception) {
			System.err.print("Error while inserting to MongoDB");
			System.err.println(exception);
		}
	}

	public <T> List<T> search(Class<T> tClass, String key, Object value) {
		Query<T> query = datastore.createQuery(tClass);
		query.field(key).equals(value);
		return query.asList();
	}

	public static DatabaseUtil create() {
		if (databaseUtil == null) {
			databaseUtil = new DatabaseUtil();
		}
		return databaseUtil;
	}

	public Double getPagerank(Integer docId) {
		PageRankData result = searchById(PageRankData.class, docId);
		if (result == null) {
			return 0.0;
		}
		return result.getScore();
	}

	public static void close() {
		if (databaseUtil == null) {
			return;
		}
		databaseUtil.mongoClient.close();
		databaseUtil = null;
		return;
	}

	public AnchorTextToken searchAnchorText(String term) {
		return searchById(AnchorTextToken.class, term);
	}

	public Map<String, InvertedIndex> getInvertedIndexRows(String query) {
		Map<String, InvertedIndex> tokenToEntry = new HashMap<String, InvertedIndex>();
		for (String term : WordsTokenizer.tokenizeWithStemmingFilterStop(query
				.toLowerCase())) {
			InvertedIndex invertedIndex = databaseUtil
					.searchInvertedIndex(term);
			tokenToEntry.put(term, invertedIndex);
		}
		return tokenToEntry;
	}
}
