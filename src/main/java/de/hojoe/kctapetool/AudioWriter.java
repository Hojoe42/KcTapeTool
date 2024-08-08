package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.Line.Info;


/**
 * Methoden zum Schreiben von Audiodaten.
 *
 * @author Holger Jödicke
 */
public class AudioWriter
{
  /** {@link Info} Objekt für Abspiel Geräte. */
  private static final Line.Info outputLineInfo = new Line.Info(SourceDataLine.class);

  /**
   * Kopiert alle Daten aus dem {@link KcAudioInputStream} in die {@link SourceDataLine}.
   */
  public void copy(KcAudioInputStream kcAudioInputStream, SourceDataLine sourceLine) throws IOException
  {
    int frameSize = kcAudioInputStream.getAudioFormat().getFrameSize();
    // der Puffer muss ein vielfaches der Framesize sein
    byte[] audioPuffer = new byte[frameSize * 64 * 1024];
    int readCount = 0;
    while( (readCount = kcAudioInputStream.read(audioPuffer, 0, audioPuffer.length)) != -1 )
    {
      if( readCount >= 0 )
      {
        int writeCount = sourceLine.write(audioPuffer, 0, readCount);
        if(writeCount < readCount)
        {
          System.out.println("Ausgabe wurde geschlossen oder gestoppt");
          break;
        }
      }
    }
  }

  /**
   * Liefert den Ausgabemixer zum übergebenen Namen oder falls der <code>null</code> ist, den Defaultmixer.
   */
  public Mixer getAusgabeMixer(String ausgabeMixerName)
  {
    Mixer mixer;
    if(ausgabeMixerName == null)
    {
      mixer = getDefaultAusgabeMixer();
      if(mixer == null)
      {
        throw new RuntimeException("Konnte keinen default Ausgabemixer öffnen.");
      }
    }
    else
    {
      mixer = getMixer(ausgabeMixerName);
      if(mixer == null)
      {
        throw new RuntimeException(String.format("Konnte Ausgabemixer [%s] nicht öffnen.", ausgabeMixerName));
      }
    }
    return mixer;
  }

  /**
   * Liefert den vollständigen Mixername oder <code>null</code> wenn es keinen passenden Mixer gibt.
   *
   * @return den Mixername oder <code>null</code>, falls der Mixer nicht existiert
   */
  public String getAusgabeMixerName(String ausgabeMixerName)
  {
    Mixer mixer = getMixer(ausgabeMixerName);
    if(mixer != null)
    {
      return mixer.getMixerInfo().getName();
    }
    return null;
  }

  private Mixer getMixer(String ausgabeMixerName)
  {
    for( Mixer mixer : getAlleAusgabeMixer() )
    {
      if(mixer.getMixerInfo().getName().startsWith(ausgabeMixerName))
      {
        return mixer;
      }
    }
    return null;
  }

  /**
   * Liefert die Mixer aller Sound Ausgabe Geräte / Mixer.
   */
  public List<Mixer> getAlleAusgabeMixer()
  {
    ArrayList<Mixer> alleOutputMixer = new ArrayList<>();
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      @SuppressWarnings("resource")
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(outputLineInfo) )
      {
        alleOutputMixer.add(mixer);
      }
    }
    return alleOutputMixer;
  }

  /**
   * Schreibt die übergebene {@link KcDatei} in eine WAV Datei.
   */
  public void schreibeAudioDatei(Path path, List<KcDatei> kcDatei)
  {
    try( KcAudioInputStream kcAudioInputStream = new KcAudioInputStream(kcDatei);
      AudioInputStream ais = new AudioInputStream(kcAudioInputStream, kcAudioInputStream.getAudioFormat(), AudioSystem.NOT_SPECIFIED) )
    {
      AudioSystem.write(ais, Type.WAVE, path.toFile());
    }
    catch( IOException e )
    {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Liefert den default Mixer für die Soundausgabe oder falls es keinen Default gibt den ersten Mixer der etwas ausgeben kann.
   *
   * @return einen Mixer für die Soundausgabe oder <code>null</code>, falls keiner gefunden wurde.
   */
  public Mixer getDefaultAusgabeMixer()
  {
    Mixer defaultMixer = AudioSystem.getMixer(null);
    if( defaultMixer != null && defaultMixer.isLineSupported(outputLineInfo) )
    {
      return defaultMixer;
    }
    for( Mixer.Info mixerInfo : AudioSystem.getMixerInfo() )
    {
      Mixer mixer = AudioSystem.getMixer(mixerInfo);
      if( mixer.isLineSupported(outputLineInfo) )
      {
        return mixer;
      }
    }
    return null;
  }

}
