package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import javax.sound.sampled.*;

import org.apache.commons.io.FilenameUtils;

import picocli.CommandLine;

/**
 * Main Klasse für das KC Tape Tool. Hier befindet sich die zentrale Programmlogik.
 *
 * @author Holger Jödicke
 */
public class KcTapeTool
{
  /** Returncode für alles in Ordnung. */
  public static final Integer RC_OK = CommandLine.ExitCode.OK;

  private KcTapeToolCommand konfig;
  private PrintStream out = System.out;

  private Modus inputModus;
  private Modus outputModus;
  private AudioWriter audioWriter = new AudioWriter();
  private AudioReader audioReader = new AudioReader();
  private DateiReader dateiReader = new DateiReader();

  /**
   * Erzeugt ein neues KC Tabep Tool für die übergebene Konfiguration.
   */
  public KcTapeTool(KcTapeToolCommand konfig)
  {
    this.konfig = Objects.requireNonNull(konfig);
  }

  /**
   * Typ der zu lesenden oder zu schreibenden Daten.
   * <pre>
   * Lese Audio (Soundkarte oder WAV) - KcDatei - Schreibe Audio (Soundkarte oder WAV)
   * Lese Audio (Soundkarte oder WAV) - KcDatei - Schreibe Datendatei
   * Lese Datendatei - KcDatei - Schreibe Audio (Soundkarte oder WAV)
   * Lese Datendatei - KcDatei - Schreibe Datendatei
   * </pre>
   */
  enum Modus
  {
    /** Lesen oder schreiben von / in eine Wave Datei. */
    AUDIO_FILE,
    /** Lesen oder schreiben von / in einen Input- oder Outputmixer. */
    AUDIO_MIXER,
    /** Lesen oder schreiben von / in eine Daten Datei. */
    DATA_FILE
  }

  /**
   * Hier beginnt die eigentliche Programm Logik.
   *
   * @return der Programm Returncode
   */
  public Integer execute()
  {
    if( konfig.isList() )
    {
      listInputGeraete();
      listOutputGeraete();
      return CommandLine.ExitCode.OK;
    }
    if( !checkCommandLineParameter() )
    {
      return CommandLine.ExitCode.USAGE;
    }
    inputModus = getInputModus(getInputFile(), audioReader.getEingabeMixerName(konfig.getSource()));
    outputModus = getOutputModus();
    try
    {
      List<KcDatei> kcDatei;
      kcDatei = leseInput();
      schreibeOutput(kcDatei);
    }
    catch( TimeoutException e )
    {
      out.format("Wartezeit von %d Sekunden ist abgelaufen, beende Programm ...%n", konfig.getTimeout());
    }
    return CommandLine.ExitCode.OK;
  }

  // einigen Setter Methoden zum einfacheren Testen

  void setOut(PrintStream out)
  {
    this.out = out;
  }

  void setAudioWriter(AudioWriter audioWriter)
  {
    this.audioWriter = audioWriter;
  }

  void setAudioReader(AudioReader audioReader)
  {
    this.audioReader = audioReader;
  }

  void setDateiReader(DateiReader dateiReader)
  {
    this.dateiReader = dateiReader;
  }

  /**
   * Liefert <code>true</code> wenn alles OK ist, ansonsten <code>false</code>.
   */
  private boolean checkCommandLineParameter()
  {
    if( konfig.getSource() == null )
    {
      out.println("Es wurde keine Quelle zum Lesen von Daten angegeben: -s oder --source");
      out.println("Verwende 'KcTapeTool --help' für mehr Informationen");
      return false;
    }
    if( getInputFile() == null && audioReader.getEingabeMixerName(konfig.getSource()) == null )
    {
      out.format("Die angegebene Quelle [%s] ist weder eine lesbare Datei noch ein Soundeingang%n", konfig.getSource());
      return false;
    }
    if( konfig.getDestination() != null && getDestinationFile() == null && getAusgabeMixerName() == null )
    {
      out.format("Das angegebene Ziel [%s] ist weder eine Datei noch ein Soundausgang%n", konfig.getDestination());
      return false;
    }
    return true;
  }

