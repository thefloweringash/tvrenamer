package com.google.code.tvrenamer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.code.tvrenamer.model.ParsedFileName;
import com.google.code.tvrenamer.model.Season;
import com.google.code.tvrenamer.model.Show;
import com.google.code.tvrenamer.util.MapIterable;
import com.google.code.tvrenamer.util.RoundRobinIterable;
import com.google.code.tvrenamer.util.TakeIterable;

public class TVRenamer {
//  private static Logger logger = Logger.getLogger(TVRenamer.class);
  private Show show;

  public TVRenamer() {
  }

  public void setShow(Show show) {
    this.show = show;
  }

  public ArrayList<Show> downloadOptions(String showName) {
    return TVRageProvider.getShowOptions(showName);
  }

  public void downloadListing() {
    TVRageProvider.getShowListing(show);
  }


  interface Evaluator<T> {
    public int value(T o);
  }
  private static <T> int minimumIndexBy(List<T> xs, Evaluator<T> f) {
    int i = 0;
    int lowestIndex = 0;
    Iterator<T> iter = xs.iterator();

    int lowestScore;
    T lowest;

    lowest = iter.next();
    lowestIndex = i++;
    lowestScore = f.value(lowest);

    while (iter.hasNext()) {
      T candidate = iter.next();
      int candidateIndex = i++;
      int candidateScore = f.value(candidate);
      if (candidateScore < lowestScore) {
        lowest = candidate;
        lowestIndex = candidateIndex;
        lowestScore = candidateScore;
      }
    }
    return lowestIndex;
    // lowest is also available if the return type should have multiple values
  }

  public Map<String, List<String>> cluster(Iterable<String> filenames) {
    Map<String, List<String>> result = new HashMap<String, List<String>>();

    for (String f : filenames) {
      Matcher m = numberPattern.matcher(f);
      if (m.find()) {
        String prefix = f.substring(0, m.start());
        List<String> bucket;
        if (!result.containsKey(prefix)) {
          bucket = new ArrayList<String>();
          result.put(prefix, bucket);
        }
        else {
          bucket = result.get(prefix);
        }
        bucket.add(f);
      }
      else {
        throw new RuntimeException("Cannot find prefix of " + f);
      }

    }

    return result;
  }

  private final Pattern numberPattern = Pattern.compile("([0-9]+)");
  private List<ParsedFileName> parseClusteredFiles(Iterable<String> filenames) {
    List<List<Integer>> sequences = new ArrayList();
    List<List<String>> tokenLists = new ArrayList();

    for (String filename : filenames) {
      Matcher m = numberPattern.matcher(filename);

      int sequenceNo = 0;
      int tokenStart = 0;
      List<String> tokenList = new ArrayList<String>();
      tokenLists.add(tokenList);

      while (m.find()) {
        List<Integer> sequence;
        if (sequenceNo >= sequences.size()) {
          sequence = new ArrayList<Integer>();
          sequences.add(sequence);
        }
        else {
          sequence = sequences.get(sequenceNo);
        }
        sequenceNo++;

        if (m.start() != tokenStart) {
          tokenList.add(filename.substring(tokenStart, m.start()));
        }
        tokenStart = m.end();

        int n = Integer.parseInt(m.group());
        sequence.add(n);
      }
      if (tokenStart < filename.length()) {
        tokenList.add(filename.substring(tokenStart));
      }
    }

    // Dumb heuristic to find episode number
    List<Integer> episodeSequence;
    List<Integer> seasonSequence;
    int episodeIndex = minimumIndexBy(sequences, new Evaluator<List<Integer>>(){
      public int value(List<Integer> sequence) {
        int score = 0;
        Iterator<Integer> sequenceI = sequence.iterator();
        int last = sequenceI.next();
        while (sequenceI.hasNext()) {
          int expected = last + 1;
          int actual = sequenceI.next();
          score += expected - actual;
        }
        return score;
      }});

      episodeSequence = sequences.get(episodeIndex);
      seasonSequence = sequences.get(episodeIndex - 1);

    List<ParsedFileName> results = new ArrayList<ParsedFileName>();
    int i = 0;
    for (String filename : filenames) {
      StringBuilder sb = new StringBuilder();
      Iterable<String> sequenceParts = new MapIterable<List<Integer>, String>(new TakeIterable(sequences, episodeIndex - 1), new MapIterable.Map<List<Integer>, String>() {
        public String map(List<Integer> o) {
          return String.valueOf(o.get(0));
        }
      });

      for (String p : new RoundRobinIterable<String>(Arrays.asList(tokenLists.get(i), sequenceParts))) {
        sb.append(p);
      }
      results.add(new ParsedFileName(sb.toString(), seasonSequence.get(i), episodeSequence.get(i)));
      i++;
    }

    return results;
  }

  public List<ParsedFileName> parseFiles(Iterable<String> filenames) {
    Map<String, List<String>> buckets = cluster(filenames);
    Map<String, ParsedFileName> resultMap = new HashMap<String, ParsedFileName>();
    List<ParsedFileName> results = new ArrayList<ParsedFileName>();
    for (List<String> bucket : buckets.values()) {
      Iterator<String> inputI = bucket.iterator();
      Iterator<ParsedFileName> resultsI = parseClusteredFiles(bucket).iterator();

      while (inputI.hasNext() && resultsI.hasNext()) {
        resultMap.put(inputI.next(), resultsI.next());
      }
    }
    for (String f : filenames) {
      results.add(resultMap.get(f));
    }
    return results;
  }


  public String formatFileName(String fileName, ParsedFileName parsed, String format) {
    String showTitle = replacePunctuation(show.getName().toLowerCase());
    String cleanedFileName = replacePunctuation(fileName.toLowerCase());

    if (!cleanedFileName.startsWith(showTitle)) {
//      logger.error("Show's name (" + showName + ") does not match file name: "
//          + fileName);
      return fileName;
    }


    Season s = show.getSeason(String.valueOf(parsed.season));
    if (s == null) {
//      logger.error("season not found: " + seasonNum);
      return fileName;
    }
    String title = s.getTitle(String.valueOf(parsed.episode));
    if (title == null) {
//      logger.error("Title not found for episode: " + episodeNum);
      return fileName;
    }
    title = sanitiseTitle(title);
    String extension = getExtension(fileName);

    format = format.replace("%S", show.getName());
    format = format.replace("%s", String.valueOf(parsed.season));
    format = format.replace("%e", String.valueOf(parsed.episode));
    format = format.replace("%t", title);

    return format + "." + extension;
  }

  public String getShowName(File file) {
    String showName = file.getParentFile().getName();

    // If the showname is 'Season x' the go up another directory
    if (showName.toLowerCase().startsWith("season")) {
      showName = file.getParentFile().getParentFile().getName();
    }
    return showName;
  }

  private String getExtension(String filename) {
    int dot = filename.lastIndexOf('.');
    return filename.substring(dot + 1);
  }

  private String sanitiseTitle(String title) {
    // need to add more mappings, such as ':'
    title = title.replace(":", " -");
    title = title.replace('/', '-');
    title = title.replace('\\', '-');
    title = title.replace("?", "");
    title = title.replace("`", "'");
    return title;
  }

  private String replacePunctuation(String s) {
    s = s.replaceAll("\\.", " ");
    s = s.replaceAll(",", " ");
    return s;
  }
}
