package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import javax.sound.sampled.*;
import javax.sound.sampled.Line.Info;


/**
 * Code zum Einlesen von Sound Daten und extrahieren der aufmodulierten Daten.
 *
 * @author Holger Jödicke
 */
public class AudioReader
{
  /** {@link Info} Objekt für Aufnahme Geräte. */
  private static final Line.Info inputLineInfo = new Line.Info(TargetDataLine.class);

  /**
   * Liest die angegebene WAV Datei ein, interpretiert die Signale und liefert die binären Daten in Form einer {@link KcDatei} zurück.
   *
   * @param inputWave Pfad und Name einer zu lesenden WAV Datei
   * @return eine {@link KcDatei} mit den binären Daten
   */
  public KcDatei leseWavDatei(Path inputWave)
  {
    try( AudioInputStream ais = createAudioStream(inputWave) )
    {
      return leseDaten(ais);
    }
    catch( IOException e )
    {
      throw new RuntimeException("Fehler beim Lesen von " + inputWave.toAbsolutePath(), e);
    }
  }

  /**
   * Liest die Sound Daten vom angegebenem Mixer, interpretiert die Signale und liefert die binären Daten in Form einer {@link KcDatei} zurück.
   *
   * @param inputMixerName Name des Sound Eingangs
   * @return eine {@link KcDatei} mit den binären Daten
   * @throws TimeoutException wenn bis zum Ablauf des Timeouts keine Daten gefunden wurden
   */
  public KcDatei leseMixerDaten(String inputMixerName, int readTimeout) throws TimeoutException
  {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    AudioInput audioInput = createAudioInputStream(inputMixerName);
    AudioInputStream ais = audioInput.ais;
    audioInput.line.start();
    try( NullDurchgangWaveAnalyzer waveAnalyzer = new NullDurchgangWaveAnalyzer(ais) )
    {
      KcKassettenReader kcKassettenReader = new KcKassettenReader(waveAnalyzer);
      executor.schedule(new RunnableImplementation(ais, kcKassettenReader), readTimeout, TimeUnit.SECONDS);
      KcDatei kcDatei = kcKassettenReader.leseDatei();
      if(!kcKassettenReader.isAnfangGefunden())
      {
        throw new TimeoutException();
      }
      return kcDatei;
    }
    catch( IOException e )
    {
      throw new RuntimeException("Fehler beim Lesen der Audiodaten von [" + inputMixerName + "].", e);
    }
    finally
    {
      executor.shutdownNow();
      try
      {
        executor.awaitTermination(0, TimeUnit.SECONDS);
      }
      catch( InterruptedException e )  { /* ignore */  }
    }
  }

  /**
   * Liefert den vollständigen Mixername.
   *
   * @return den Mixername oder <code>null</code>, falls der Mixer nicht existiert
   */
  @SuppressWarnings("resource")
  public String getEingabeMixerName(String mixerName)
  {
    Mixer mixer = getMixer(mixerName);
    if( mixer == null )
    {
      return null;
    }
    return mixer.getMixerInfo().getName();
  }

  private Mixer getMixer(String mixerName)
  {
    for( Mixer mixer : getAlleEingabeMixer() )
    {
      if( mixer.getMixerInfo().getName().startsWith(mixerName) )
      {
        return mixer;
      }
    }
    return null;
  }

