package de.hojoe.kctapetool;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import javax.sound.sampled.*;

public class GleitenderMittelwertStream implements IntegerStream
{

  private int fensterLaenge;
  private IntegerStream is;
  private Deque<Integer> puffer;
  private int summe = 0;

  public GleitenderMittelwertStream(IntegerStream is, int fensterLaenge)
  {
    this.is = is;
    this.fensterLaenge = fensterLaenge;
    puffer = new ArrayDeque<>(fensterLaenge);
  }


  /**
   * Liefert <code>true</code> wenn noch Daten zum Lesen vorhanden sind.
   */
  @Override
  public boolean available()
  {
    return !puffer.isEmpty() || is.available();
  }

  /**
   * Liefert den nächsten Wert.
   */
  @Override
  public int read() throws IOException
  {
    readPuffer();
    int result = (int)Math.round(summe / (double)puffer.size());
    summe -= puffer.removeFirst();
    return result;
  }

  private void readPuffer() throws IOException
  {
    while(puffer.size() < fensterLaenge)
    {
      if(!is.available())
      {
        break;
      }
      int wert = is.read();
      summe += wert;
      puffer.addLast(wert);
    }
  }


  @Override
  public void close() throws IOException
  {
    is.close();
  }

  public static void main(String[] args) throws Exception
  {
    AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, true);
    Path quellPath = Paths.get("d:/tmp/kc/Test1.wav");
    Path zielPath = Paths.get("d:/tmp/kc/Test1_gm.wav");
    try( GleitenderMittelwertStream in =  new GleitenderMittelwertStream(new AudioMonoIntegerStream(AudioSystem.getAudioInputStream(quellPath.toFile())),5))
    {
      TargetDataLine tdl = new TestTargetDataLine(audioFormat, in);
      AudioInputStream ais = new AudioInputStream(tdl);
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, zielPath.toFile());
    }
    catch( UnsupportedAudioFileException e )
    {
      throw new IOException("Format von [" + quellPath.toAbsolutePath() + "] wird nicht unterstützt", e);
    }

  }

}
