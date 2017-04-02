package com.se.index;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.se.algorithm.TermSourceTarget;
import com.se.data.AnchorPosting;
import com.se.data.AnchorTextToken;
import com.se.db.DatabaseUtil;
import com.se.file.FileHandler;

public class AnchorTextProcessingMR {
	private static final String PATH = "path";
	private static final String BOOK_KEEPING_FILE = "bookkeeping";
	private static DatabaseUtil db;
	private static Map<String, Integer> urlToDocId;
	private static Gson gson = new Gson();
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnchorTextProcessingMR.class);

	public static class AnchorTextMapper extends
			Mapper<Object, Text, IntWritable, Text> {

		@Override
		public void map(Object key, Text value, Context context)
				throws InterruptedException, IOException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			if (itr.countTokens() < 2) {
				return;
			}
			String filePath = itr.nextToken();
			String url = itr.nextToken();

			int sourceDocId = getDocumentId(filePath);
			Configuration conf = context.getConfiguration();
			String path = conf.get(PATH);
			File file = new File(path + filePath);
			Document document = null;
			try {
				document = Jsoup.parse(file, "UTF-8", url);
				for (Element element : document.select("a[href]")) {
					String outgoingUrl = element.absUrl("href");
					if (outgoingUrl == null || outgoingUrl.isEmpty()) {
						continue;
					}
					outgoingUrl = trim(outgoingUrl);
					Integer targetDocId = urlToDocId.get(outgoingUrl);
					if (targetDocId == null) {
						continue;
					}
					List<String> tokens = WordsTokenizer
							.tokenizeWithStemmingFilterStop(element.text());
					for (String token : tokens) {
						TermSourceTarget termSourceTarget = new TermSourceTarget();
						termSourceTarget.setSourceDocId(sourceDocId);
						termSourceTarget.setTargetDocId(targetDocId);
						termSourceTarget.setToken(token);
						context.write(new IntWritable(targetDocId), new Text(
								gson.toJson(termSourceTarget)));
					}
				}
			} catch (IllegalArgumentException e) {
				LOGGER.error(e.getMessage());
			}

		}

		private int getDocumentId(String filePath) {
			String[] parts = filePath.split("/");
			return 500 * Integer.valueOf(parts[0]) + Integer.valueOf(parts[1]);
		}

		private String trim(String outgoingUrl) {
			String protocolRemovedUrl = outgoingUrl.replaceFirst(
					"^(http://|https://)", "");
			if (protocolRemovedUrl.endsWith("/")) {
				return outgoingUrl.substring(0, outgoingUrl.length() - 1);
			}
			return protocolRemovedUrl;
		}
	}

	public static class AnchorTextCombiner extends
			Reducer<IntWritable, Text, Text, Text> {

		@Override
		public void reduce(IntWritable targetDocId, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			Map<String, AnchorPosting> map = new HashMap<String, AnchorPosting>();
			for (Text value : values) {
				TermSourceTarget termSourceTarget = gson.fromJson(
						value.toString(), TermSourceTarget.class);
				String term = termSourceTarget.getToken();
				AnchorPosting anchorPosting;
				if (map.containsKey(term)) {
					anchorPosting = new AnchorPosting();
				} else {
					anchorPosting = new AnchorPosting();
					anchorPosting.setDocID(targetDocId.get());
					map.put(term, anchorPosting);
				}
				anchorPosting.addSourceDocId(termSourceTarget.getSourceDocId());
			}

			for (Entry<String, AnchorPosting> entry : map.entrySet()) {
				context.write(new Text(entry.getKey()),
						new Text(gson.toJson(entry.getValue())));
			}
		}
	}

	public static class DummayMapper extends
			Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable key, Text value, Context context)
				throws InterruptedException, IOException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			if (itr.countTokens() < 2) {
				return;
			}
			String token = itr.nextToken();
			String json = itr.nextToken();
			context.write(new Text(token), new Text(json));
		}
	}

	public static class AnchorTextReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text term, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			AnchorTextToken anchorTextToken = new AnchorTextToken();
			for (Text targetDocumentId : values) {
				AnchorPosting anchorPosting = gson.fromJson(
						targetDocumentId.toString(), AnchorPosting.class);
				anchorTextToken.addTargetDocId(anchorPosting);
			}

			anchorTextToken.sortTargetDocIds();
			anchorTextToken.setTerm(term.toString());
			db.insert(anchorTextToken);
		}
	}

	public static void run(Map<String, Integer> urlToDocIdMap)
			throws IOException, ClassNotFoundException, InterruptedException {
		urlToDocId = urlToDocIdMap;
		db = DatabaseUtil.create();

		Configuration conf = new Configuration();

		String path = FileHandler.configFetch(PATH);
		String book = FileHandler.configFetch(BOOK_KEEPING_FILE);
		conf.set(PATH, path);
		Job job = Job.getInstance(conf, "Anchor Text Collection Creator");
		job.setJarByClass(PageRankAlgorithmMR.class);
		job.setMapperClass(AnchorTextMapper.class);
		job.setReducerClass(AnchorTextCombiner.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(path + book));
		FileOutputFormat.setOutputPath(job, new Path("target/temp/anchorMR"));
		job.waitForCompletion(true);

		Configuration conf2 = new Configuration();
		Job job2 = Job.getInstance(conf2, "Anchor Text Collection Creator");
		job2.setJarByClass(PageRankAlgorithmMR.class);
		job2.setMapperClass(DummayMapper.class);
		FileInputFormat.addInputPath(job2, new Path("target/temp/anchorMR"));
		job2.setReducerClass(AnchorTextReducer.class);
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputFormatClass(NullOutputFormat.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		job2.waitForCompletion(true);

		FileSystem fs = FileSystem.get(new Configuration());
		fs.delete(new Path("target/temp/anchorMR"), true);

		DatabaseUtil.close();
	}

}