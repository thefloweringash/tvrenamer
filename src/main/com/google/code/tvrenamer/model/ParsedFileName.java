package com.google.code.tvrenamer.model;

public class ParsedFileName {
  public final String series;
  public final int season;
  public final int episode;

  public ParsedFileName(String series, int season, int episode) {
    this.series = series;
    this.season = season;
    this.episode = episode;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ParsedFileName)) {
      return false;
    }

    ParsedFileName parsedOther = (ParsedFileName) other;
    return parsedOther.season == season && parsedOther.episode == episode &&
      (parsedOther.series == series || parsedOther.series.equals(series));
  }

  @Override
  public String toString() {
    return "[" + series + ", S:" + season + ", E:" + episode + "]";
  }
}