  private List<KcDatei> leseInput() throws TimeoutException
  {
    switch( inputModus )
    {
      case DATA_FILE :
      {
        return leseDatenDatei(getInputFile());
      }
      case AUDIO_FILE :
      {
        return audioReader.leseWavDatei(getInputFile());
      }
      case AUDIO_MIXER :
      {
        KcDatei kdDatei = audioReader.leseMixerDaten(audioReader.getEingabeMixerName(konfig.getSource()), konfig.getTimeout());
        return Collections.singletonList(kdDatei);
      }
      default :
        throw new IllegalArgumentException("Unexpected value: " + inputModus);
    }
  }

  private void schreibeOutput(List<KcDatei> kcDatei)
  {
    switch( outputModus )
    {
      case DATA_FILE :
      {
        schreibeDatenDatei(kcDatei);
        break;
      }
      case AUDIO_FILE :
      {
        schreibeAudioDatei(kcDatei);
        break;
      }
      case AUDIO_MIXER :
      {
        schreibeAudioMixer(kcDatei);
        break;
      }
      default :
        throw new IllegalArgumentException("Unexpected value: " + inputModus);
    }
  }

  @SuppressWarnings("resource")
  private void schreibeAudioMixer(List<KcDatei> kcDatei)
  {
    Mixer mixer = audioWriter.getAusgabeMixer(getAusgabeMixerName());
    try( KcAudioInputStream kcAudioInputStream = new KcAudioInputStream(kcDatei) )
    {
      AudioFormat audioFormat = kcAudioInputStream.getAudioFormat();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
      try( SourceDataLine sourceLine = (SourceDataLine)mixer.getLine(info) )
      {
        sourceLine.open(audioFormat);
        sourceLine.start();
        audioWriter.copy(kcAudioInputStream, sourceLine);
        sourceLine.drain();
      }
      catch( LineUnavailableException e )
      {
        throw new IOException("Fehler beim Öffnen der Ausgabe", e);
      }
    }
    catch( IOException e )
    {
      throw new UncheckedIOException(e);
    }
  }

  private void listInputGeraete()
  {
    out.println("Verfügbare Sound Eingabe Geräte (* default):");
    @SuppressWarnings("resource")
    Mixer defaultMixer = audioReader.getDefaultInputMixer();
    for( Mixer mixer : audioReader.getAlleEingabeMixer() )
    {
      String prefix = Objects.equals(defaultMixer, mixer) ? "  (*) " : "      ";
      out.println(prefix + mixer.getMixerInfo().getName());
    }
  }

  private void listOutputGeraete()
  {
    out.println("Verfügbare Sound Ausgabe Geräte (* default):");
    @SuppressWarnings("resource")
    Mixer defaultMixer = audioWriter.getDefaultAusgabeMixer();
    for( Mixer mixer : audioWriter.getAlleAusgabeMixer() )
    {
      String prefix = Objects.equals(defaultMixer, mixer) ? "  (*) " : "      ";
      out.println(prefix + mixer.getMixerInfo().getName());
    }
  }

  private List<KcDatei> leseDatenDatei(Path inputFile)
  {
    List<KcDatei> kcDatei = dateiReader.load(inputFile);
    out.format("Datei eingelesen: %s%n", inputFile.toAbsolutePath());
    return kcDatei;
  }

  private void schreibeAudioDatei(List<KcDatei> kcDatei)
  {
    Path destination = getDestinationFile();
    audioWriter.schreibeAudioDatei(destination, kcDatei);
    out.format("Datei geschrieben: %s%n", destination);
  }

  private void schreibeDatenDatei(List<KcDatei> kcDateien)
  {
    int index = 1;
    for( KcDatei kcDatei : kcDateien )
    {
      try
      {
        Path path = getDestinationFile();
        if(path == null)
        {
          path = Paths.get(kcDatei.getFullDateiName());
        }
        else
        {
          if(kcDateien.size() > 1)
          {
            path = addCount(path, index++);
          }
        }
        if(konfig.getDirectory() != null && !path.isAbsolute())
        {
          path = konfig.getDirectory().resolve(path);
        }
        schreibeDatei(path, kcDatei);
        out.format("Datei geschrieben: %s%n", path);
      }
      catch( IOException e )
      {
        throw new UncheckedIOException("Fehler beim Scheiben der Daten nach [" + konfig.getDestination() + "].", e);
      }
    }
  }

