package com.google.code.tvrenamer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.code.tvrenamer.controller.TVRenamer;
import com.google.code.tvrenamer.model.ParsedFileName;

public class TVRenamerTest {

  private TVRenamer tvrenamer;

  @Before
  public void createTVRenamer() {
    tvrenamer = new TVRenamer();
  }

  public void assertParse(List<String> filenames, List<ParsedFileName> expected) {
    List<ParsedFileName> actual = tvrenamer.parseFiles(filenames);
    Iterator<ParsedFileName> actualI = actual.iterator();
    Iterator<ParsedFileName> expectedI = expected.iterator();
    while (actualI.hasNext() && expectedI.hasNext()) {
      assertEquals(expectedI.next(), actualI.next());
    }
    assertEquals(expectedI.hasNext(), actualI.hasNext());
  }

  @Test
  public void testParseFileName() {
    assertParse(Arrays.asList("warehouse.13.s1e01.720p.hdtv.x264-dimension.mkv","warehouse.13.s1e02.720p.hdtv.x264-dimension.mkv",
    		"Caprica [1x02] Rebirth.avi", "Caprica [1x03] The Reins of a Waterfall.avi", "Caprica [1x04] Gravedancing (fake part 2).avi"),
        Arrays.asList(new ParsedFileName("warehouse.13.s", 1, 1), new ParsedFileName("warehouse.13.s", 1, 2),
        new ParsedFileName("Caprica [", 1, 2), new ParsedFileName("Caprica [", 1, 3), new ParsedFileName("Caprica [", 1, 4))
        );
  }

}
