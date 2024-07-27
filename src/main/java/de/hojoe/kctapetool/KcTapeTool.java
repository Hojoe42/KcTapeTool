package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
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

  /*
   * Lese Audio (Soundkarte oder WAV) - KcDatei - Schreibe (Soundkarte oder WAV)
   * Lese Audio (Soundkarte oder WAV) - KcDatei - Schreibe Datendatei
   * Lese Datendatei - KcDatei - Schreibe (Soundkarte oder WAV)
   * Lese Datendatei - KcDatei - Schreibe Datendatei
   */
  private Modus inputModus;
  private Modus outputModus;
  private AudioWriter audioWriter = new AudioWriter();
  private AudioReader audioReader = new AudioReader();

  /**
   * Erzeugt ein neues KC Tabep Tool für die übergebene Konfiguration.
   */
  public KcTapeTool(KcTapeToolCommand konfig)
  {
    this.konfig = Objects.requireNonNull(konfig);
  }

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
      KcDatei kcDatei;
      kcDatei = leseInput();
      schreibeOutput(kcDatei);
    }
    catch( TimeoutException e )
    {
      out.format("Wartezeit von %d Sekunden ist abgelaufen, beende Programm ...%n", konfig.getTimeout());
    }
    return CommandLine.ExitCode.OK;
  }

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
      out.printf("Die angegebene Quelle [%s] ist weder eine Datei noch ein Soundeingang%n", konfig.getSource());
      return false;
    }
    if( konfig.getDestination() != null && getDestinationFile() == null && getAusgabeMixerName() == null )
    {
      out.printf("Das angegebene Ziel [%s] ist weder eine Datei noch ein Soundausgang%n", konfig.getDestination());
      return false;
    }
    return true;
  }

  private KcDatei leseInput() throws TimeoutException
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
        return audioReader.leseMixerDaten(audioReader.getEingabeMixerName(konfig.getSource()), konfig.getTimeout());
      }
      default :
        throw new IllegalArgumentException("Unexpected value: " + inputModus);
    }
  }

  private void schreibeOutput(KcDatei kcDatei)
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
        Path destination = getDestinationFile();
        audioWriter.schreibeAudioDatei(destination, kcDatei);
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

  private void schreibeAudioMixer(KcDatei kcDatei)
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

  private KcDatei leseDatenDatei(Path inputFile)
  {
    return audioReader.load(inputFile);
  }

  private void schreibeDatenDatei(KcDatei kcDatei)
  {
    try
    {
      Path path = Paths.get(kcDatei.getFullDateiName());
      if(konfig.getDirectory() != null)
      {
        path = konfig.getDirectory().resolve(path);
      }
      schreibeDatei(path, kcDatei);
    }
    catch( IOException e )
    {
      throw new UncheckedIOException("Fehler beim Scheiben der Daten nach [" + konfig.getDestination() + "].", e);
    }
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
  private boolean isWaveFile(Path file)
  {
    if( file == null )
    {
      return false;
    }
    String fileName = file.getFileName().toString();
    String fileExtension = FilenameUtils.getExtension(fileName);
    if( fileExtension == null )
    {
      return false;
    }
    return "wav".equalsIgnoreCase(fileExtension);
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
    if( isWaveFile(destinationFile) )
    {
      return Modus.AUDIO_FILE;
    }
    return Modus.DATA_FILE;
  }

  private Path getInputFile()
  {
    if( konfig.getSource() == null )
    {
      return null;
    }
    try
    {
      Path source = Paths.get(konfig.getSource());
      return konfig.appendToDirectory(source);
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
