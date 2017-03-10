package com.se.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import com.google.gson.Gson;
import com.se.data.Documents;
import com.se.data.Posting;
import com.se.data.InvertedIndex;
import com.se.data.Posting;
import com.se.data.Utility;
import com.se.db.DatabaseUtil;
import com.se.file.FileHandler;

public class IndexerMR {
	private static final String PATH = "path";
	private static final String BOOK_KEEPING_FILE = "bookkeeping";
	private static Gson gson = new Gson();
	private static DatabaseUtil db = new DatabaseUtil();
	private static Utility utility = new Utility();
	private static Long N = 40000l;
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			String path = conf.get(PATH);
			StringTokenizer itr = new StringTokenizer(value.toString());
			if (itr.countTokens() < 2) {
				return;
			}
			String filePath = itr.nextToken();
			String url = itr.nextToken();
			String[] parts = filePath.split("/");
			int docID = 500 * Integer.valueOf(parts[0])
					+ Integer.valueOf(parts[1]);
			File file = new File(path + filePath);

			Documents docEntry = new Documents(docID, filePath, url);
			db.insert(docEntry);

			Map<String, Posting> postingsMap = Tokenizer.tokenize(file, docID,
					url);

			for (Entry<String, Posting> entry : postingsMap.entrySet()) {
				context.write(new Text(entry.getKey()),
						new Text(gson.toJson(entry.getValue())));
			}
			utility.incrementValue();
		}
	}

	public static class PostingsReducer extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text term, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			InvertedIndex wordEntry = new InvertedIndex();
			wordEntry.setTerm(term.toString());
			List<Posting> postings = new ArrayList<>();
			for (Text value : values) {
				postings.add(gson.fromJson(value.toString(), Posting.class));
			}
			Collections.sort(postings);
			wordEntry.setPostings(postings);
			wordEntry.setDocFrq(postings.size());
			
			for(Posting posting: postings){
				Double tfidf = (1 + Math.log10(posting.getTermFreq())) * Math.log10(N.doubleValue()/postings.size());
				posting.setTfidf(tfidf);
			}
			
			db.insert(wordEntry);
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		String path = FileHandler.configFetch(PATH);
		String book = FileHandler.configFetch(BOOK_KEEPING_FILE);
		conf.set(PATH, path);
		Job job = Job.getInstance(conf, "Postings Creator");
		job.setJarByClass(IndexerMR.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(PostingsReducer.class);
		job.setOutputFormatClass(NullOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(path + book));
		job.waitForCompletion(true);
		db.insert(utility);
	}

}

// for (Element element : doc.getAllElements()) {
// if (!set.contains(element.tagName())) {
// tokenize(docID, element.ownText(), element.tagName(),
// postingMap);
// }
// }
// return postingMap;
