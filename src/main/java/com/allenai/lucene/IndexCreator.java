package com.allenai.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexCreator {
  public static final String INDEX_DIRECTORY = "/_data/index";

  public static String createIndex(
      String pathToCorpus,
      String similarityType,
      boolean byLine,
      String analyzerType,
      Integer minLengthText
  ) throws IOException, InterruptedException, ParseException {
    Analyzer analyzer = null;
    
    switch (analyzerType) {
      case "with_synonyms":
        analyzer = new SynonymAnalyzer();
        break;
      case "standard":
        analyzer = new StandardAnalyzer();
        break;
    }
     
    
    Directory directory = FSDirectory.open(Paths.get(pathToCorpus + INDEX_DIRECTORY));
    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
    indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    // Similarity
    switch (similarityType) {
      case "bm25": 
        indexWriterConfig.setSimilarity(new BM25Similarity());
        System.out.println(" # set BM25Similarity");
        // TODO добавить поддержку параметров
        break;
      case "classic":
        indexWriterConfig.setSimilarity(new ClassicSimilarity());
        System.out.println(" # set ClassicSimilarity");
        break;
      case "lmd":
        indexWriterConfig.setSimilarity(new LMDirichletSimilarity());
        System.out.println(" # set LMDirichletSimilarity");
        // TODO нужно изучить эту штуку
        break;
      case "default":
        System.out.println(" # set DefaultSimilarity");
        break;
      // TODO: DFRSimilarity, IBSimilarity, LMJelinekMercerSimilarity, MultiSimilarity
    }
    
    indexWriterConfig.setRAMBufferSizeMB(1024.0 * 60);
    indexWriterConfig.setMaxBufferedDocs(IndexWriterConfig.DISABLE_AUTO_FLUSH);
    
    final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
    
    File folder = new File(pathToCorpus);
    File[] listOfFiles = folder.listFiles();
    if ((listOfFiles == null) || (listOfFiles.length == 0)) {
      return "Folder with text is empty";
    }

    Integer cores = Runtime.getRuntime().availableProcessors();
    System.out.println(" # Cores:" + cores);
    ExecutorService executorService = Executors.newFixedThreadPool(cores);
    System.out.println(" # Number of files: " + listOfFiles.length);
    for (final File file : listOfFiles) 
    {
      if (file.isFile()) {
        System.out.println(file.getAbsolutePath());
        if (byLine) {
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int count = 0;
            for (String line; (line = br.readLine()) != null; ) {
              if (count % 1000000 == 0) {
                System.out.println(count);
              }
              count++;
              
              if (line.length() >= minLengthText) {
                addDoc(indexWriter, executorService, line);
              }
            }
          }
        } else {
          try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int count = 0;
            String text = " ";
            
            for (String line; (line = br.readLine()) != null; ) {
              if (count % 1000000 == 0) {
                System.out.println(count);
              }
              
              if (line.startsWith("#####")) {
                if (text.length() >= minLengthText) {
                  addDoc(indexWriter, executorService, text);
                }
                text = "";
                count++;
              } else {
                text += " " + line;
              }
            }
          }
        }
      }
    }

    System.out.println(" # All files done, wait when index will be close");
    executorService.shutdown();
    executorService.awaitTermination(36000, TimeUnit.SECONDS);
    indexWriter.close();
    System.out.println(" # index Close");
    
    return "Ok";
  }

  private static void addDoc(final IndexWriter indexWriter, ExecutorService executorService, final String text) {
    executorService.submit(new Runnable() {
      public void run() {
        try {
          Document doc = new Document();
          doc.add(new TextField("text", text, Field.Store.YES));
          indexWriter.addDocument(doc);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
