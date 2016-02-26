package com.allenai.lucene;

import java.util.ArrayList;
import java.util.List;

public class IndexScores {
  private List<Float> scoresList;
  private List<String> docList;
  private List<Integer> docIds;

  public void addScoresList (Float f) {
    this.scoresList.add(f);
  }
  
  public void addDocList (String s) {
    this.docList.add(s);
  }
  
  public void addDocIds (Integer i) {
    this.docIds.add(i);
  }

  public IndexScores() {
    this.scoresList = new ArrayList<>();
    this.docIds = new ArrayList<>();
    this.docList = new ArrayList<>();
  }

  public IndexScores(List<Float> scoresList, List<String> docList, List<Integer> docIds) {
    this.scoresList = scoresList;
    this.docIds = docIds;
    this.docList = docList;
  }

  public List<Integer> getDocIds() {
    return docIds;
  }

  public void setDocIds(List<Integer> docIds) {
    this.docIds = docIds;
  }

  public List<Float> getScoresList() {
    return scoresList;
  }

  public void setScoresList(List<Float> scoresList) {
    this.scoresList = scoresList;
  }

  public List<String> getDocList() {
    return docList;
  }

  public void setDocList(List<String> docList) {
    this.docList = docList;
  }

  @Override
  public String toString() {
    return "IndexScores{" +
        "scoresList=" + scoresList +
        ", docList=" + docList +
        '}';
  }
}
