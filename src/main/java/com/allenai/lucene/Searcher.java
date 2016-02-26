package com.allenai.lucene;

import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SimpleMergedSegmentWarmer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.InfoStream;

@Singleton
public class Searcher {
  public static final String INDEX_DIRECTORY = "/_data/index";
  
  private IndexReader indexReader;
  private IndexSearcher searcher;
  private boolean closed = true;
  private Analyzer analyzer = null;
  
  public boolean indexIsOpen() {
    return searcher != null && !closed;
  }
  
  public void openIndex(String path, String similarity) throws IOException {
    Directory directory = FSDirectory.open(Paths.get(path + INDEX_DIRECTORY));
    indexReader = DirectoryReader.open(directory);
    warmReader(indexReader);

    Integer cores = Runtime.getRuntime().availableProcessors();
    searcher = new IndexSearcher(indexReader, Executors.newFixedThreadPool(cores));

    switch (similarity) {
      case "bm25":
        searcher.setSimilarity(new BM25Similarity());
        System.out.println(" # set BM25Similarity");
        // TODO добавить поддержку параметров
        break;
      case "classic":
        searcher.setSimilarity(new ClassicSimilarity());
        System.out.println(" # set ClassicSimilarity");
        break;
      case "lmd":
        searcher.setSimilarity(new LMDirichletSimilarity());
        System.out.println(" # set LMDirichletSimilarity");
        // TODO нужно изучить эту штуку
        break;
      case "default":
        System.out.println(" # set DefaultSimilarity");
        break;
      // TODO: DFRSimilarity, IBSimilarity, LMJelinekMercerSimilarity, MultiSimilarity
    }
    
    
    closed = false;;
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

  public void closeIndex() throws IOException {
    indexReader.close();
    closed = true;
  }

  public void warmReader(IndexReader reader) throws IOException {
    SimpleMergedSegmentWarmer warmer = new SimpleMergedSegmentWarmer(InfoStream.NO_OUTPUT);
    for (LeafReaderContext atomicReaderContext: reader.leaves()) {
      warmer.warm(atomicReaderContext.reader());
    }
  }
}
