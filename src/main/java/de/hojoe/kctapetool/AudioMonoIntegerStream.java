package de.hojoe.kctapetool;

import java.io.*;

import javax.sound.sampled.*;

public class AudioMonoIntegerStream implements IntegerStream
{
  private OutputStream os;
  private AudioInputStream audioIs;
  private byte[] audioPuffer;
  private int anzahlBytesImPuffer;
  private int pos;
  private int kanal = 0;
  private SamplePuffer samplePuffer;
  private int bytesPerSample;

  public AudioMonoIntegerStream(AudioInputStream ais)
  {
    this.audioIs = ais;
    AudioFormat format = audioIs.getFormat();
    audioPuffer = new byte[1024 * format.getFrameSize()];
    anzahlBytesImPuffer = 0;
    pos = 0;
    samplePuffer = new SamplePuffer(format.getSampleSizeInBits());
    bytesPerSample = format.getSampleSizeInBits() / 8;
//    try
//    {
//      os = new FileOutputStream("tmp.bin");
//    }
//    catch( FileNotFoundException e )
//    {
//      // @todo Auto-generated catch block
//      e.printStackTrace();
//    }
  }

  @Override
  public boolean available()
  {
    try
    {
      fillPuffer();
      return anzahlBytesImPuffer > 0;
    }
    catch( IOException e )
    {
      return false;
    }
  }

  @Override
  public int read() throws IOException
  {
    fillPuffer();
    if( anzahlBytesImPuffer == -1 )
    {
      throw new IOException("keine Daten mehr vorhanden");
    }
    samplePuffer.reset();
    for( int channel = 0; channel < audioIs.getFormat().getChannels(); channel++ )
    {
      if( kanal == channel )
      {
        for( int i = 0; i < bytesPerSample; i++ )
        {
          samplePuffer.add(audioPuffer[pos++]);
        }
      }
      else
      {
        for( int i = 0; i < bytesPerSample; i++ )
        {
          pos++;
        }
      }
    }
    int sample = samplePuffer.getSample();
    return sample;
  }

  /**
   * Füllt den Puffer mit Daten falls nötig.
   */
  private void fillPuffer() throws IOException
  {
    if( pos >= anzahlBytesImPuffer )
    {
      anzahlBytesImPuffer = audioIs.read(audioPuffer);
      pos = 0;
      if( os != null )
      {
        if( anzahlBytesImPuffer == -1 )
        {
          os.close();
        }
        else
        {
          os.write(audioPuffer, 0, anzahlBytesImPuffer);
        }
      }
    }
  }

  @Override
  public void close() throws IOException
  {
    audioIs.close();
  }

  public int getKanal()
  {
    return kanal;
  }

  public void setKanal(int kanal)
  {
    this.kanal = kanal;
  }

}
