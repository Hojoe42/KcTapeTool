package de.hojoe.kctapetool;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.sound.sampled.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;

import de.hojoe.kctapetool.charset.KcCaosCharsetProvider;

@SuppressWarnings("resource")
class KcTapeToolTest
{

  private KcTapeToolCommand konfig;
  private PrintStream printStream;
  private ByteArrayOutputStream bos;
  @TempDir
  private Path tempDir;

  @BeforeEach
  void setup()
  {
    konfig = new KcTapeToolCommand();
    bos = new ByteArrayOutputStream();
    printStream = new PrintStream(bos, true, UTF_8);
  }

  @AfterEach
  void tearDown()
  {
    printStream.close();
  }

  /**
   * Liest eine WAV Datei und schreibt eine KCC Datei
   */
  @Test
  void testWaveFile2Kcc() throws Exception
  {
    konfig.setSource(createPath("/caos_e-anfang.wav").toString());
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);

    kcTapeTool.execute();

    // lädt die neue CAOS_E.KCC Datei aus dem Projekt Root
    byte[] caosE = readAndDelete(Paths.get("CAOS_E.KCC"));
    // einige wichtige Werte manuell prüfen
    assertThat(caosE.length, is(640));
    String name = new String(caosE, 0, 6, KcCaosCharsetProvider.CAOS_CHARSET);
    assertThat( name, is("CAOS_E"));
    assertThat( caosE[16], is((byte)0x02)); // Anzahl Parameter
    int start = caosE[17] + 256 * (0xff & caosE[18]);
    assertThat( start, is(0xE000));
    int ende = (0xff & caosE[19]) + 256 * (0xff & caosE[20]);
    assertThat( ende, is(0xE1FF));
    // lädt die originale CAOS_E.KCC Datei aus dem Test Resources Verzeichnis
    byte[] originalCaosE = Files.readAllBytes(createPath("/CAOS_E.KCC"));
    assertThat(caosE, is(originalCaosE));
  }

  @Test
  void testWaveFile2KccMitDefaultDirectory() throws Exception
  {
    Path testWave = tempDir.resolve("caos_e-test.wav");
    Files.copy(createPath("/caos_e-anfang.wav"), testWave);
    konfig.setSource("caos_e-test.wav");
    konfig.setDirectory(tempDir);
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);

    kcTapeTool.execute();

    Path outputFilePath = tempDir.resolve("caos_e-test.wav");
    assertThat(Files.exists(outputFilePath), is(true));
    byte[] originalCaosE = Files.readAllBytes(createPath("/CAOS_E.KCC"));
    byte[] tempFile = Files.readAllBytes(tempDir.resolve("CAOS_E.KCC"));
    assertThat(tempFile.length, is(originalCaosE.length));
    assertThat(tempFile, is(originalCaosE));
  }

  /**
   * Liest vom Mixer und schreibt eine KCC Datei
   */
  @Test
  void testWaveMixer2Kcc() throws Exception
  {
    konfig.setSource("Input");
    konfig.setDirectory(tempDir);
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);
    // Mockinitialisierung
    KcDatei kcDatei = new AudioReader().load(createPath("/CAOS_E.KCC"));
    AudioReader audioReaderMock = mock(AudioReader.class);
    when(audioReaderMock.getEingabeMixerName("Input")).thenReturn("InputMixer");
    when(audioReaderMock.leseMixerDaten(eq("InputMixer"), anyInt())).thenReturn(kcDatei);
    kcTapeTool.setAudioReader(audioReaderMock);

    kcTapeTool.execute();

    Path outputFilePath = tempDir.resolve("CAOS_E.KCC");
    assertThat(Files.exists(outputFilePath), is(true));
    byte[] originalCaosE = Files.readAllBytes(createPath("/CAOS_E.KCC"));
    byte[] tempFile = Files.readAllBytes(outputFilePath);
    assertThat(tempFile, is(originalCaosE));
  }

  @Test
  void testKcc2WaveFile() throws Exception
  {
    konfig.setSource(createPath("/CAOS_E.KCC").toString());
    konfig.setDestination("caos_test.wav");
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);

    kcTapeTool.execute();

    byte[] caosE = readAndDelete(Paths.get("caos_test.wav"));
    Path originalCaosE = createPath("/caos_e-anfang.wav");
    long erwarteteSize = Files.size(originalCaosE);
    assertThat(Long.valueOf(caosE.length), is(erwarteteSize));
  }

  /** Ausgabe auf dem default Mixer */
  @Test
  void testKcc2WaveFileMixer() throws Exception
  {
    konfig.setSource(createPath("/CAOS_E.KCC").toString());
    konfig.setDestination(null); // default Mixer
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);
    // Mocks
    AudioWriter audioWriterMock = mock(AudioWriter.class);
    Mixer mixerMock = mock(Mixer.class);
    SourceDataLine sdlMock = mock(SourceDataLine.class);
    // Mocks vorbereiten
    when(audioWriterMock.getAusgabeMixer(isNull())).thenReturn(mixerMock);
    when(mixerMock.getLine(ArgumentMatchers.any(DataLine.Info.class))).thenReturn(sdlMock);
    kcTapeTool.setAudioWriter(audioWriterMock);

    kcTapeTool.execute();

    verify(audioWriterMock).copy(any(KcAudioInputStream.class), eq(sdlMock));
  }

  /** Ausgabe auf dem "Kopfhörer" Mixer */
  @Test
  void testKcc2WaveFileMixer2() throws Exception
  {
    konfig.setSource(createPath("/CAOS_E.KCC").toString());
    konfig.setDestination("Kopfhörer");
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);
    // Mocks
    AudioWriter audioWriterMock = mock(AudioWriter.class);
    Mixer mixerMock = mock(Mixer.class);
    SourceDataLine sdlMock = mock(SourceDataLine.class);
    // Mocks vorbereiten
    when(audioWriterMock.getAusgabeMixerName(startsWith("Kopfhörer"))).thenReturn("Kopfhörer Mixer");
    when(audioWriterMock.getAusgabeMixer("Kopfhörer Mixer")).thenReturn(mixerMock);
    when(mixerMock.getLine(ArgumentMatchers.any(DataLine.Info.class))).thenReturn(sdlMock);
    kcTapeTool.setAudioWriter(audioWriterMock);

    kcTapeTool.execute();

    verify(audioWriterMock).copy(any(KcAudioInputStream.class), eq(sdlMock));
  }

  @Test
  void testLeereCommandoZeile()
  {
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);
    kcTapeTool.setOut(printStream);

    kcTapeTool.execute();

    String output = bos.toString(StandardCharsets.UTF_8);
    assertThat(output, is(notNullValue()));
    assertThat(output, matchesRegex("Es wurde keine Quelle zum Lesen von Daten angegeben: -s oder --source\\RVerwende 'KcTapeTool --help' für mehr Informationen\\R"));
  }

  @Test
  void testListCommand()
  {
    KcTapeTool kcTapeTool = new KcTapeTool(konfig);
    konfig.setList(true);
    kcTapeTool.setOut(printStream);

    kcTapeTool.execute();

    String string = bos.toString(StandardCharsets.UTF_8);
    assertThat(string, is(notNullValue()));
    assertThat(string, is(matchesRegex("(?s)Verfügbare Sound Eingabe Geräte \\(\\* default\\):.+Verfügbare Sound Ausgabe Geräte \\(\\* default\\):.+")));
  }

  /**
   * Erzeugt ein Path Objekt zu einer Datei im (Test-) Klassenpfad.
   */
  private Path createPath(String resourceName) throws URISyntaxException
  {
    URL url = getClass().getResource(resourceName);
    return Paths.get(url.toURI());
  }

  private byte[] readAndDelete(Path filePath) throws IOException
  {
    assertThat(Files.exists(filePath), is(true));
    byte[] caosE = Files.readAllBytes(filePath);
    Files.delete(filePath);
    return caosE;
  }

}
