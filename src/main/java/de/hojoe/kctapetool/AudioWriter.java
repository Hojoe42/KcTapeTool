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

  private boolean playback;
  private String playbackDevice;

  /**
   * Aktiviert oder deaktiviert das Playback.
   */
  public void setPlayback(boolean playback)
  {
    this.playback = playback;
  }

  /**
   * Setzt das zu verwendende Playback Ausgabegerät.
   */
  public void setPlaybackDevice(String playbackDevice)
  {
    this.playbackDevice = playbackDevice;
  }


  @SuppressWarnings("resource")
  public void schreibeAudioMixer(List<KcDatei> kcDatei, String ausgabeMixerName)
  {
    Mixer mixer = getAusgabeMixer(ausgabeMixerName);
    try( KcAudioInputStream kcAudioInputStream = new KcAudioInputStream(kcDatei) )
    {
      Mixer playbackMixer = null;
      if( playback )
      {
        playbackMixer = getAusgabeMixer(playbackDevice);
      }
      StreamCopy streamCopy = new StreamCopy(kcAudioInputStream, playbackMixer == null ? 1 : 2);
      List<AudioInputStream> audioStreams = streamCopy.getAudioStreams();
      if( playbackMixer != null )
      {
        schreibeZumMixerMixer(audioStreams.get(1), playbackMixer, false);
      }
      schreibeZumMixerMixer(audioStreams.get(0), mixer, true);
    }
    catch( IOException e )
    {
      throw new UncheckedIOException(e);
    }
  }

  public void schreibeZumMixerMixer(AudioInputStream ais, Mixer mixer, boolean synchron)
  {
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, ais.getFormat());
    try
    {
      @SuppressWarnings("resource") // wird im copy geschlossen
      SourceDataLine sourceLine = (SourceDataLine)mixer.getLine(info);
      sourceLine.open(ais.getFormat());
      sourceLine.start();
      copy(ais, sourceLine, synchron);
    }
    catch( LineUnavailableException e )
    {
      throw new RuntimeException("Fehler beim Öffnen des Mixers: " + mixer.getMixerInfo().getName(), e);
    }
  }

  /**
   * Kopiert alle Daten aus dem übergebenen {@link InputStream} in die {@link SourceDataLine}.
   */
  public void copy(InputStream inputStream, SourceDataLine sourceLine, boolean synchron)
  {
    Runnable runable = () ->
    {
      try
      {
        // der Puffer muss ein vielfaches der Framesize sein
        int frameSize = sourceLine.getFormat().getFrameSize();
        int bufferSize = Math.min(sourceLine.getBufferSize(), frameSize * 64 * 1024);
        byte[] audioPuffer = new byte[bufferSize];
        int readCount = 0;
        while( (readCount = inputStream.read(audioPuffer, 0, audioPuffer.length)) != -1 )
        {
          if( readCount >= 0 )
          {
            int writeCount = sourceLine.write(audioPuffer, 0, readCount);
            if(writeCount < readCount)
            {
              System.out.println("Ausgabe [" + sourceLine.getLineInfo() + "] wurde geschlossen oder gestoppt");
              break;
            }
          }
        }
      }
      catch( IOException e )
      {
        throw new UncheckedIOException(e);
      }
      finally
      {
        sourceLine.close();
      }
    };
    if(synchron)
    {
      runable.run();
    }
    else
    {
      new Thread(runable, "AudioWriter Playback Thread").start();
    }
  }

  /**
   * Liefert den Ausgabemixer zum übergebenen Namen oder falls der <code>null</code> ist, den Defaultmixer.
   *
   * @return einen Ausgabemixer oder null falls es keinen gibt.
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
