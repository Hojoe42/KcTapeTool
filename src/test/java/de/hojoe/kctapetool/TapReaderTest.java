package de.hojoe.kctapetool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.api.Test;

class TapReaderTest
{

  @Test
  void testLoadTap1Datei()
  {
    byte[] tap = new byte[TapReader.TAP1_HEADER.length + 2 + 2 * 128];
    Arrays.fill(tap, (byte)42);
    System.arraycopy(TapReader.TAP1_HEADER, 0, tap, 0, TapReader.TAP1_HEADER.length);
    tap[16] = 1;
    tap[16 + 128 + 1] = (byte)-1; // 0xff
    TapReader tapReader = new TapReader(tap);
    List<KcDatei> kcDateien = tapReader.loadTap1Datei();
    assertThat(kcDateien, is(notNullValue()));
    assertThat(kcDateien.size(), is(1));
    KcDatei kcDatei = kcDateien.get(0);
    assertThat(kcDatei.getBloecke().size(), is(2));
    byte[] erwartet = createAndFill(128, (byte)42);
    assertThat(kcDatei.getBloecke().get(0).getDaten(), is(erwartet));
    assertThat(kcDatei.getBloecke().get(1).getDaten(), is(erwartet));
  }

  @Test
  void testLoadTap1Dateien()
  {
    byte[] tap = new byte[TapReader.TAP1_HEADER.length + 2 + 256 + TapReader.TAP1_HEADER.length + 3 + 3 * 128];
    Arrays.fill(tap, (byte)42);
    // Datei 1
    System.arraycopy(TapReader.TAP1_HEADER, 0, tap, 0, TapReader.TAP1_HEADER.length);
    int index = 16;
    tap[index] = 1; // 1. Block
    tap[index + 128 + 1] = (byte)-1; // 2. und letzter Block
    // Datei 2
    index = index + 2 + 2*128;
    System.arraycopy(TapReader.TAP1_HEADER, 0, tap, index, TapReader.TAP1_HEADER.length);
    index += 16;
    tap[index] = 1;
    tap[index + 128 + 1] = 2;
    tap[index + 256 + 2] = (byte)-1;
    TapReader tapReader = new TapReader(tap);
    List<KcDatei> kcDateien = tapReader.loadTap1Datei();
    assertThat(kcDateien, is(notNullValue()));
    assertThat(kcDateien.size(), is(2));
    KcDatei kcDatei = kcDateien.get(0);
    assertThat(kcDatei.getBloecke().size(), is(2));
    kcDatei = kcDateien.get(1);
    assertThat(kcDatei.getBloecke().size(), is(3));
  }

  @Test
  void testIsTap1DateiPositiv()
  {
    assertThat(TapReader.isTap1Datei(TapReader.TAP1_HEADER), is(true));
  }

  @Test
  void testIsTap1DateiPositivNegativ1()
  {
    assertThat(TapReader.isTap1Datei(new byte[0]), is(false));
  }

  @Test
  void testIsTap1DateiPositivNegativ2()
  {
    assertThat(TapReader.isTap1Datei("fas ist kein TAP Header".getBytes(StandardCharsets.ISO_8859_1)), is(false));
  }

  private byte[] createAndFill(int length, byte content)
  {
    byte[] array = new byte[length];
    Arrays.fill(array, content);
    return array;
  }

}
