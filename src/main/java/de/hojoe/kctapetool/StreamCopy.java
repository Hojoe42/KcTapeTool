package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.sound.sampled.*;

public class StreamCopy
{
  boolean running = false;
  private AudioInputStream inputStream;
  private List<OutputStream> outputStreams = new ArrayList<>();

  public StreamCopy(AudioInputStream inputStream)
  {
    this.inputStream = inputStream;
  }

  public AudioInputStream createAudioInputStream()
  {
    if( running )
    {
      throw new IllegalStateException("Streams müssen vor dem Beginn des Kopierens erzeugt werden.");
    }
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    outputStreams.add(pipedOutputStream);
    try
    {
      return new AudioInputStream(new PipedInputStream(pipedOutputStream), inputStream.getFormat(), AudioSystem.NOT_SPECIFIED);
    }
    catch( IOException e )
    {
      throw new RuntimeException(e);
    }
  }

  public void start()
  {
    running = true;
    new Thread(new StreamCopyRunnable()).start();
  }

  /**
   * Kopiert alle Daten aus dem InputStream in die Target Streams. Das Runnable läuft bis der Input
   * Stream -1 oder eine Exception liefert.
   *
   * @author Holger Jödicke
   */
  private final class StreamCopyRunnable implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        byte[] bytes = new byte[64 * inputStream.getFormat().getFrameSize()];
        int anzahl;
        while( (anzahl = inputStream.read(bytes, 0, bytes.length)) >= 0 )
        {
          for( OutputStream os : outputStreams )
          {
            os.write(bytes, 0, anzahl);
          }
        }
        for( OutputStream os : outputStreams )
        {
          os.close();
        }
      }
      catch( IOException e )
      {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    Path path = Paths.get("d:/tmp/kc/Test1.wav");
    Path path1 = Paths.get("d:/tmp/kc/Test-Out1.wav");
    Path path2 = Paths.get("d:/tmp/kc/Test-Out2.wav");
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile());
    StreamCopy streamCopy = new StreamCopy(audioInputStream);
    AudioInputStream ais1 = streamCopy.createAudioInputStream();
    AudioInputStream ais2 = streamCopy.createAudioInputStream();
    streamCopy.start();
    AudioSystem.write(ais1, AudioFileFormat.Type.WAVE, path1.toFile());
    AudioSystem.write(ais2, AudioFileFormat.Type.WAVE, path2.toFile());
  }

}