  /**
   * Fügt einen Index / Zähler in den Dateinamen vor der Endung ein.
   */
  private Path addCount(Path path, int index)
  {
    String fileName = path.getFileName().toString();
    String baseName = FilenameUtils.getBaseName(fileName);
    String extension = FilenameUtils.getExtension(fileName);
    return path.getParent().resolve(baseName + "-" + index + "." + extension);
  }

  private void schreibeDatei(Path path, KcDatei kcDatei) throws IOException
  {
    PcDateiKonverter konverter = getKonverter(kcDatei);
    try( OutputStream os = Files.newOutputStream(path) )
    {
      konverter.write(os, kcDatei);
    }
  }

  private PcDateiKonverter getKonverter(KcDatei kcDatei)
  {
    return kcDatei.isBasicDateiType() ? new BasicConverter() : new CaosKonverter();
  }

  /**
   * @param file kann <code>null</code> sein
   */
  private boolean isWaveFileExtension(Path file)
  {
    if( file == null )
    {
      return false;
    }
    String fileName = file.getFileName().toString();
    String fileExtension = FilenameUtils.getExtension(fileName);
    return "wav".equalsIgnoreCase(fileExtension);
  }

  /**
   * @param file kann <code>null</code> sein
   */
  private boolean isWaveFile(Path file)
  {
    if( file == null )
    {
      return false;
    }
    try( AudioInputStream as = audioReader.createAudioStream(file) )
    {
      // testweise den Stream erzeugen aber nicht lesen, um zu prüfen, dass es wirklich eine WAV Datei ist
    }
    catch (IOException e)
    {
      return false;
    }
    return true;
  }

  private Modus getInputModus(Path inputFile, String inputMixerName)
  {
    if( inputMixerName == null )
    {
      if( isWaveFile(inputFile) )
      {
        return Modus.AUDIO_FILE;
      }
      return Modus.DATA_FILE;
    }
    return Modus.AUDIO_MIXER;
  }

  private Modus getOutputModus()
  {
    String destination = konfig.getDestination();
    if( destination == null )
    {
      switch( inputModus )
      {
        case AUDIO_FILE :
          return Modus.DATA_FILE;
        case AUDIO_MIXER :
          return Modus.DATA_FILE;
        case DATA_FILE :
          return Modus.AUDIO_MIXER;
        default :
          throw new IllegalArgumentException("Unexpected value: " + inputModus);
      }
    }
    String ausgabeMixerName = getAusgabeMixerName();
    if( ausgabeMixerName != null )
    {
      return Modus.AUDIO_MIXER;
    }
    Path destinationFile = getDestinationFile();
    if( isWaveFileExtension(destinationFile) )
    {
      return Modus.AUDIO_FILE;
    }
    return Modus.DATA_FILE;
  }

  /**
   * Liefert einen {@link Path} zur einzulesenden Datei.
   *
   * @return den {@link Path} zur einzulesenden Datei oder <code>null</code> wenn es keine Datei gibt.
   */
  private Path getInputFile()
  {
    if( konfig.getSource() == null )
    {
      return null;
    }
    try
    {
      Path source = Paths.get(konfig.getSource());
      Path inputFilePath = konfig.appendToDirectory(source);
      if(Files.isReadable(inputFilePath))
      {
        return inputFilePath;
      }
      return null;
    }
    catch( InvalidPathException e )
    {
      return null;
    }
  }

  private Path getDestinationFile()
  {
    if( konfig.getDestination() == null )
    {
      return null;
    }
    try
    {
      Path destination = Paths.get(konfig.getDestination());
      return konfig.appendToDirectory(destination);
    }
    catch( InvalidPathException e )
    {
      return null;
    }
  }

  private String getAusgabeMixerName()
  {
    String destination = konfig.getDestination();
    if( destination == null )
    {
      return null;
    }
    return audioWriter.getAusgabeMixerName(destination);
  }

  @Override
  public String toString()
  {
    return "KcTapeTool [inputModus=" + inputModus + ", outputModus=" + outputModus + "]";
  }

}
