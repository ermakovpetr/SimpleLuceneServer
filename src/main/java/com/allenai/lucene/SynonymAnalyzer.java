package com.allenai.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.util.CharArraySet;

public class SynonymAnalyzer extends Analyzer {

  private static final String PATH_SYNONYM_INDEX = "wn_s.pl";
  private TokenStreamComponents tokenStreamComponents = null;

  public SynonymAnalyzer() {
    super(PER_FIELD_REUSE_STRATEGY);
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    if (tokenStreamComponents == null) {
      Tokenizer source = new ClassicTokenizer();
      SynonymMap mySynonymMap = null;

      try {
        mySynonymMap = buildSynonym();
      } catch (IOException | ParseException e) {
        e.printStackTrace();
      }

      TokenStream filter = new LowerCaseFilter(source);

      filter = new SynonymFilter(filter, mySynonymMap, false);

      filter = new StandardFilter(filter);

      filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
      tokenStreamComponents = new TokenStreamComponents(source, filter);
    }
    return tokenStreamComponents;
  }

  protected boolean reset(final Reader reader) throws IOException {
    tokenStreamComponents.getTokenizer().close();
    tokenStreamComponents.getTokenStream().close();
    return true;
  }
  
  private SynonymMap buildSynonym() throws IOException, ParseException
  {
    File file = new File(PATH_SYNONYM_INDEX);
    System.out.println("File: " + file.isFile() + " Path: " + file.getAbsolutePath());
    InputStream stream = new FileInputStream(file);
    Reader rulesReader = new InputStreamReader(stream);

    SynonymMap.Builder parser = new WordnetSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
    ((WordnetSynonymParser) parser).parse(rulesReader);

    return parser.build();
  }
}