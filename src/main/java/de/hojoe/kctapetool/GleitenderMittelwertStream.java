package de.hojoe.kctapetool;

import java.io.*;
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

  /**
   * Erster einfacher Test ...
   */
  public static void main(String[] args) throws Exception
  {
    AudioFormat audioFormat = AudioReader.createCdAudioFormat();
    Path quellPath = Paths.get("d:/tmp/kc/Test1.wav");
    Path zielPath = Paths.get("d:/tmp/kc/Test1_gm.wav");
    if( Files.exists(zielPath) )
    {
      System.out.println("Lösche: " + zielPath);
      Files.delete(zielPath);
    }
    try( AudioMonoIntegerStream amis = new AudioMonoIntegerStream(AudioSystem.getAudioInputStream(quellPath.toFile()));
         GleitenderMittelwertStream in =  new GleitenderMittelwertStream(amis,5);
         AudioInputStream ais = new AudioInputStream(new GwInputStream(in), audioFormat, AudioSystem.NOT_SPECIFIED); )
    {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, zielPath.toFile());
    }
    catch( UnsupportedAudioFileException e )
    {
      throw new IOException("Format von [" + quellPath.toAbsolutePath() + "] wird nicht unterstützt", e);
    }
    if( !Files.isRegularFile(zielPath) )
    {
      System.out.println(zielPath + " existiert nicht!");
    }
    else
    {
      System.out.println(zielPath + " existiert :-)");
    }
  }

  static class GwInputStream extends InputStream
  {
    private GleitenderMittelwertStream in;
    private Integer tmp = null;

    public GwInputStream(GleitenderMittelwertStream in)
    {
      this.in = in;
    }

    @Override
    public int read() throws IOException
    {
      if(tmp == null)
      {
        if(!in.available())
        {
          return -1;
        }
        tmp = in.read();
        return (tmp.intValue() >> 8) & 0xff;
      }
      int b = tmp.intValue() & 0xff;
      tmp = null;
      return b;
    }
  }


}
