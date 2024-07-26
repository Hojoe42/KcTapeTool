package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;

import javax.sound.sampled.*;


/**
 * Code zum Einlesen von Sound Daten und extrahieren der aufmodulierten Daten.
 *
 * @author Holger Jödicke
 */
public class AudioReader
{

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
   */
  public KcDatei leseMixerDaten(String inputMixerName)
  {
    try
    {
      AudioInput audioInput = createAudioInputStream(inputMixerName);
      AudioInputStream ais = audioInput.ais;
      audioInput.line.start();
      NullDurchgangWaveAnalyzer waveAnalyzer = new NullDurchgangWaveAnalyzer(ais);
      KcKassettenReader kcKassettenReader = new KcKassettenReader(waveAnalyzer);
      KcDatei kcDatei = kcKassettenReader.leseDatei();
      return kcDatei;
    }
    catch( IOException e )
    {
      throw new RuntimeException("Fehler beim Lesen der Audiodaten von [" + inputMixerName + "].", e);
    }
  }

  /**
   * Liefert den vollständigen Mixername.
   *
   * @return den Mixername oder <code>null</code>, falls der Mixer nicht existiert
   */
  public String getEingabeMixerName(String mixerName)
  {
    for( Mixer mixer : AudioGeraete.getAlleInputGeraeteNamen() )
    {
      String name = mixer.getMixerInfo().getName();
      if( name.startsWith(mixerName) )
      {
        return name;
      }
    }
    return null;
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
      // todo ist eigentlich nicht immer notwendig, oder?
      return AudioSystem.getAudioInputStream(getCdAudioFormat(), ais);
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
      mixer = AudioGeraete.getDefaultInputMixer();
      if( mixer == null )
      {
        System.out.println("Keine default Eingangs Quelle gefunden, beende Programm.");
        return null;
      }
      System.out.println("Verwende Eingang \"" + mixer.getMixerInfo().getName() + "\" als Default");
    }
    else
    {
      mixer = AudioGeraete.getMixer(inputSourceName);
      if( mixer == null )
      {
        System.out.println("Keine Quelle gefunden welche mit \"" + inputSourceName + "\" beginnt, beende Programm.");
        return null;
      }
      System.out.println("Verwende Eingang \"" + mixer.getMixerInfo().getName() + "\"");
    }
    AudioFormat audioFormat = getCdAudioFormat();
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
   * Liefert das Audioformat für Stereo CD Qualität.
   */
  private AudioFormat getCdAudioFormat()
  {
    float sampleRate = 44100;
    int sampleSizeInBits = 16;
    int channels = 2;
    boolean signed = true;
    boolean bigEndian = false;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
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