  /**
   * Liefert alle mixer für die Soundeingabe.
   */
  public List<Mixer> getAlleEingabeMixer()
  {
    ArrayList<Mixer> alleInputMixer = new ArrayList<>();
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      @SuppressWarnings("resource")
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(inputLineInfo) )
      {
        alleInputMixer.add(mixer);
      }
    }
    return alleInputMixer;
  }

  public KcDatei load(Path path)
  {
    byte[] allBytes;
    try
    {
      allBytes = Files.readAllBytes(path);
    }
    catch( IOException e )
    {
      throw new UncheckedIOException("Fehler beim Lesen der Daten von [" + path + "].", e);
    }
    int index = 0;
    KcDatei kcDatei = new KcDatei();
    int blockNr = 1;
    while( index < allBytes.length )
    {
      byte[] blockData = new byte[KcDateiBlock.BLOCK_SIZE];
      int sizeToCopy = Math.min(blockData.length, allBytes.length - index);
      System.arraycopy(allBytes, index, blockData, 0, sizeToCopy);
      index += sizeToCopy;
      if( index >= allBytes.length )
      {
        // letzter Block
        blockNr = 0xff;
      }
      kcDatei.add(new KcDateiBlock(blockNr, KcDateiBlock.berechneChecksumme(blockData), blockData));
      blockNr++;

    }
    return kcDatei;
  }

  private KcDatei leseDaten(AudioInputStream ais) throws IOException
  {
    NullDurchgangWaveAnalyzer waveAnalyzer = new NullDurchgangWaveAnalyzer(ais);
    KcKassettenReader kcKassettenReader = new KcKassettenReader(waveAnalyzer);
    return kcKassettenReader.leseDatei();
  }

  /**
   * Erzeugt einen {@link AudioInputStream} für die übergebene Datei.
   *
   * @param inputPath Pfad zu einer WAV Datei
   * @throws IOException bei
   */
  private AudioInputStream createAudioStream(Path inputPath) throws IOException
  {
    try
    {
      File fileIn = inputPath.toFile();
      AudioInputStream ais = AudioSystem.getAudioInputStream(fileIn);
      // Wrappen in einen CD Audio Stream
      return AudioSystem.getAudioInputStream(createCdAudioFormat(), ais);
    }
    catch( UnsupportedAudioFileException e )
    {
      throw new IOException("Format von [" + inputPath.toAbsolutePath() + "] wird nicht unterstützt", e);
    }
  }

  private AudioInput createAudioInputStream(String inputSourceName)
  {
    Mixer mixer;
    if( inputSourceName.isEmpty() )
    {
      mixer = getDefaultInputMixer();
      if( mixer == null )
      {
        System.out.println("Keine default Eingangs Quelle gefunden, beende Programm.");
        return null;
      }
      System.out.println("Verwende Eingang \"" + mixer.getMixerInfo().getName() + "\" als Default");
    }
    else
    {
      mixer = getMixer(inputSourceName);
      if( mixer == null )
      {
        System.out.println("Keine Quelle gefunden welche mit \"" + inputSourceName + "\" beginnt, beende Programm.");
        return null;
      }
      System.out.println("Verwende Eingang \"" + mixer.getMixerInfo().getName() + "\"");
    }
    AudioFormat audioFormat = createCdAudioFormat();
    DataLine.Info targetDataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
    if( !mixer.isLineSupported(targetDataLineInfo) )
    {
      System.out.println("Einlesen in CD Qualität wird nicht unterstützt, beende Programm.");
      return null;
    }
    try
    {
      TargetDataLine line;
      line = (TargetDataLine)mixer.getLine(targetDataLineInfo);
      line.open();
      return new AudioInput(line, new AudioInputStream(line));
    }
    catch( LineUnavailableException e )
    {
      System.out.println("Fehler beim Zugriff auf [" + mixer.getMixerInfo().getName() + "], beende Programm.");
      return null;
    }
  }

  /**
   * Liefert den default Mixer für die Soundeingabe oder falls es keinen Default gibt den ersten Mixer kann.
   *
   * @return einen Mixer für die Soundeingabe oder <code>null</code>, falls keiner gefunden wurde.
   */
  public Mixer getDefaultInputMixer()
  {
    Mixer defaultMixer = AudioSystem.getMixer(null);
    if( defaultMixer != null && defaultMixer.isLineSupported(inputLineInfo) )
    {
      // wird normalerweise nicht funktionieren, da der Default Mixer meist die Standardausgabe (und nicht Eingabe) ist
      return defaultMixer;
    }
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(inputLineInfo) )
      {
        return mixer;
      }
    }
    return null;
  }

  /**
   * Liefert das Audioformat für Stereo CD Qualität.
   */
  private AudioFormat createCdAudioFormat()
  {
    float sampleRate = 44100;
    int sampleSizeInBits = 16;
    int channels = 2;
    boolean signed = true;
    boolean bigEndian = false;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }

  private static final class RunnableImplementation implements Runnable
  {
    private final AudioInputStream ais;
    private final KcKassettenReader kcKassettenReader;

    private RunnableImplementation(AudioInputStream ais, KcKassettenReader kcKassettenReader)
    {
      this.ais = ais;
      this.kcKassettenReader = kcKassettenReader;
    }

    @Override
    public void run()
    {
      if( !kcKassettenReader.isAnfangGefunden() )
      {
        try
        {
          ais.close();
        }
        catch( IOException e )
        {
          /* ignorieren */ }
      }
    }
  }

  private static class AudioInput
  {
    private TargetDataLine line;
    private AudioInputStream ais;

    public AudioInput(TargetDataLine line, AudioInputStream ais)
    {
      this.line = line;
      this.ais = ais;
    }
  }

}
