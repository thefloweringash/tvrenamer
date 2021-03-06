package com.google.code.tvrenamer.model;

import java.util.HashMap;

public class Show {
  private final String id;
  private final String name;
  private final String url;

  private final HashMap<String, Season> seasons;

  public Show(String id, String name, String url) {
    this.id = id;
    this.name = name;
    this.url = url;

    seasons = new HashMap<String, Season>();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public void setSeason(String sNum, Season season) {
    seasons.put(sNum, season);
  }

  public Season getSeason(String sNum) {
    return seasons.get(sNum);
  }
}
