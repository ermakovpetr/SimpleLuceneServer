package com.allenai.lucene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ConsoleSearcher {
  public static final String INDEX_DIRECTORY = "/_data/index";

  private IndexReader indexReader;
  private IndexSearcher searcher;
  private Analyzer analyzer = null;

  public void openIndex(String path) throws IOException {
    Directory directory = FSDirectory.open(Paths.get(path + INDEX_DIRECTORY));
    indexReader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(indexReader);
  }

  public IndexScores getScore(
      String queryString,
      Integer nResult,
      boolean returnDocs,
      String analyzerType
  ) throws ParseException, IOException {

    if (analyzer == null) {
      switch (analyzerType) {
        case "with_synonyms":
          analyzer = new SynonymAnalyzer();
          break;
        case "standard":
          analyzer = new StandardAnalyzer();
          break;
      }
    }

    Query q = new QueryParser("text", analyzer).parse(queryString);
    TopDocs docs = searcher.search(q, nResult);
    ScoreDoc[] hits = docs.scoreDocs;
    IndexScores indexScores = new IndexScores();

    for (ScoreDoc hit : hits) {
      indexScores.addScoresList(hit.score);
      indexScores.addDocIds(hit.doc);
      if (returnDocs) {
        Document doc = searcher.doc(hit.doc);
        indexScores.addDocList(doc.get("text"));
      }
    }
    return indexScores;
  }

  public static void main(String[] args) throws IOException, ParseException {
    System.out.println(Arrays.toString(args));
    String filepath = args[0];
    Integer nResult = Integer.valueOf(args[1]);
    String pathToCorpus = args[2];
    String returnDocs = args[3];
    String analyzerType = args[4];

    ConsoleSearcher searcher = new ConsoleSearcher();
    searcher.openIndex(pathToCorpus);

    try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
      int count = 0;
      for (String line; (line = br.readLine()) != null; ) {
        if (count % 100 == 0) {
          System.out.println(count);
        }
        count++;
        IndexScores resp = searcher.getScore(line, nResult, returnDocs.equals("yes"), analyzerType);
        System.out.println(resp);
      }
    }
  }
}
