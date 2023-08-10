package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;

import picocli.CommandLine.*;

/**
 * Implementation des 'read' Kommandos.
 *
 * @author Holger Jödicke
 */
@Command(name = "read", description = "Kommando zum Lesen von Audiodaten, insbesondere von Daten die mit 'SAVE' auf dem KC gespeicherten werden oder zum Lesen von WAVE Dateien")
public class KcReader implements Callable<Integer>
{

  private static final String INPUT_SOURCE_DESC = "Name der Input Source, kann abgekürzt werden, solange es eindeutig ist. Siehe --list-sources. " +
    "Wenn nichts angegeben ist, dann wird die erste gefundene Quelle verwendet. Wird ignoriert wenn aus einer Wave Datei gelesen wird.";

  @Option(names = {"-h", "--help"},
          usageHelp = true,
          description = "Zeigt diese Hilfe und beendet das Programm")
  boolean usageHelpRequested;

  @Option(names = {"-l", "--list-sources"},
          description = "Liste aller Sound Eingänge ausgeben und beendet das Programm")
  private boolean listSources;

  @Option(names = {"-f", "--file"},
          description = "Liest aus der angegebenen WAVE Datei",
          paramLabel = "<WAVE-DATEI>")
  private Path file;

  @Option(names = {"-i", "--input-source"},
          description = INPUT_SOURCE_DESC,
          paramLabel = "<Input Quelle>", defaultValue = "")
  private String inputSourceName;

  @ParentCommand
  private KcTapeTool kcTabpeTool;

  /**
   * Hier beginnt das Lesen vom KC und schreiben der Daten / Dateien auf den PC.
   */
  @Override
  public Integer call() throws Exception
  {
    if( listSources )
    {
      System.out.println("Verfügbare Input Geräte:");
      getAlleInputGeraeteNamen().forEach(s -> System.out.println("  " + s));
      return KcTapeTool.RC_OK;
    }
    try
    {
      AudioInputStream ais;
      if( this.file != null )
      {
        try
        {
          File fileIn = file.toFile();
          ais = AudioSystem.getAudioInputStream(fileIn);
          ais = AudioSystem.getAudioInputStream(getCdAudioFormat(), ais);
        }
        catch( UnsupportedAudioFileException e )
        {
          throw new IOException("Format von [" + file.toAbsolutePath() + "] wird nicht unterstützt", e);
        }
      }
      else
      {
        AudioInput audioInput = createAudioInputStream();
        ais = audioInput.ais;
//        new Thread(new Stopper(audioInput.line, 10_000), "Stopper-Thread").start();
        audioInput.line.start();
      }
      System.out.println("Starte Aufnahme (Abbruch mit Ctrl + C)");

// Kopie dazwischen schmuggeln
      StreamCopy streamCopy = new StreamCopy(ais);
      ais = streamCopy.createAudioInputStream();
      AudioInputStream aisKopie = streamCopy.createAudioInputStream();
      streamCopy.start();
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          try
          {
            AudioSystem.write(aisKopie, Type.WAVE, Paths.get("CopyStream.wav").toFile());
          }
          catch( IOException e )
          {
            e.printStackTrace();
          }
        }
      }, "Datei-Kopierer").start();

      NullDurchgangWaveAnalyzer waveAnalyzer = new NullDurchgangWaveAnalyzer(ais);
      KcKassettenReader kcKassettenReader = new KcKassettenReader(waveAnalyzer);
      KcDatei kcDatei = kcKassettenReader.leseDatei();
      Path path = Paths.get(kcDatei.getFullDateiName());
      schreibeDatei(path, kcDatei);
      System.out.println(path + " wurde geschrieben");
    }
    catch( IOException e )
    {
      System.out.println("IO Fehler beim Lesen der Daten: " + e.getLocalizedMessage());
      throw new RuntimeException("IO Fehler beim Lesen der Daten", e);
    }
    return KcTapeTool.RC_OK;
  }

  private void schreibeDatei(Path path, KcDatei kcDatei) throws IOException
  {
    PcDateiKonverter konverter = getKonverter(kcDatei);
    try (OutputStream os = Files.newOutputStream(path))
    {
      konverter.write(os, kcDatei);
    }
  }

  private PcDateiKonverter getKonverter(KcDatei kcDatei)
  {
    return kcDatei.isBasicDateiType() ? new BasicConverter() : new CaosKonverter();
  }

  /**
   * Liefert die Namen aller Sound Input Geräte / Mixer. Mit diesem Namen kann sich der passende
   * Mixer mittels <code>AudioSystem.getMixer(mixerInfo)</code> geholt werden.
   */
  private List<String> getAlleInputGeraeteNamen()
  {
    ArrayList<String> namen = new ArrayList<>();
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      for( Line.Info targetLineInfo : mixer.getTargetLineInfo() )
      {
        try
        {
          Line line = AudioSystem.getLine(targetLineInfo);
          if( line instanceof TargetDataLine )
          {
            namen.add(mixerInfo.getName());
          }
        }
        catch( Exception e )
        {
          System.out.println("Line für " + targetLineInfo + " nicht verfügbar: " + e.getMessage());
          continue;
        }
      }
    }
    return namen;
  }

  private AudioInput createAudioInputStream()
  {
    Mixer mixer;
    if( inputSourceName.isEmpty() )
    {
      mixer = getDefaultMixer();
      if( mixer == null )
      {
        System.out.println("Keinen Default Eingangs Quelle gefunden, beende Programm.");
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

  private Mixer getMixer(String inputSourceName)
  {
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      if( mixerInfo.getName().startsWith(inputSourceName) )
      {
        return AudioSystem.getMixer(mixerInfo);
      }
    }
    return null;
  }

  private Mixer getDefaultMixer()
  {
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      for( Line.Info targetLineInfo : mixer.getTargetLineInfo() )
      {
        try
        {
          Line line = AudioSystem.getLine(targetLineInfo);
          if( line instanceof TargetDataLine )
          {
            return mixer;
          }
        }
        catch( Exception e )
        {
          System.out.println("Line für " + targetLineInfo + " nicht verfügbar: " + e.getMessage());
          continue;
        }
      }
    }
    return null;
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

  private final static class Stopper implements Runnable
  {
    private final TargetDataLine line;

    private int millis;

    private Stopper(TargetDataLine line, int millis)
    {
      this.line = line;
      this.millis = millis;
    }

    @Override
    public void run()
    {
      try
      {
        Thread.sleep(millis);
      }
      catch( InterruptedException e )
      {
        // continue
      }
      line.stop();
      line.close();
    }
  }

  static class AudioInput
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
