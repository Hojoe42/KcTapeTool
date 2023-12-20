package de.hojoe.kctapetool;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import javax.sound.sampled.*;

/**
 * Liest einen {@link AudioInputStream} und kopiert die Daten in mehrere {@link AudioInputStream}s.
 *
 * @author Holger Jödicke
 */
public class StreamCopy
{
  private AudioInputStream inputStream;
  private List<OutputStream> outputStreams = new ArrayList<>();
  private List<AudioInputStream> audioStreams = new ArrayList<>();

  /**
   *
   * @param inputStream der Quell Stream.
   */
  public StreamCopy(AudioInputStream inputStream, int anzahl)
  {
    this.inputStream = Objects.requireNonNull(inputStream);
    AudioFormat audioFormat = inputStream.getFormat();
    for( int i = 0; i < anzahl; i++ )
    {
      try
      {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        outputStreams.add(pipedOutputStream);
        AudioInputStream audioInputStream = new AudioInputStream(new PipedInputStream(pipedOutputStream), audioFormat, AudioSystem.NOT_SPECIFIED);
        audioStreams.add(audioInputStream);
      }
      catch( IOException e )
      {
        throw new RuntimeException(e);
      }
    }
    new Thread(new StreamCopyRunnable()).start();
  }

  /**
   * Liefert alle Ziel {@link AudioInputStream}s.
   */
  public List<AudioInputStream> getAudioStreams()
  {
    return Collections.unmodifiableList(audioStreams);
  }

  /**
   * Kopiert alle Daten aus dem InputStream in die Ziel Streams. Das Runnable läuft bis der Input
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
        // @todo 2023-08-14 Exception irgendwie zur Verfügung stellen
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
    StreamCopy streamCopy = new StreamCopy(audioInputStream, 2);
    List<AudioInputStream> kopien = streamCopy.getAudioStreams();
    AudioInputStream ais1 = kopien.get(0);
    AudioInputStream ais2 = kopien.get(1);
    AudioSystem.write(ais1, AudioFileFormat.Type.WAVE, path1.toFile());
    AudioSystem.write(ais2, AudioFileFormat.Type.WAVE, path2.toFile());
  }

}
