package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.Path;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;


/**
 * Methoden zum Schreiben von Audiodaten.
 *
 * @author Holger Jödicke
 */
public class AudioWriter
{

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
      mixer = AudioGeraete.getDefaultAusgabeMixer();
      if(mixer == null)
      {
        throw new RuntimeException("Konnte keinen default Ausgabemixer öffnen.");
      }
    }
    else
    {
      mixer = AudioGeraete.getMixer(ausgabeMixerName);
      if(mixer == null)
      {
        throw new RuntimeException(String.format("Konnte Ausgabemixer [%s] nicht öffnen.", ausgabeMixerName));
      }
    }
    return mixer;
  }

  public void schreibeAudioDatei(Path path, KcDatei kcDatei)
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

}
